package cn.xu.infrastructure.transaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分布式事务注解
 * 用于标记需要分布式事务管理的方法
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedTransactional {
    
    /**
     * 事务超时时间（毫秒）
     */
    long timeout() default 30000;
    
    /**
     * 重试次数
     */
    int retryCount() default 3;
}