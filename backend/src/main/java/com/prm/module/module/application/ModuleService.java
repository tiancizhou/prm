package com.prm.module.module.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.prm.common.exception.BizException;
import com.prm.common.util.SecurityUtil;
import com.prm.module.module.dto.ModuleDTO;
import com.prm.module.module.dto.SaveModuleRequest;
import com.prm.module.module.entity.PrmModule;
import com.prm.module.module.mapper.ModuleMapper;
import com.prm.module.project.entity.ProjectMember;
import com.prm.module.project.mapper.ProjectMemberMapper;
import com.prm.module.requirement.entity.Requirement;
import com.prm.module.requirement.mapper.RequirementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleMapper moduleMapper;
    private final RequirementMapper requirementMapper;
    private final ProjectMemberMapper projectMemberMapper;

    public List<ModuleDTO> listTree(Long projectId) {
        ensureProjectMember(projectId);
        List<PrmModule> all = moduleMapper.selectList(
                new LambdaQueryWrapper<PrmModule>()
                        .eq(PrmModule::getProjectId, projectId)
                        .eq(PrmModule::getDeleted, 0)
                        .orderByAsc(PrmModule::getSortOrder)
                        .orderByAsc(PrmModule::getId));

        return buildTree(all, null);
    }

    private List<ModuleDTO> buildTree(List<PrmModule> all, Long parentId) {
        return all.stream()
                .filter(m -> Objects.equals(m.getParentId(), parentId))
                .map(m -> {
                    ModuleDTO dto = toDTO(m);
                    dto.setChildren(buildTree(all, m.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public ModuleDTO create(Long projectId, SaveModuleRequest req) {
        ensureProjectManager(projectId);
        if (req.getParentId() != null) {
            PrmModule parent = moduleMapper.selectById(req.getParentId());
            if (parent == null || parent.getDeleted() == 1) {
                throw BizException.notFound("父模块");
            }
            ensureModuleBelongsToProject(parent, projectId);
        }
        PrmModule m = new PrmModule();
        m.setProjectId(projectId);
        m.setParentId(req.getParentId());
        m.setName(req.getName());
        m.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        m.setDeleted(0);
        m.setCreatedBy(SecurityUtil.getCurrentUserId());
        m.setCreatedAt(LocalDateTime.now());
        m.setUpdatedAt(LocalDateTime.now());
        moduleMapper.insert(m);
        return toDTO(m);
    }

    public ModuleDTO update(Long projectId, Long id, SaveModuleRequest req) {
        ensureProjectManager(projectId);
        PrmModule m = moduleMapper.selectById(id);
        if (m == null || m.getDeleted() == 1) {
            throw BizException.notFound("模块");
        }
        ensureModuleBelongsToProject(m, projectId);
        m.setName(req.getName());
        if (req.getSortOrder() != null) {
            m.setSortOrder(req.getSortOrder());
        }
        m.setUpdatedBy(SecurityUtil.getCurrentUserId());
        m.setUpdatedAt(LocalDateTime.now());
        moduleMapper.updateById(m);
        return toDTO(m);
    }

    public void delete(Long projectId, Long id) {
        ensureProjectManager(projectId);
        PrmModule m = moduleMapper.selectById(id);
        if (m == null || m.getDeleted() == 1) {
            throw BizException.notFound("模块");
        }
        ensureModuleBelongsToProject(m, projectId);
        // 软删除模块
        m.setDeleted(1);
        m.setUpdatedBy(SecurityUtil.getCurrentUserId());
        m.setUpdatedAt(LocalDateTime.now());
        moduleMapper.updateById(m);

        // 将该模块下的需求 module_id 置为 null
        requirementMapper.update(null,
                new LambdaUpdateWrapper<Requirement>()
                        .eq(Requirement::getModuleId, id)
                        .set(Requirement::getModuleId, null));
    }

    private void ensureProjectMember(Long projectId) {
        if (SecurityUtil.isSuperAdmin()) {
            return;
        }
        ProjectMember membership = findMembership(projectId, SecurityUtil.getCurrentUserId());
        if (membership == null) {
            throw BizException.forbidden("无权查看该项目模块");
        }
    }

    private void ensureProjectManager(Long projectId) {
        if (SecurityUtil.isSuperAdmin()) {
            return;
        }
        ProjectMember membership = findMembership(projectId, SecurityUtil.getCurrentUserId());
        if (!isProjectManager(membership)) {
            throw BizException.forbidden("只有项目管理员才能维护模块");
        }
    }

    private ProjectMember findMembership(Long projectId, Long userId) {
        return projectMemberMapper.selectOne(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId));
    }

    private boolean isProjectManager(ProjectMember membership) {
        return membership != null && "PROJECT_ADMIN".equalsIgnoreCase(membership.getRole());
    }

    private void ensureModuleBelongsToProject(PrmModule module, Long projectId) {
        if (!Objects.equals(module.getProjectId(), projectId)) {
            throw BizException.forbidden("模块不属于当前项目");
        }
    }

    private ModuleDTO toDTO(PrmModule m) {
        ModuleDTO dto = new ModuleDTO();
        dto.setId(m.getId());
        dto.setProjectId(m.getProjectId());
        dto.setParentId(m.getParentId());
        dto.setName(m.getName());
        dto.setSortOrder(m.getSortOrder());
        return dto;
    }
}
