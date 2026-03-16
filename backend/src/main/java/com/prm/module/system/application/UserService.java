package com.prm.module.system.application;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prm.common.exception.BizException;
import com.prm.common.util.SecurityUtil;
import com.prm.module.project.entity.Project;
import com.prm.module.project.entity.ProjectMember;
import com.prm.module.project.mapper.ProjectMapper;
import com.prm.module.project.mapper.ProjectMemberMapper;
import com.prm.module.system.dto.CreateUserRequest;
import com.prm.module.system.dto.UpdateUserRequest;
import com.prm.module.system.dto.UserDTO;
import com.prm.module.system.dto.UserJoinedProjectDTO;
import com.prm.module.system.dto.UserProjectScopeDTO;
import com.prm.module.system.entity.SysDepartment;
import com.prm.module.system.entity.SysRole;
import com.prm.module.system.entity.SysUser;
import com.prm.module.system.entity.SysUserRole;
import com.prm.module.system.mapper.SysDepartmentMapper;
import com.prm.module.system.mapper.SysRoleMapper;
import com.prm.module.system.mapper.SysUserMapper;
import com.prm.module.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysDepartmentMapper departmentMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final ProjectMapper projectMapper;

    public List<SysRole> listAllRoles() {
        return roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getDeleted, 0).orderByAsc(SysRole::getId));
    }

    public IPage<UserDTO> page(int pageNum, int pageSize, String keyword, Long departmentId) {
        Page<SysUser> pageReq = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0)
                .orderByDesc(SysUser::getCreatedAt);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getEmployeeNo, keyword));
        }
        if (departmentId != null) {
            wrapper.in(SysUser::getDepartmentId, resolveDepartmentScopeIds(departmentId));
        }

        IPage<SysUser> userPage = userMapper.selectPage(pageReq, wrapper);
        Map<Long, Long> joinedProjectCountMap = loadJoinedProjectCountMap(
                userPage.getRecords().stream().map(SysUser::getId).toList());
        return userPage.convert(user -> toDTO(user, joinedProjectCountMap.getOrDefault(user.getId(), 0L)));
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
        applyDepartment(user, request.getDepartmentId(), request.getDepartment());
        user.setTeam(request.getTeam());
        user.setExternalId(request.getExternalId());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus("ACTIVE");
        user.setDeleted(0);
        userMapper.insert(user);
        setUserRoles(user.getId(), request.getRoleIds());
        return toDTO(user, 0L);
    }

    public UserDTO getById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw BizException.notFound("用户");
        }
        return toDTO(user, resolveJoinedProjectCount(id));
    }

    public UserProjectScopeDTO getProjectScope(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw BizException.notFound("用户");
        }

        UserProjectScopeDTO dto = new UserProjectScopeDTO();
        dto.setUserId(user.getId());
        dto.setDisplayName(StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getNickname());
        dto.setUsername(user.getUsername());
        dto.setEmployeeNo(user.getEmployeeNo());
        dto.setRoles(userMapper.selectRoleCodesByUserId(user.getId()));

        List<ProjectMember> memberships = projectMemberMapper.selectList(
                new LambdaQueryWrapper<ProjectMember>()
                        .eq(ProjectMember::getUserId, user.getId())
                        .orderByDesc(ProjectMember::getCreatedAt)
        );
        if (memberships.isEmpty()) {
            dto.setJoinedProjects(List.of());
            return dto;
        }

        Map<Long, Project> projectMap = projectMapper.selectBatchIds(
                        memberships.stream().map(ProjectMember::getProjectId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Project::getId, project -> project));

        List<UserJoinedProjectDTO> joinedProjects = new ArrayList<>();
        for (ProjectMember membership : memberships) {
            Project project = projectMap.get(membership.getProjectId());
            if (project == null) {
                continue;
            }
            UserJoinedProjectDTO joinedProject = new UserJoinedProjectDTO();
            joinedProject.setId(project.getId());
            joinedProject.setName(project.getName());
            joinedProject.setCode(project.getCode());
            joinedProjects.add(joinedProject);
        }
        dto.setJoinedProjects(joinedProjects);
        return dto;
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
        applyDepartment(user, request.getDepartmentId(), request.getDepartment());
        user.setTeam(request.getTeam());
        user.setExternalId(request.getExternalId());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        userMapper.updateById(user);
        setUserRoles(id, request.getRoleIds());
        return toDTO(userMapper.selectById(id), resolveJoinedProjectCount(id));
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

    @Transactional
    public void delete(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw BizException.notFound("用户");
        }

        Long currentUserId = SecurityUtil.getCurrentUserIdOrNull();
        if (currentUserId != null && currentUserId.equals(id)) {
            throw BizException.of("不能删除当前登录用户");
        }

        SysUser update = new SysUser();
        update.setId(id);
        update.setStatus("DISABLED");
        update.setDeleted(1);
        userMapper.updateById(update);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
    }

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

    private void applyDepartment(SysUser user, Long departmentId, String legacyDepartmentName) {
        if (departmentId != null) {
            SysDepartment department = departmentMapper.selectById(departmentId);
            if (department == null || department.getDeleted() == 1) {
                throw BizException.notFound("部门");
            }
            user.setDepartmentId(department.getId());
            user.setDepartment(department.getName());
            return;
        }

        if (StringUtils.hasText(legacyDepartmentName)) {
            String trimmed = legacyDepartmentName.trim();
            SysDepartment department = departmentMapper.selectOne(
                    new LambdaQueryWrapper<SysDepartment>()
                            .eq(SysDepartment::getDeleted, 0)
                            .eq(SysDepartment::getName, trimmed)
                            .last("LIMIT 1")
            );
            user.setDepartmentId(department == null ? null : department.getId());
            user.setDepartment(trimmed);
            return;
        }

        user.setDepartmentId(null);
        user.setDepartment(null);
    }

    private List<Long> resolveDepartmentScopeIds(Long departmentId) {
        List<SysDepartment> departments = departmentMapper.selectList(
                new LambdaQueryWrapper<SysDepartment>()
                        .eq(SysDepartment::getDeleted, 0)
        );
        Map<Long, List<Long>> childrenByParentId = departments.stream()
                .filter(item -> item.getParentId() != null)
                .collect(Collectors.groupingBy(SysDepartment::getParentId,
                        Collectors.mapping(SysDepartment::getId, Collectors.toList())));

        Set<Long> scopedDepartmentIds = new LinkedHashSet<>();
        Deque<Long> pendingDepartmentIds = new ArrayDeque<>();
        pendingDepartmentIds.add(departmentId);

        while (!pendingDepartmentIds.isEmpty()) {
            Long currentDepartmentId = pendingDepartmentIds.removeFirst();
            if (!scopedDepartmentIds.add(currentDepartmentId)) {
                continue;
            }
            for (Long childDepartmentId : childrenByParentId.getOrDefault(currentDepartmentId, List.of())) {
                pendingDepartmentIds.addLast(childDepartmentId);
            }
        }

        return new ArrayList<>(scopedDepartmentIds);
    }

    private String resolveDepartmentName(SysUser user) {
        if (user.getDepartmentId() != null) {
            SysDepartment department = departmentMapper.selectById(user.getDepartmentId());
            if (department != null && department.getDeleted() == 0) {
                return department.getName();
            }
        }
        return user.getDepartment();
    }

    /**
     * 解析部门完整路径（从根到当前部门），如：一级部门 / 二级部门 / 三级部门
     */
    private String resolveDepartmentPath(SysUser user) {
        if (user.getDepartmentId() == null) {
            return user.getDepartment();
        }
        List<String> names = new ArrayList<>();
        Long currentId = user.getDepartmentId();
        while (currentId != null) {
            SysDepartment dept = departmentMapper.selectById(currentId);
            if (dept == null || dept.getDeleted() == 1) {
                break;
            }
            names.add(dept.getName());
            currentId = dept.getParentId();
        }
        Collections.reverse(names);
        return names.isEmpty() ? user.getDepartment() : String.join(" / ", names);
    }

    private Map<Long, Long> loadJoinedProjectCountMap(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Map.of();
        }
        return projectMemberMapper.selectList(
                        new LambdaQueryWrapper<ProjectMember>().in(ProjectMember::getUserId, userIds))
                .stream()
                .collect(Collectors.groupingBy(ProjectMember::getUserId, Collectors.counting()));
    }

    private Long resolveJoinedProjectCount(Long userId) {
        if (userId == null) {
            return 0L;
        }
        return projectMemberMapper.selectCount(
                new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getUserId, userId));
    }

    private UserDTO toDTO(SysUser user, Long joinedProjectCount) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setRealName(user.getRealName());
        dto.setEmployeeNo(user.getEmployeeNo());
        dto.setDepartmentId(user.getDepartmentId());
        dto.setDepartment(resolveDepartmentName(user));
        dto.setDepartmentPath(resolveDepartmentPath(user));
        dto.setTeam(user.getTeam());
        dto.setExternalId(user.getExternalId());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAvatar(user.getAvatar());
        dto.setStatus(user.getStatus());
        dto.setJoinedProjectCount(joinedProjectCount == null ? 0L : joinedProjectCount);
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
