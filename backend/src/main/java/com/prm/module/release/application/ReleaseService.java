package com.prm.module.release.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prm.common.exception.BizException;
import com.prm.common.util.SecurityUtil;
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

@Service
@RequiredArgsConstructor
public class ReleaseService {

    private final ReleaseMapper releaseMapper;
    private final SysUserMapper userMapper;

    public IPage<ReleaseDTO> page(int pageNum, int pageSize, Long projectId) {
        Page<Release> pageReq = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Release> wrapper = new LambdaQueryWrapper<Release>()
                .eq(Release::getDeleted, 0)
                .orderByDesc(Release::getCreatedAt);
        if (projectId != null) wrapper.eq(Release::getProjectId, projectId);
        return releaseMapper.selectPage(pageReq, wrapper).convert(this::toDTO);
    }

    @Transactional
    public ReleaseDTO create(CreateReleaseRequest request) {
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
        if (!"DRAFT".equals(release.getStatus())) throw BizException.of("只有草稿状态的版本可以发布");
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
        return toDTO(release);
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
