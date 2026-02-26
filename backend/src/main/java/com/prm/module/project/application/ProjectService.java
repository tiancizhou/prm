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

    // ─── 权限辅助 ─────────────────────────────────────────────

    /** 当前用户是否是超级管理员 */
    private boolean isSuperAdmin() {
        return SecurityUtil.isSuperAdmin();
    }

    /** 当前用户是否是项目经理（系统角色） */
    private boolean isProjectAdmin() {
        return SecurityUtil.hasRole("PROJECT_ADMIN");
    }

    /** 当前用户是否是指定项目的成员 */
    private boolean isMemberOf(Long projectId) {
        Long userId = SecurityUtil.getCurrentUserId();
        return memberMapper.selectCount(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId)) > 0;
    }

    /**
     * 检查当前用户是否有项目编辑权限：
     *   - SUPER_ADMIN：可编辑所有项目
     *   - PROJECT_ADMIN：仅可编辑自己是成员的项目
     *   - 其他：无编辑权
     */
    private void requireEditPermission(Long projectId) {
        if (isSuperAdmin()) return;
        if (isProjectAdmin() && isMemberOf(projectId)) return;
        throw BizException.forbidden("无项目编辑权限");
    }

    /**
     * 检查当前用户是否有创建项目的权限：
     *   - SUPER_ADMIN 或 PROJECT_ADMIN 可创建
     */
    private void requireCreatePermission() {
        if (isSuperAdmin() || isProjectAdmin()) return;
        throw BizException.forbidden("只有项目经理或超级管理员才能创建项目");
    }

    // ─── 项目列表 ──────────────────────────────────────────────

    public IPage<ProjectDTO> page(int pageNum, int pageSize, String keyword, String status) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        boolean isAdmin = isSuperAdmin();

        // 非超管：只能看自己所在项目
        Set<Long> myProjectIds = null;
        if (!isAdmin) {
            myProjectIds = memberMapper.selectList(
                    new LambdaQueryWrapper<ProjectMember>()
                            .eq(ProjectMember::getUserId, currentUserId))
                    .stream().map(ProjectMember::getProjectId).collect(Collectors.toSet());
            if (myProjectIds.isEmpty()) {
                return new Page<ProjectDTO>(pageNum, pageSize).convert(p -> null);
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
            wrapper.and(w -> w.like(Project::getName, keyword).or().like(Project::getCode, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Project::getStatus, status);
        }

        IPage<Project> projectPage = projectMapper.selectPage(pageReq, wrapper);
        List<Long> ownerIds = projectPage.getRecords().stream()
                .map(Project::getOwnerId).distinct().collect(Collectors.toList());
        Map<Long, SysUser> userMap = ownerIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(ownerIds).stream()
                        .collect(Collectors.toMap(SysUser::getId, u -> u));

        Set<Long> editableIds = null;
        if (!isAdmin) {
            // 项目经理：自己所在的项目可编辑；其他角色：无编辑权
            if (isProjectAdmin()) {
                editableIds = myProjectIds;
            }
        }
        final Set<Long> finalEditableIds = editableIds;
        final boolean finalIsAdmin = isAdmin;

        return projectPage.convert(p -> {
            boolean canEdit = finalIsAdmin || (finalEditableIds != null && finalEditableIds.contains(p.getId()));
            return toDTO(p, userMap.get(p.getOwnerId()), canEdit);
        });
    }

    // ─── CRUD ─────────────────────────────────────────────────

    @Transactional
    public ProjectDTO create(CreateProjectRequest request) {
        requireCreatePermission();

        long count = projectMapper.selectCount(
                new LambdaQueryWrapper<Project>().eq(Project::getCode, request.getCode()));
        if (count > 0) throw BizException.of("项目代号已存在");

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
        if (project == null || project.getDeleted() == 1) throw BizException.notFound("项目");
        boolean canEdit = isSuperAdmin() || (isProjectAdmin() && isMemberOf(id));
        SysUser owner = userMapper.selectById(project.getOwnerId());
        return toDTO(project, owner, canEdit);
    }

    @Transactional
    public ProjectDTO update(Long id, CreateProjectRequest request) {
        requireEditPermission(id);
        Project project = projectMapper.selectById(id);
        if (project == null || project.getDeleted() == 1) throw BizException.notFound("项目");
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
        if (project == null) throw BizException.notFound("项目");
        project.setStatus(status);
        project.setUpdatedBy(SecurityUtil.getCurrentUserId());
        projectMapper.updateById(project);
    }

    // ─── 成员管理 ──────────────────────────────────────────────

    public List<ProjectMember> getMembers(Long projectId) {
        return memberMapper.selectList(
                new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getProjectId, projectId));
    }

    /** 项目成员列表（含用户姓名、工号、系统角色名），用于指派下拉和成员页展示 */
    public List<ProjectMemberVO> getMemberVOs(Long projectId) {
        List<ProjectMember> members = memberMapper.selectList(
                new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getProjectId, projectId));
        if (members.isEmpty()) return List.of();

        List<Long> userIds = members.stream().map(ProjectMember::getUserId).distinct().toList();
        Map<Long, SysUser> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));

        Map<String, String> sysRoleNameMap = roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getDeleted, 0))
                .stream().collect(Collectors.toMap(SysRole::getCode, SysRole::getName));

        List<ProjectMemberVO> result = new ArrayList<>();
        for (ProjectMember m : members) {
            ProjectMemberVO vo = new ProjectMemberVO();
            vo.setUserId(m.getUserId());
            SysUser u = userMap.get(m.getUserId());
            if (u != null) {
                vo.setNickname(StringUtils.hasText(u.getRealName()) ? u.getRealName() : u.getNickname());
                vo.setUsername(u.getUsername());
                vo.setEmployeeNo(u.getEmployeeNo());
                List<String> roleCodes = userMapper.selectRoleCodesByUserId(u.getId());
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
        if (exists > 0) throw BizException.of("成员已存在");
        ProjectMember m = new ProjectMember();
        m.setProjectId(projectId);
        m.setUserId(userId);
        m.setRole("MEMBER");
        m.setCreatedAt(LocalDateTime.now());
        memberMapper.insert(m);
    }

    @Transactional
    public void removeMember(Long projectId, Long userId) {
        requireEditPermission(projectId);
        memberMapper.delete(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId));
    }

    // ─── DTO 转换 ──────────────────────────────────────────────

    private ProjectDTO toDTO(Project p, SysUser owner, boolean canEdit) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setCode(p.getCode());
        dto.setDescription(p.getDescription());
        dto.setStatus(p.getStatus());
        dto.setVisibility(p.getVisibility());
        dto.setOwnerId(p.getOwnerId());
        dto.setOwnerName(owner != null ? (StringUtils.hasText(owner.getRealName()) ? owner.getRealName() : owner.getNickname()) : null);
        dto.setStartDate(p.getStartDate());
        dto.setEndDate(p.getEndDate());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setCanEdit(canEdit);
        return dto;
    }
}
