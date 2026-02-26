package com.prm.module.log.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解：标注在 Controller 方法上，自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {

    /** 模块名 */
    String module() default "";

    /** 操作动作 */
    String action() default "";

    /** 业务类型 */
    String bizType() default "";
}
