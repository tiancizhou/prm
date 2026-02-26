package com.prm.common.exception;

import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        super(message);
        this.code = 400;
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public static BizException of(String message) {
        return new BizException(message);
    }

    public static BizException notFound(String resource) {
        return new BizException(404, resource + " 不存在");
    }

    public static BizException forbidden(String action) {
        return new BizException(403, "无权限：" + action);
    }
}
