package com.prm.module.system.application;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prm.common.exception.BizException;
import com.prm.module.system.dto.CreateUserRequest;
import com.prm.module.system.dto.UpdateUserRequest;
import com.prm.module.system.dto.UserDTO;
import com.prm.module.system.entity.SysRole;
import com.prm.module.system.entity.SysUser;
import com.prm.module.system.entity.SysUserRole;
import com.prm.module.system.mapper.SysRoleMapper;
import com.prm.module.system.mapper.SysUserMapper;
import com.prm.module.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;

    public List<SysRole> listAllRoles() {
        return roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getDeleted, 0).orderByAsc(SysRole::getId));
    }

    public IPage<UserDTO> page(int pageNum, int pageSize, String keyword) {
        Page<SysUser> pageReq = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0)
                .orderByDesc(SysUser::getCreatedAt);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getEmployeeNo, keyword));
        }
        IPage<SysUser> userPage = userMapper.selectPage(pageReq, wrapper);
        return userPage.convert(this::toDTO);
    }

    @Transactional
    public UserDTO create(CreateUserRequest request) {
        long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (count > 0) {
            throw BizException.of("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(BCrypt.hashpw(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setNickname(StringUtils.hasText(request.getRealName()) ? request.getRealName() : request.getUsername());
        user.setEmployeeNo(request.getEmployeeNo());
        user.setDepartment(request.getDepartment());
        user.setTeam(request.getTeam());
        user.setExternalId(request.getExternalId());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus("ACTIVE");
        user.setDeleted(0);
        userMapper.insert(user);
        setUserRoles(user.getId(), request.getRoleIds());
        return toDTO(user);
    }

    public UserDTO getById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw BizException.notFound("用户");
        }
        return toDTO(user);
    }

    @Transactional
    public UserDTO update(Long id, UpdateUserRequest request) {
        SysUser user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw BizException.notFound("用户");
        }
        user.setRealName(request.getRealName());
        user.setNickname(request.getRealName());
        user.setEmployeeNo(request.getEmployeeNo());
        user.setDepartment(request.getDepartment());
        user.setTeam(request.getTeam());
        user.setExternalId(request.getExternalId());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        userMapper.updateById(user);
        setUserRoles(id, request.getRoleIds());
        return toDTO(userMapper.selectById(id));
    }

    @Transactional
    public void changeStatus(Long id, String status) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw BizException.notFound("用户");
        SysUser update = new SysUser();
        update.setId(id);
        update.setStatus(status);
        userMapper.updateById(update);
    }

    /** 覆盖式设置用户角色（为 null 时跳过，不清空） */
    private void setUserRoles(Long userId, List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) return;
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        for (Long roleId : roleIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
    }

    @Transactional
    public void assignRole(Long userId, Long roleId) {
        long exists = userRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
                        .eq(SysUserRole::getRoleId, roleId));
        if (exists == 0) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(BCrypt.hashpw(newPassword));
        userMapper.updateById(user);
    }

    private UserDTO toDTO(SysUser user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setRealName(user.getRealName());
        dto.setEmployeeNo(user.getEmployeeNo());
        dto.setDepartment(user.getDepartment());
        dto.setTeam(user.getTeam());
        dto.setExternalId(user.getExternalId());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAvatar(user.getAvatar());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        if (user.getId() != null) {
            List<String> roles = userMapper.selectRoleCodesByUserId(user.getId());
            dto.setRoles(roles);
            List<Long> roleIds = userRoleMapper.selectList(
                    new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId()))
                    .stream().map(SysUserRole::getRoleId).toList();
            dto.setRoleIds(roleIds);
        }
        return dto;
    }
}
