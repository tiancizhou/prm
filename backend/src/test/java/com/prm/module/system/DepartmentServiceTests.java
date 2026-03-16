package com.prm.module.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.prm.common.exception.BizException;
import com.prm.module.system.application.DepartmentService;
import com.prm.module.system.dto.DepartmentTreeDTO;
import com.prm.module.system.entity.SysDepartment;
import com.prm.module.system.mapper.SysDepartmentMapper;
import com.prm.module.system.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTests {

    @Mock
    private SysDepartmentMapper departmentMapper;
    @Mock
    private SysUserMapper userMapper;

    private DepartmentService departmentService;

    @BeforeEach
    void setUp() {
        departmentService = new DepartmentService(departmentMapper, userMapper);
    }

    @Test
    void listTreeShouldBuildParentChildHierarchy() {
        SysDepartment root = department(1L, "Engineering", null);
        SysDepartment child = department(2L, "Platform", 1L);

        when(departmentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(root, child));

        List<DepartmentTreeDTO> tree = departmentService.listTree();

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).getName()).isEqualTo("默认公司");
        assertThat(tree.get(0).getChildren()).hasSize(1);
        assertThat(tree.get(0).getChildren().get(0).getName()).isEqualTo("Engineering");
        assertThat(tree.get(0).getChildren().get(0).getChildren()).hasSize(1);
        assertThat(tree.get(0).getChildren().get(0).getChildren().get(0).getName()).isEqualTo("Platform");
    }

    @Test
    void deleteShouldRejectWhenDepartmentHasChildren() {
        when(departmentMapper.selectById(1L)).thenReturn(department(1L, "Engineering", null));
        when(departmentMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThatThrownBy(() -> departmentService.delete(1L))
                .isInstanceOf(BizException.class);
    }

    @Test
    void deleteShouldRejectWhenDepartmentHasUsers() {
        when(departmentMapper.selectById(1L)).thenReturn(department(1L, "Engineering", null));
        when(departmentMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThatThrownBy(() -> departmentService.delete(1L))
                .isInstanceOf(BizException.class);
    }

    private SysDepartment department(Long id, String name, Long parentId) {
        SysDepartment department = new SysDepartment();
        department.setId(id);
        department.setName(name);
        department.setParentId(parentId);
        department.setDeleted(0);
        return department;
    }
}
