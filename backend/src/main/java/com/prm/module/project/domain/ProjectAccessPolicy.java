package com.prm.module.project.domain;

import com.prm.common.util.SecurityUtil;
import com.prm.module.project.entity.ProjectMember;

public final class ProjectAccessPolicy {

    private ProjectAccessPolicy() {
    }

    public static boolean canRead(ProjectMember membership) {
        return SecurityUtil.isSuperAdmin() || membership != null;
    }

    public static boolean canManage(ProjectMember membership) {
        return SecurityUtil.isSuperAdmin()
                || (membership != null && SecurityUtil.hasRole("PROJECT_ADMIN"));
    }

    public static boolean hasProjectManagerSystemRole() {
        return SecurityUtil.isSuperAdmin() || SecurityUtil.hasRole("PROJECT_ADMIN");
    }
}
