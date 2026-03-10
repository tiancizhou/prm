package com.prm.module.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prm.common.exception.BizException;
import com.prm.common.util.SecurityUtil;
import com.prm.module.project.entity.ProjectMember;
import com.prm.module.project.mapper.ProjectMemberMapper;
import com.prm.module.requirement.mapper.RequirementMapper;
import com.prm.module.system.mapper.SysUserMapper;
import com.prm.module.task.application.TaskService;
import com.prm.module.task.domain.TaskStateMachine;
import com.prm.module.task.dto.CreateTaskRequest;
import com.prm.module.task.dto.TaskDTO;
import com.prm.module.task.entity.Task;
import com.prm.module.task.mapper.TaskMapper;
import com.prm.module.task.mapper.TaskWorklogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServicePermissionTests {

    @Mock
    private TaskMapper taskMapper;
    @Mock
    private TaskWorklogMapper worklogMapper;
    @Mock
    private TaskStateMachine stateMachine;
    @Mock
    private SysUserMapper userMapper;
    @Mock
    private RequirementMapper requirementMapper;
    @Mock
    private ProjectMemberMapper projectMemberMapper;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        lenient().when(taskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(new Page<Task>(1, 20));
    }

    @Test
    void createShouldDenyProjectMemberEvenWhenGlobalManagerFlagIsTrue() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);
            securityUtil.when(SecurityUtil::isManager).thenReturn(true);

            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 2001L, "DEV"));

            assertThatThrownBy(() -> taskService.create(createRequest(1001L, "Task-1")))
                    .isInstanceOf(BizException.class);

            verify(taskMapper, never()).insert(any(Task.class));
        }
    }

    @Test
    void createShouldAllowProjectManagerWithoutGlobalManagerFlag() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(3001L);
            securityUtil.when(SecurityUtil::isManager).thenReturn(false);

            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 3001L, "PROJECT_ADMIN"));

            assertThatCode(() -> taskService.create(createRequest(1001L, "Task-1")))
                    .doesNotThrowAnyException();

            verify(taskMapper).insert(any(Task.class));
        }
    }

    @Test
    void updateStatusShouldDenyProjectMemberOnOthersTaskEvenWhenGlobalManagerFlagIsTrue() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);
            securityUtil.when(SecurityUtil::isManager).thenReturn(true);

            when(taskMapper.selectById(9001L)).thenReturn(task(9001L, 1001L, 3001L, "TODO"));
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 2001L, "DEV"));

            assertThatThrownBy(() -> taskService.updateStatus(9001L, "IN_PROGRESS"))
                    .isInstanceOf(BizException.class);

            verify(taskMapper, never()).updateById(any(Task.class));
        }
    }

    @Test
    void assignShouldAllowProjectManagerWithoutGlobalManagerFlag() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(3001L);
            securityUtil.when(SecurityUtil::isManager).thenReturn(false);

            when(taskMapper.selectById(9001L)).thenReturn(task(9001L, 1001L, 2001L, "TODO"));
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 3001L, "PROJECT_ADMIN"));

            assertThatCode(() -> taskService.assign(9001L, 3002L)).doesNotThrowAnyException();

            verify(taskMapper).updateById(any(Task.class));
        }
    }

    @Test
    void getByIdShouldDenyUserOutsideProjectEvenWhenGlobalManagerFlagIsTrue() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);
            securityUtil.when(SecurityUtil::isManager).thenReturn(true);

            when(taskMapper.selectById(9001L)).thenReturn(task(9001L, 1001L, 3001L, "TODO"));
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            assertThatThrownBy(() -> taskService.getById(9001L))
                    .isInstanceOf(BizException.class);
        }
    }

    @Test
    void pageShouldReturnEmptyForNonMemberWithoutQueryingTasksEvenWhenGlobalManagerFlagIsTrue() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);
            securityUtil.when(SecurityUtil::isManager).thenReturn(true);

            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            IPage<TaskDTO> page = taskService.page(1, 20, 1001L, null, null, null, null);

            assertThat(page.getRecords()).isEmpty();
            verify(taskMapper, never()).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }
    }

    private CreateTaskRequest createRequest(Long projectId, String title) {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setProjectId(projectId);
        request.setTitle(title);
        request.setEstimatedHours(BigDecimal.ONE);
        return request;
    }

    private Task task(Long id, Long projectId, Long assigneeId, String status) {
        Task task = new Task();
        task.setId(id);
        task.setProjectId(projectId);
        task.setAssigneeId(assigneeId);
        task.setStatus(status);
        task.setDeleted(0);
        return task;
    }

    private ProjectMember member(Long projectId, Long userId, String role) {
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRole(role);
        return member;
    }
}
