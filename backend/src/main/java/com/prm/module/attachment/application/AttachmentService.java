package com.prm.module.attachment.application;

import com.prm.common.exception.BizException;
import com.prm.common.util.SecurityUtil;
import com.prm.module.attachment.dto.AttachmentDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.prm.module.attachment.entity.Attachment;
import com.prm.module.attachment.mapper.AttachmentMapper;
import com.prm.module.requirement.entity.Requirement;
import com.prm.module.requirement.mapper.RequirementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentMapper attachmentMapper;
    private final RequirementMapper requirementMapper;

    @Value("${prm.upload.path:${user.home}/prm-uploads}")
    private String uploadBasePath;

    private static final String BIZ_REQUIREMENT = "REQUIREMENT";
    private static final int MAX_FILENAME_LEN = 200;

    /** 解析为绝对路径，避免在 Tomcat 工作目录下使用相对路径导致路径不存在 */
    private Path getUploadRoot() {
        Path p = Paths.get(uploadBasePath);
        if (!p.isAbsolute()) {
            p = Paths.get(System.getProperty("user.dir", ".")).resolve(p);
        }
        return p.toAbsolutePath().normalize();
    }

    /** 仅需求负责人或管理员可访问（查看列表、下载） */
    private void ensureCanAccessRequirement(Long requirementId) {
        Requirement req = requirementMapper.selectById(requirementId);
        if (req == null || req.getDeleted() == 1) throw BizException.notFound("需求");
        if (SecurityUtil.isManager()) return;
        if (req.getAssigneeId() == null || !req.getAssigneeId().equals(SecurityUtil.getCurrentUserId())) {
            throw BizException.forbidden("无权访问该需求的附件");
        }
    }

    @Transactional
    public AttachmentDTO uploadForRequirement(Long requirementId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BizException.of("请选择要上传的文件");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "未命名";
        }
        if (originalFilename.length() > MAX_FILENAME_LEN) {
            originalFilename = originalFilename.substring(0, MAX_FILENAME_LEN);
        }
        String safeName = UUID.randomUUID().toString().replace("-", "") + "_" + originalFilename.replaceAll("[\\\\/:*?\"<>|]", "_");
        Path root = getUploadRoot();
        Path dir = root.resolve(BIZ_REQUIREMENT).resolve(String.valueOf(requirementId));
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw BizException.of("创建上传目录失败: " + e.getMessage());
        }
        Path target = dir.resolve(safeName);
        try {
            file.transferTo(target);
        } catch (IOException e) {
            throw BizException.of("保存文件失败: " + e.getMessage());
        }
        String relativePath = BIZ_REQUIREMENT + "/" + requirementId + "/" + safeName;
        Long userId = SecurityUtil.getCurrentUserId();
        Attachment att = new Attachment();
        att.setBizType(BIZ_REQUIREMENT);
        att.setBizId(requirementId);
        att.setFilename(originalFilename);
        att.setFilepath(relativePath);
        att.setFileSize(file.getSize());
        att.setMimeType(file.getContentType());
        att.setUploaderId(userId);
        att.setDeleted(0);
        attachmentMapper.insert(att);
        return toDTO(att);
    }

    public List<AttachmentDTO> listByRequirement(Long requirementId) {
        ensureCanAccessRequirement(requirementId);
        List<Attachment> list = attachmentMapper.selectList(
                new LambdaQueryWrapper<Attachment>()
                        .eq(Attachment::getBizType, BIZ_REQUIREMENT)
                        .eq(Attachment::getBizId, requirementId)
                        .orderByDesc(Attachment::getCreatedAt)
        );
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long requirementId, Long attachmentId) {
        Attachment att = attachmentMapper.selectById(attachmentId);
        if (att == null || att.getDeleted() == 1) throw BizException.notFound("附件");
        if (!BIZ_REQUIREMENT.equals(att.getBizType()) || !requirementId.equals(att.getBizId())) {
            throw BizException.forbidden("无权删除该附件");
        }
        attachmentMapper.deleteById(attachmentId);
    }

    /**
     * 获取附件文件用于下载，仅需求负责人或管理员可调用。
     * @return 文件路径与原始文件名
     */
    public AttachmentDownloadResult getFileForDownload(Long requirementId, Long attachmentId) {
        ensureCanAccessRequirement(requirementId);
        Attachment att = attachmentMapper.selectById(attachmentId);
        if (att == null || att.getDeleted() == 1) throw BizException.notFound("附件");
        if (!BIZ_REQUIREMENT.equals(att.getBizType()) || !requirementId.equals(att.getBizId())) {
            throw BizException.forbidden("无权下载该附件");
        }
        Path fullPath = getUploadRoot().resolve(att.getFilepath());
        if (!Files.isRegularFile(fullPath)) throw BizException.notFound("附件文件不存在");
        return new AttachmentDownloadResult(fullPath, att.getFilename());
    }

    /** 下载结果：路径 + 原始文件名（用于 Content-Disposition） */
    public static class AttachmentDownloadResult {
        public final Path path;
        public final String filename;
        public AttachmentDownloadResult(Path path, String filename) {
            this.path = path;
            this.filename = filename != null ? filename : "download";
        }
    }

    private AttachmentDTO toDTO(Attachment a) {
        AttachmentDTO dto = new AttachmentDTO();
        dto.setId(a.getId());
        dto.setBizType(a.getBizType());
        dto.setBizId(a.getBizId());
        dto.setFilename(a.getFilename());
        dto.setFilepath(a.getFilepath());
        dto.setFileSize(a.getFileSize());
        dto.setMimeType(a.getMimeType());
        dto.setUploaderId(a.getUploaderId());
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }
}
