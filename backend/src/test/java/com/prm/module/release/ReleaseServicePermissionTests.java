package com.prm.module.release;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prm.common.exception.BizException;
import com.prm.common.util.SecurityUtil;
import com.prm.module.project.entity.ProjectMember;
import com.prm.module.project.mapper.ProjectMemberMapper;
import com.prm.module.release.application.ReleaseService;
import com.prm.module.release.dto.CreateReleaseRequest;
import com.prm.module.release.entity.Release;
import com.prm.module.release.mapper.ReleaseMapper;
import com.prm.module.system.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReleaseServicePermissionTests {

    @Mock
    private ReleaseMapper releaseMapper;
    @Mock
    private SysUserMapper userMapper;
    @Mock
    private ProjectMemberMapper projectMemberMapper;

    @InjectMocks
    private ReleaseService releaseService;

    @BeforeEach
    void setUp() {
        lenient().when(releaseMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(new Page<Release>(1, 20));
    }

    @Test
    void createShouldDenyProjectMember() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);

            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 2001L, "DEV"));

            assertThatThrownBy(() -> releaseService.create(createRequest(1001L, "1.0.0")))
                    .isInstanceOf(BizException.class);

            verify(releaseMapper, never()).insert(any(Release.class));
        }
    }

    @Test
    void createShouldAllowProjectManager() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(3001L);

            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 3001L, "PROJECT_ADMIN"));

            assertThatCode(() -> releaseService.create(createRequest(1001L, "1.0.0")))
                    .doesNotThrowAnyException();

            verify(releaseMapper).insert(any(Release.class));
        }
    }

    @Test
    void getByIdShouldDenyUserOutsideProject() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);

            when(releaseMapper.selectById(9001L)).thenReturn(release(9001L, 1001L, "DRAFT"));
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            assertThatThrownBy(() -> releaseService.getById(9001L))
                    .isInstanceOf(BizException.class);
        }
    }

    @Test
    void pageShouldReturnEmptyForNonMemberWithoutQueryingReleases() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);

            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            assertThat(releaseService.page(1, 20, 1001L).getRecords()).isEmpty();
            verify(releaseMapper, never()).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }
    }

    @Test
    void publishShouldDenyProjectMember() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);

            when(releaseMapper.selectById(9001L)).thenReturn(release(9001L, 1001L, "DRAFT"));
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 2001L, "DEV"));

            assertThatThrownBy(() -> releaseService.publish(9001L))
                    .isInstanceOf(BizException.class);

            verify(releaseMapper, never()).updateById(any(Release.class));
        }
    }

    @Test
    void publishShouldAllowProjectManager() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(3001L);

            when(releaseMapper.selectById(9001L)).thenReturn(release(9001L, 1001L, "DRAFT"));
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 3001L, "PROJECT_ADMIN"));

            assertThatCode(() -> releaseService.publish(9001L)).doesNotThrowAnyException();

            verify(releaseMapper).updateById(any(Release.class));
        }
    }

    private CreateReleaseRequest createRequest(Long projectId, String version) {
        CreateReleaseRequest request = new CreateReleaseRequest();
        request.setProjectId(projectId);
        request.setVersion(version);
        return request;
    }

    private Release release(Long id, Long projectId, String status) {
        Release release = new Release();
        release.setId(id);
        release.setProjectId(projectId);
        release.setStatus(status);
        release.setDeleted(0);
        return release;
    }

    private ProjectMember member(Long projectId, Long userId, String role) {
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRole(role);
        return member;
    }
}
