package cn.xu.infrastructure.general.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池配置
 */
@Configuration
public class ThreadPoolConfig {

    /**
     * 点赞服务专用线程池
     */
    @Bean("domainLikeThreadPool")
    public ExecutorService likeThreadPool() {
        return new ThreadPoolExecutor(
                4,                      // 核心线程数
                8,                      // 最大线程数
                60L,                    // 空闲线程存活时间
                TimeUnit.SECONDS,       // 时间单位
                new LinkedBlockingQueue<>(1000),  // 工作队列
                r -> new Thread(r, "domain-like-thread-" + r.hashCode()),  // 线程工厂
                new ThreadPoolExecutor.CallerRunsPolicy()  // 拒绝策略
        );
    }
} 