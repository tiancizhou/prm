package com.prm.module.system.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.prm.common.exception.BizException;
import com.prm.module.system.dto.RoleGroupDetailDTO;
import com.prm.module.system.dto.RoleGroupMemberDTO;
import com.prm.module.system.dto.RoleGroupRowDTO;
import com.prm.module.system.dto.SaveRoleGroupRequest;
import com.prm.module.system.entity.SysRole;
import com.prm.module.system.entity.SysUser;
import com.prm.module.system.entity.SysUserRole;
import com.prm.module.system.mapper.SysRoleMapper;
import com.prm.module.system.mapper.SysUserMapper;
import com.prm.module.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleAdminService {

    private final SysRoleMapper roleMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;

    public List<RoleGroupRowDTO> listRoleGroups() {
        return roleMapper.selectList(
                        new LambdaQueryWrapper<SysRole>()
                                .eq(SysRole::getDeleted, 0)
                                .orderByAsc(SysRole::getId)
                ).stream()
                .map(this::toRowDTO)
                .toList();
    }

    public RoleGroupDetailDTO getRoleGroup(Long id) {
        SysRole role = requireRole(id);
        return toDetailDTO(role);
    }

    @Transactional
    public RoleGroupDetailDTO create(SaveRoleGroupRequest request) {
        validateRequest(request, null);

        SysRole role = new SysRole();
        role.setName(request.getName().trim());
        role.setCode(request.getCode().trim());
        role.setTagType(normalizeTagType(request.getTagType()));
        role.setDescription(normalize(request.getDescription()));
        role.setDeleted(0);
        roleMapper.insert(role);
        return toDetailDTO(roleMapper.selectById(role.getId()));
    }

    @Transactional
    public RoleGroupDetailDTO update(Long id, SaveRoleGroupRequest request) {
        SysRole role = requireRole(id);
        validateRequest(request, id);

        role.setName(request.getName().trim());
        role.setCode(request.getCode().trim());
        role.setTagType(normalizeTagType(request.getTagType()));
        role.setDescription(normalize(request.getDescription()));
        roleMapper.updateById(role);
        return toDetailDTO(roleMapper.selectById(id));
    }

    @Transactional
    public void delete(Long id) {
        requireRole(id);

        long memberCount = userRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, id)
        );
        if (memberCount > 0) {
            throw BizException.of("该分组下仍有用户，无法删除");
        }

        roleMapper.deleteById(id);
    }

    private RoleGroupRowDTO toRowDTO(SysRole role) {
        List<RoleGroupMemberDTO> members = loadMembers(role.getId());

        RoleGroupRowDTO dto = new RoleGroupRowDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setCode(role.getCode());
        dto.setTagType(role.getTagType() != null ? role.getTagType() : "info");
        dto.setDescription(resolveDescription(role));
        dto.setUsers(members.stream().map(RoleGroupMemberDTO::getDisplayName).collect(Collectors.joining(", ")));
        dto.setMemberCount((long) members.size());
        return dto;
    }

    private RoleGroupDetailDTO toDetailDTO(SysRole role) {
        RoleGroupDetailDTO dto = new RoleGroupDetailDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setCode(role.getCode());
        dto.setTagType(role.getTagType() != null ? role.getTagType() : "info");
        dto.setDescription(resolveDescription(role));
        dto.setMembers(loadMembers(role.getId()));
        return dto;
    }

    private List<RoleGroupMemberDTO> loadMembers(Long roleId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, roleId)
                        .orderByAsc(SysUserRole::getId)
        );
        if (userRoles.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = userRoles.stream().map(SysUserRole::getUserId).distinct().toList();
        List<SysUser> users = userMapper.selectBatchIds(userIds);
        Map<Long, SysUser> userMap = new LinkedHashMap<>();
        for (SysUser user : users) {
            if (user != null && user.getDeleted() != null && user.getDeleted() == 0) {
                userMap.put(user.getId(), user);
            }
        }

        List<RoleGroupMemberDTO> members = new ArrayList<>();
        for (Long userId : userIds) {
            SysUser user = userMap.get(userId);
            if (user == null) {
                continue;
            }
            RoleGroupMemberDTO dto = new RoleGroupMemberDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setRealName(user.getRealName());
            dto.setDisplayName(StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername());
            members.add(dto);
        }
        return members;
    }

    private void validateRequest(SaveRoleGroupRequest request, Long currentId) {
        if (!StringUtils.hasText(request.getName())) {
            throw BizException.of("角色名称不能为空");
        }
        if (!StringUtils.hasText(request.getCode())) {
            throw BizException.of("角色编码不能为空");
        }

        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getDeleted, 0)
                .eq(SysRole::getCode, request.getCode().trim());
        if (currentId != null) {
            wrapper.ne(SysRole::getId, currentId);
        }

        if (roleMapper.selectCount(wrapper) > 0) {
            throw BizException.of("角色编码已存在");
        }
    }

    private SysRole requireRole(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null || role.getDeleted() == 1) {
            throw BizException.notFound("角色分组");
        }
        return role;
    }

    private String resolveDescription(SysRole role) {
        return StringUtils.hasText(role.getDescription()) ? role.getDescription() : role.getName();
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private static final List<String> ALLOWED_TAG_TYPES = List.of("danger", "warning", "primary", "success", "info");

    private String normalizeTagType(String value) {
        if (!StringUtils.hasText(value)) {
            return "info";
        }
        String v = value.trim().toLowerCase();
        return ALLOWED_TAG_TYPES.contains(v) ? v : "info";
    }
}
