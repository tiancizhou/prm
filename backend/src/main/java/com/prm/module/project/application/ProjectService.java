package com.prm.module.project.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prm.common.exception.BizException;
import com.prm.common.util.SecurityUtil;
import com.prm.module.project.dto.CreateProjectRequest;
import com.prm.module.project.dto.ProjectDTO;
import com.prm.module.project.dto.ProjectMemberVO;
import com.prm.module.project.entity.Project;
import com.prm.module.project.entity.ProjectMember;
import com.prm.module.project.mapper.ProjectMapper;
import com.prm.module.project.mapper.ProjectMemberMapper;
import com.prm.module.system.entity.SysRole;
import com.prm.module.system.entity.SysUser;
import com.prm.module.system.mapper.SysRoleMapper;
import com.prm.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper memberMapper;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;

    private boolean isSuperAdmin() {
        return SecurityUtil.isSuperAdmin();
    }

    private boolean hasProjectAdminRole() {
        return SecurityUtil.hasRole("PROJECT_ADMIN");
    }

    private ProjectMember findMyMembership(Long projectId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        return memberMapper.selectOne(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, currentUserId));
    }

    private boolean isProjectManagerOf(Long projectId) {
        ProjectMember membership = findMyMembership(projectId);
        return membership != null && "PROJECT_ADMIN".equalsIgnoreCase(membership.getRole());
    }

    private void requireEditPermission(Long projectId) {
        if (isSuperAdmin() || isProjectManagerOf(projectId)) {
            return;
        }
        throw BizException.forbidden("无项目编辑权限");
    }

    private void requireCreatePermission() {
        if (isSuperAdmin() || hasProjectAdminRole()) {
            return;
        }
        throw BizException.forbidden("只有项目经理或超级管理员才能创建项目");
    }

    public IPage<ProjectDTO> page(int pageNum, int pageSize, String keyword, String status) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        boolean isAdmin = isSuperAdmin();

        Set<Long> myProjectIds = null;
        Set<Long> managerProjectIds = null;
        if (!isAdmin) {
            List<ProjectMember> memberships = memberMapper.selectList(
                    new LambdaQueryWrapper<ProjectMember>()
                            .eq(ProjectMember::getUserId, currentUserId));
            myProjectIds = memberships.stream()
                    .map(ProjectMember::getProjectId)
                    .collect(Collectors.toCollection(HashSet::new));
            managerProjectIds = memberships.stream()
                    .filter(member -> "PROJECT_ADMIN".equalsIgnoreCase(member.getRole()))
                    .map(ProjectMember::getProjectId)
                    .collect(Collectors.toCollection(HashSet::new));

            if (myProjectIds.isEmpty()) {
                return new Page<ProjectDTO>(pageNum, pageSize).convert(item -> null);
            }
        }

        Page<Project> pageReq = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<Project>()
                .eq(Project::getDeleted, 0)
                .orderByDesc(Project::getCreatedAt);
        if (!isAdmin) {
            wrapper.in(Project::getId, myProjectIds);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(condition -> condition
                    .like(Project::getName, keyword)
                    .or()
                    .like(Project::getCode, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Project::getStatus, status);
        }

        IPage<Project> projectPage = projectMapper.selectPage(pageReq, wrapper);
        List<Long> ownerIds = projectPage.getRecords().stream()
                .map(Project::getOwnerId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, SysUser> userMap = ownerIds.isEmpty()
                ? Map.of()
                : userMapper.selectBatchIds(ownerIds).stream()
                .collect(Collectors.toMap(SysUser::getId, user -> user));

        Set<Long> editableIds = isAdmin ? null : managerProjectIds;
        final boolean finalIsAdmin = isAdmin;
        final Set<Long> finalEditableIds = editableIds;
        return projectPage.convert(project -> {
            boolean canEdit = finalIsAdmin
                    || (finalEditableIds != null && finalEditableIds.contains(project.getId()));
            return toDTO(project, userMap.get(project.getOwnerId()), canEdit);
        });
    }

    @Transactional
    public ProjectDTO create(CreateProjectRequest request) {
        requireCreatePermission();

        long count = projectMapper.selectCount(
                new LambdaQueryWrapper<Project>().eq(Project::getCode, request.getCode()));
        if (count > 0) {
            throw BizException.of("项目代号已存在");
        }

        Long currentUserId = SecurityUtil.getCurrentUserId();
        Project project = new Project();
        project.setName(request.getName());
        project.setCode(request.getCode().toUpperCase());
        project.setDescription(request.getDescription());
        project.setVisibility(request.getVisibility());
        project.setStatus("ACTIVE");
        project.setOwnerId(currentUserId);
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setDeleted(0);
        project.setCreatedBy(currentUserId);
        project.setUpdatedBy(currentUserId);
        projectMapper.insert(project);

        ProjectMember ownerMember = new ProjectMember();
        ownerMember.setProjectId(project.getId());
        ownerMember.setUserId(currentUserId);
        ownerMember.setRole("PROJECT_ADMIN");
        ownerMember.setCreatedAt(LocalDateTime.now());
        memberMapper.insert(ownerMember);

        SysUser owner = userMapper.selectById(currentUserId);
        return toDTO(project, owner, true);
    }

    public ProjectDTO getById(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null || project.getDeleted() == 1) {
            throw BizException.notFound("项目");
        }
        boolean canEdit = isSuperAdmin() || isProjectManagerOf(id);
        SysUser owner = userMapper.selectById(project.getOwnerId());
        return toDTO(project, owner, canEdit);
    }

    @Transactional
    public ProjectDTO update(Long id, CreateProjectRequest request) {
        requireEditPermission(id);
        Project project = projectMapper.selectById(id);
        if (project == null || project.getDeleted() == 1) {
            throw BizException.notFound("项目");
        }
        Long currentUserId = SecurityUtil.getCurrentUserId();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setVisibility(request.getVisibility());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setUpdatedBy(currentUserId);
        projectMapper.updateById(project);
        SysUser owner = userMapper.selectById(project.getOwnerId());
        return toDTO(project, owner, true);
    }

    @Transactional
    public void archive(Long id) {
        requireEditPermission(id);
        updateStatus(id, "ARCHIVED");
    }

    @Transactional
    public void close(Long id) {
        requireEditPermission(id);
        updateStatus(id, "CLOSED");
    }

    private void updateStatus(Long id, String status) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw BizException.notFound("项目");
        }
        project.setStatus(status);
        project.setUpdatedBy(SecurityUtil.getCurrentUserId());
        projectMapper.updateById(project);
    }

    public List<ProjectMember> getMembers(Long projectId) {
        return memberMapper.selectList(
                new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getProjectId, projectId));
    }

    public List<ProjectMemberVO> getMemberVOs(Long projectId) {
        List<ProjectMember> members = memberMapper.selectList(
                new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getProjectId, projectId));
        if (members.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = members.stream().map(ProjectMember::getUserId).distinct().toList();
        Map<Long, SysUser> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, user -> user));

        Map<String, String> sysRoleNameMap = roleMapper.selectList(
                        new LambdaQueryWrapper<SysRole>().eq(SysRole::getDeleted, 0))
                .stream()
                .collect(Collectors.toMap(SysRole::getCode, SysRole::getName));

        List<ProjectMemberVO> result = new ArrayList<>();
        for (ProjectMember member : members) {
            ProjectMemberVO vo = new ProjectMemberVO();
            vo.setUserId(member.getUserId());

            SysUser user = userMap.get(member.getUserId());
            if (user != null) {
                vo.setNickname(StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getNickname());
                vo.setUsername(user.getUsername());
                vo.setEmployeeNo(user.getEmployeeNo());

                List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getId());
                if (!roleCodes.isEmpty()) {
                    vo.setRole(roleCodes.get(0));
                    vo.setRoleName(sysRoleNameMap.getOrDefault(roleCodes.get(0), roleCodes.get(0)));
                }
            }
            result.add(vo);
        }
        return result;
    }

    @Transactional
    public void addMember(Long projectId, Long userId) {
        requireEditPermission(projectId);
        long exists = memberMapper.selectCount(
                new LambdaQueryWrapper<ProjectMember>()
                        .eq(ProjectMember::getProjectId, projectId)
                        .eq(ProjectMember::getUserId, userId));
        if (exists > 0) {
            throw BizException.of("成员已存在");
        }

        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRole("MEMBER");
        member.setCreatedAt(LocalDateTime.now());
        memberMapper.insert(member);
    }

    @Transactional
    public void removeMember(Long projectId, Long userId) {
        requireEditPermission(projectId);
        memberMapper.delete(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId));
    }

    private ProjectDTO toDTO(Project project, SysUser owner, boolean canEdit) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setCode(project.getCode());
        dto.setDescription(project.getDescription());
        dto.setStatus(project.getStatus());
        dto.setVisibility(project.getVisibility());
        dto.setOwnerId(project.getOwnerId());
        dto.setOwnerName(owner != null
                ? (StringUtils.hasText(owner.getRealName()) ? owner.getRealName() : owner.getNickname())
                : null);
        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setCanEdit(canEdit);
        return dto;
    }
}
