package com.prm.module.module.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.prm.common.exception.BizException;
import com.prm.common.util.SecurityUtil;
import com.prm.module.module.dto.ModuleDTO;
import com.prm.module.module.dto.SaveModuleRequest;
import com.prm.module.module.entity.PrmModule;
import com.prm.module.module.mapper.ModuleMapper;
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

    public List<ModuleDTO> listTree(Long projectId) {
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
        if (!SecurityUtil.isManager()) {
            throw BizException.forbidden("只有项目管理员才能创建模块");
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

    public ModuleDTO update(Long id, long l, SaveModuleRequest req) {
        if (!SecurityUtil.isManager()) {
            throw BizException.forbidden("只有项目管理员才能修改模块");
        }
        PrmModule m = moduleMapper.selectById(id);
        if (m == null || m.getDeleted() == 1) {
            throw BizException.notFound("模块");
        }
        m.setName(req.getName());
        if (req.getSortOrder() != null) {
            m.setSortOrder(req.getSortOrder());
        }
        m.setUpdatedBy(SecurityUtil.getCurrentUserId());
        m.setUpdatedAt(LocalDateTime.now());
        moduleMapper.updateById(m);
        return toDTO(m);
    }

    public void delete(Long id) {
        if (!SecurityUtil.isManager()) {
            throw BizException.forbidden("只有项目管理员才能删除模块");
        }
        PrmModule m = moduleMapper.selectById(id);
        if (m == null || m.getDeleted() == 1) {
            throw BizException.notFound("模块");
        }
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
