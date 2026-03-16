package com.prm.module.module;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.prm.common.exception.BizException;
import com.prm.common.util.SecurityUtil;
import com.prm.module.module.application.ModuleService;
import com.prm.module.module.dto.SaveModuleRequest;
import com.prm.module.module.entity.PrmModule;
import com.prm.module.module.mapper.ModuleMapper;
import com.prm.module.project.entity.ProjectMember;
import com.prm.module.project.mapper.ProjectMemberMapper;
import com.prm.module.requirement.mapper.RequirementMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModuleServicePermissionTests {

    @Mock
    private ModuleMapper moduleMapper;
    @Mock
    private RequirementMapper requirementMapper;
    @Mock
    private ProjectMemberMapper projectMemberMapper;

    private ModuleService moduleService;

    @BeforeEach
    void setUp() {
        moduleService = new ModuleService(moduleMapper, requirementMapper);
    }

    @Test
    void listTreeShouldDenyNonMemberEvenWhenGlobalManagerFlagIsTrue() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);
            securityUtil.when(SecurityUtil::isManager).thenReturn(true);
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            assertThatThrownBy(() -> moduleService.listTree(1001L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("查看");

            verify(moduleMapper, never()).selectList(any(LambdaQueryWrapper.class));
        }
    }

    @Test
    void createShouldDenyGlobalManagerOutsideProject() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);
            securityUtil.when(SecurityUtil::isManager).thenReturn(true);
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            assertThatThrownBy(() -> moduleService.create(1001L, request("Core")))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("管理员");

            verify(moduleMapper, never()).insert(any(PrmModule.class));
        }
    }

    @Test
    void createShouldAllowProjectManagerInProject() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(3001L);
            securityUtil.when(SecurityUtil::isManager).thenReturn(false);
            securityUtil.when(() -> SecurityUtil.hasRole("PROJECT_ADMIN")).thenReturn(true);
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 3001L, "MEMBER"));

            assertThatCode(() -> moduleService.create(1001L, request("Core"))).doesNotThrowAnyException();

            verify(moduleMapper).insert(any(PrmModule.class));
        }
    }

    @Test
    void updateShouldDenyCrossProjectModuleMutation() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(3001L);
            securityUtil.when(() -> SecurityUtil.hasRole("PROJECT_ADMIN")).thenReturn(true);
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 3001L, "MEMBER"));
            when(moduleMapper.selectById(9001L)).thenReturn(module(9001L, 1002L, "Payments"));

            assertThatThrownBy(() -> moduleService.update(1001L, 9001L, request("Core")))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("当前项目");
        }
    }


    private SaveModuleRequest request(String name) {
        SaveModuleRequest request = new SaveModuleRequest();
        request.setName(name);
        return request;
    }

    private ProjectMember member(Long projectId, Long userId, String role) {
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRole(role);
        return member;
    }

    private PrmModule module(Long id, Long projectId, String name) {
        PrmModule module = new PrmModule();
        module.setId(id);
        module.setProjectId(projectId);
        module.setName(name);
        module.setDeleted(0);
        return module;
    }
}
