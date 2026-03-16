package com.prm.module.attachment.controller;

import com.prm.common.result.R;
import com.prm.module.attachment.application.AttachmentService;
import com.prm.module.attachment.dto.AttachmentDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "附件")
@RestController
@RequestMapping("/api/requirements")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @Operation(summary = "上传需求附件")
    @PostMapping("/{requirementId}/attachments")
    public R<AttachmentDTO> upload(
            @PathVariable Long requirementId,
            @RequestParam("file") MultipartFile file) {
        return R.ok(attachmentService.uploadForRequirement(requirementId, file));
    }

    @Operation(summary = "需求附件列表")
    @GetMapping("/{requirementId}/attachments")
    public R<List<AttachmentDTO>> list(@PathVariable Long requirementId) {
        return R.ok(attachmentService.listByRequirement(requirementId));
    }

    @Operation(summary = "删除需求附件")
    @DeleteMapping("/{requirementId}/attachments/{attachmentId}")
    public R<Void> delete(@PathVariable Long requirementId, @PathVariable Long attachmentId) {
        attachmentService.delete(requirementId, attachmentId);
        return R.ok();
    }

    @Operation(summary = "下载需求附件")
    @GetMapping("/{requirementId}/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long requirementId,
            @PathVariable Long attachmentId) {
        var result = attachmentService.getFileForDownload(requirementId, attachmentId);
        Resource resource = new FileSystemResource(result.path.toFile());
        // RFC 5987：filename* 用于现代浏览器（支持 UTF-8 中文）
        String encodedFilename = URLEncoder.encode(result.filename, StandardCharsets.UTF_8)
                .replace("+", "%20");
        // filename= 用于兼容旧浏览器，非 ASCII 字符先转义为 ? 避免头部非法字节
        String asciiFilename = result.filename.replaceAll("[^\\x20-\\x7E]", "?");
        String contentDisposition = "attachment; filename=\"" + asciiFilename
                + "\"; filename*=UTF-8''" + encodedFilename;
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }
}
