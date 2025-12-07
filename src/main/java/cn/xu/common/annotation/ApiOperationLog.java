package cn.xu.common.annotation;

import java.lang.annotation.*;

/**
 * API操作日志注解
 * 用于记录一些操作日志相关的API方法
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface ApiOperationLog {

    /**
     * API 功能描述
     *
     * @return 描述信息
     */
    String description() default "";
}
