package com.prm.common.util;

import cn.dev33.satoken.stp.StpUtil;
import com.prm.common.exception.BizException;

/**
 * 安全工具类，封装 Sa-Token 常用操作
 */
public class SecurityUtil {

    private SecurityUtil() {}

    public static Long getCurrentUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            throw BizException.forbidden("获取当前用户失败");
        }
    }

    public static Long getCurrentUserIdOrNull() {
        try {
            return StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean hasRole(String role) {
        return StpUtil.hasRole(role);
    }

    public static boolean isSuperAdmin() {
        return StpUtil.hasRole("SUPER_ADMIN");
    }

    /** 是否是管理者（超级管理员 或 项目经理），可进行增删改等管理操作 */
    public static boolean isManager() {
        return StpUtil.hasRole("SUPER_ADMIN") || StpUtil.hasRole("PROJECT_ADMIN");
    }
}
