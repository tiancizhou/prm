package com.prm.module.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prm.module.project.mapper.ProjectMapper;
import com.prm.module.project.mapper.ProjectMemberMapper;
import com.prm.module.system.application.UserService;
import com.prm.module.system.entity.SysDepartment;
import com.prm.module.system.entity.SysRole;
import com.prm.module.system.entity.SysUser;
import com.prm.module.system.mapper.SysDepartmentMapper;
import com.prm.module.system.mapper.SysRoleMapper;
import com.prm.module.system.mapper.SysUserMapper;
import com.prm.module.system.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceDepartmentScopeTests {

    @Mock
    private SysUserMapper userMapper;
    @Mock
    private SysUserRoleMapper userRoleMapper;
    @Mock
    private SysRoleMapper roleMapper;
    @Mock
    private SysDepartmentMapper departmentMapper;
    @Mock
    private ProjectMemberMapper projectMemberMapper;
    @Mock
    private ProjectMapper projectMapper;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userMapper, userRoleMapper, roleMapper, departmentMapper, projectMemberMapper, projectMapper);
    }

    @Test
    void pageShouldIncludeDescendantDepartmentsWhenFilteringByParentDepartment() {
        when(departmentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                department(10L, null),
                department(11L, 10L),
                department(12L, 11L)
        ));
        when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(new Page<>(1, 20));

        userService.page(1, 20, null, 10L);

        ArgumentCaptor<LambdaQueryWrapper<SysUser>> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(userMapper).selectPage(any(Page.class), captor.capture());
        LambdaQueryWrapper<SysUser> wrapper = captor.getValue();

        assertThat(wrapper.getSqlSegment()).containsIgnoringCase("department_id");
        assertThat(wrapper.getSqlSegment()).containsIgnoringCase("in");
        assertThat(wrapper.getParamNameValuePairs().values()).contains(10L, 11L, 12L);
    }

    private SysDepartment department(Long id, Long parentId) {
        SysDepartment department = new SysDepartment();
        department.setId(id);
        department.setParentId(parentId);
        department.setDeleted(0);
        return department;
    }
}
