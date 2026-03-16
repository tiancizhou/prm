package com.prm.module.release.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prm.common.exception.BizException;
import com.prm.common.util.SecurityUtil;
import com.prm.module.project.domain.ProjectAccessPolicy;
import com.prm.module.project.entity.ProjectMember;
import com.prm.module.project.mapper.ProjectMemberMapper;
import com.prm.module.release.dto.CreateReleaseRequest;
import com.prm.module.release.dto.ReleaseDTO;
import com.prm.module.release.entity.Release;
import com.prm.module.release.mapper.ReleaseMapper;
import com.prm.module.system.entity.SysUser;
import com.prm.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReleaseService {

    private final ReleaseMapper releaseMapper;
    private final SysUserMapper userMapper;
    private final ProjectMemberMapper projectMemberMapper;

    public IPage<ReleaseDTO> page(int pageNum, int pageSize, Long projectId) {
        Page<Release> pageReq = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Release> wrapper = new LambdaQueryWrapper<Release>()
                .eq(Release::getDeleted, 0)
                .orderByDesc(Release::getCreatedAt);

        if (!SecurityUtil.isSuperAdmin()) {
            Long currentUserId = SecurityUtil.getCurrentUserId();
            if (projectId != null) {
                ProjectMember membership = findMembership(projectId, currentUserId);
                if (membership == null) {
                    return new Page<>(pageNum, pageSize);
                }
            } else {
                List<ProjectMember> memberships = projectMemberMapper.selectList(
                        new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getUserId, currentUserId));
                if (memberships.isEmpty()) {
                    return new Page<>(pageNum, pageSize);
                }
                Set<Long> projectIds = memberships.stream()
                        .map(ProjectMember::getProjectId)
                        .collect(Collectors.toSet());
                wrapper.in(Release::getProjectId, projectIds);
            }
        }

        if (projectId != null) wrapper.eq(Release::getProjectId, projectId);
        return releaseMapper.selectPage(pageReq, wrapper).convert(this::toDTO);
    }

    @Transactional
    public ReleaseDTO create(CreateReleaseRequest request) {
        ensureProjectManager(request.getProjectId());
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Release release = new Release();
        release.setProjectId(request.getProjectId());
        release.setSprintId(request.getSprintId());
        release.setVersion(request.getVersion());
        release.setDescription(request.getDescription());
        release.setStatus("DRAFT");
        release.setReleaseDate(request.getReleaseDate());
        release.setDeleted(0);
        release.setCreatedBy(currentUserId);
        release.setUpdatedBy(currentUserId);
        releaseMapper.insert(release);
        return toDTO(release);
    }

    @Transactional
    public ReleaseDTO publish(Long id) {
        Release release = releaseMapper.selectById(id);
        if (release == null) throw BizException.notFound("版本");
        ensureProjectManager(release.getProjectId());
        if (!"DRAFT".equals(release.getStatus())) throw BizException.of("版本非草稿状态无法发布");
        Long currentUserId = SecurityUtil.getCurrentUserId();
        release.setStatus("RELEASED");
        release.setReleasedBy(currentUserId);
        release.setReleasedAt(LocalDateTime.now());
        release.setUpdatedBy(currentUserId);
        releaseMapper.updateById(release);
        return toDTO(release);
    }

    public ReleaseDTO getById(Long id) {
        Release release = releaseMapper.selectById(id);
        if (release == null || release.getDeleted() == 1) throw BizException.notFound("版本");
        ensureReadable(release);
        return toDTO(release);
    }

    private void ensureReadable(Release release) {
        if (SecurityUtil.isSuperAdmin()) {
            return;
        }
        Long currentUserId = SecurityUtil.getCurrentUserId();
        ProjectMember membership = findMembership(release.getProjectId(), currentUserId);
        if (membership == null) {
            throw BizException.forbidden("无项目查看权限");
        }
    }

    private void ensureProjectManager(Long projectId) {
        if (SecurityUtil.isSuperAdmin()) {
            return;
        }
        Long currentUserId = SecurityUtil.getCurrentUserId();
        ProjectMember membership = findMembership(projectId, currentUserId);
        if (!isProjectManager(membership)) {
            throw BizException.forbidden("仅项目经理可查看版本");
        }
    }

    private ProjectMember findMembership(Long projectId, Long userId) {
        return projectMemberMapper.selectOne(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId));
    }

    private boolean isProjectManager(ProjectMember membership) {
        return ProjectAccessPolicy.canManage(membership);
    }

    private ReleaseDTO toDTO(Release r) {
        ReleaseDTO dto = new ReleaseDTO();
        dto.setId(r.getId());
        dto.setProjectId(r.getProjectId());
        dto.setSprintId(r.getSprintId());
        dto.setVersion(r.getVersion());
        dto.setDescription(r.getDescription());
        dto.setStatus(r.getStatus());
        dto.setReleaseDate(r.getReleaseDate());
        dto.setReleasedBy(r.getReleasedBy());
        dto.setReleasedAt(r.getReleasedAt());
        dto.setCreatedAt(r.getCreatedAt());
        if (r.getReleasedBy() != null) {
            SysUser u = userMapper.selectById(r.getReleasedBy());
            if (u != null) dto.setReleasedByName(u.getNickname());
        }
        return dto;
    }
}

