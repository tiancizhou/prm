package com.prm.module.system.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.prm.common.exception.BizException;
import com.prm.module.system.dto.ModulePermissionDTO;
import com.prm.module.system.dto.SaveRoleModulePermissionsRequest;
import com.prm.module.system.entity.SysPermission;
import com.prm.module.system.entity.SysRole;
import com.prm.module.system.entity.SysRolePermission;
import com.prm.module.system.mapper.SysPermissionMapper;
import com.prm.module.system.mapper.SysRoleMapper;
import com.prm.module.system.mapper.SysRolePermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleModulePermissionService {

    private static final String MODULE_TYPE = "MODULE";
    private static final String ACTION_TYPE = "ACTION";

    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysRoleMapper roleMapper;

    public List<ModulePermissionDTO> listModulePermissions() {
        List<SysPermission> permissions = loadAssignablePermissions();

        Map<Long, ModulePermissionDTO> modulesById = new LinkedHashMap<>();
        for (SysPermission permission : permissions) {
            if (MODULE_TYPE.equals(permission.getType())) {
                modulesById.put(permission.getId(), toModulePermissionDTO(permission));
            }
        }

        for (SysPermission permission : permissions) {
            if (!ACTION_TYPE.equals(permission.getType())) {
                continue;
            }
            ModulePermissionDTO parentModule = modulesById.get(permission.getParentId());
            if (parentModule != null) {
                parentModule.getChildren().add(toModulePermissionDTO(permission));
            }
        }

        return new ArrayList<>(modulesById.values());
    }

    public List<String> getRoleModulePermissionCodes(Long roleId) {
        requireRole(roleId);
        Map<Long, SysPermission> assignablePermissionMap = loadAssignablePermissionMap();
        if (assignablePermissionMap.isEmpty()) {
            return List.of();
        }

        Set<Long> assignedIds = rolePermissionMapper.selectList(
                        new LambdaQueryWrapper<SysRolePermission>()
                                .eq(SysRolePermission::getRoleId, roleId)
                                .in(SysRolePermission::getPermissionId, assignablePermissionMap.keySet())
                ).stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return assignablePermissionMap.values().stream()
                .filter(permission -> assignedIds.contains(permission.getId()))
                .map(SysPermission::getCode)
                .toList();
    }

    @Transactional
    public List<String> saveRoleModulePermissions(Long roleId, SaveRoleModulePermissionsRequest request) {
        requireRole(roleId);

        Map<Long, SysPermission> assignablePermissionMap = loadAssignablePermissionMap();
        Map<String, SysPermission> permissionByCode = assignablePermissionMap.values().stream()
                .collect(Collectors.toMap(SysPermission::getCode, item -> item, (left, right) -> left, LinkedHashMap::new));
        Map<Long, SysPermission> permissionById = new LinkedHashMap<>(assignablePermissionMap);

        List<String> requestedCodes = request.getPermissionCodes() == null
                ? List.of()
                : request.getPermissionCodes().stream().distinct().toList();

        for (String code : requestedCodes) {
            if (!permissionByCode.containsKey(code)) {
                throw BizException.of("存在无效的权限编码: " + code);
            }
        }

        List<String> normalizedCodes = normalizeRequestedCodes(requestedCodes, permissionByCode, permissionById);

        if (!assignablePermissionMap.isEmpty()) {
            rolePermissionMapper.delete(
                    new LambdaQueryWrapper<SysRolePermission>()
                            .eq(SysRolePermission::getRoleId, roleId)
                            .in(SysRolePermission::getPermissionId, assignablePermissionMap.keySet())
            );
        }

        for (String code : normalizedCodes) {
            SysPermission permission = permissionByCode.get(code);
            SysRolePermission rolePermission = new SysRolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permission.getId());
            rolePermissionMapper.insert(rolePermission);
        }

        return normalizedCodes;
    }

    private List<SysPermission> loadAssignablePermissions() {
        return permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getDeleted, 0)
                        .in(SysPermission::getType, List.of(MODULE_TYPE, ACTION_TYPE))
                        .orderByAsc(SysPermission::getSort, SysPermission::getId)
        );
    }

    private Map<Long, SysPermission> loadAssignablePermissionMap() {
        return loadAssignablePermissions().stream()
                .collect(Collectors.toMap(SysPermission::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
    }

    private List<String> normalizeRequestedCodes(List<String> requestedCodes,
                                                 Map<String, SysPermission> permissionByCode,
                                                 Map<Long, SysPermission> permissionById) {
        LinkedHashSet<String> normalizedCodes = new LinkedHashSet<>();

        for (String code : requestedCodes) {
            SysPermission permission = permissionByCode.get(code);
            if (permission == null) {
                continue;
            }

            if (ACTION_TYPE.equals(permission.getType()) && permission.getParentId() != null) {
                SysPermission parentPermission = permissionById.get(permission.getParentId());
                if (parentPermission != null) {
                    normalizedCodes.add(parentPermission.getCode());
                }
            }

            normalizedCodes.add(permission.getCode());
        }

        return new ArrayList<>(normalizedCodes);
    }

    private SysRole requireRole(Long roleId) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null || role.getDeleted() == 1) {
            throw BizException.notFound("角色分组");
        }
        return role;
    }

    private ModulePermissionDTO toModulePermissionDTO(SysPermission permission) {
        ModulePermissionDTO dto = new ModulePermissionDTO();
        dto.setId(permission.getId());
        dto.setName(permission.getName());
        dto.setCode(permission.getCode());
        dto.setSort(permission.getSort());
        return dto;
    }
}
