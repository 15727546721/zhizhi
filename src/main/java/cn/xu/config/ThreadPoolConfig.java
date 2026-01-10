package cn.xu.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池配置类
 * 
 * 
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

    private ExecutorService likePool;
    private ExecutorService notifyPool;

    /**
     * 点赞服务专用线程池
     */
    @Bean("likeThreadPool")
    public ExecutorService likeThreadPool() {
        likePool = new ThreadPoolExecutor(
                4,                      // 核心线程数
                8,                      // 最大线程数
                60L,                    // 空闲线程存活时间
                TimeUnit.SECONDS,       // 时间单位
                new LinkedBlockingQueue<>(1000),  // 工作队列
                r -> new Thread(r, "domain-like-thread-" + r.hashCode()),  // 线程工厂
                new ThreadPoolExecutor.CallerRunsPolicy()  // 拒绝策略
        );
        return likePool;
    }

    /**
     * 通知服务专用线程池
     */
    @Bean("notifyThreadPool")
    public ExecutorService notifyThreadPool() {
        notifyPool = new ThreadPoolExecutor(
                4,                      // 核心线程数
                8,                      // 最大线程数
                60L,                    // 空闲线程存活时间
                TimeUnit.SECONDS,       // 时间单位
                new LinkedBlockingQueue<>(1000),  // 工作队列
                r -> new Thread(r, "domain-notify-thread-" + r.hashCode()),  // 线程工厂
                new ThreadPoolExecutor.CallerRunsPolicy()  // 拒绝策略
        );
        return notifyPool;
    }

    /**
     * 应用关闭时优雅关闭线程池
     */
    @PreDestroy
    public void shutdown() {
        log.info("正在关闭线程池...");
        shutdownPool(likePool, "likeThreadPool");
        shutdownPool(notifyPool, "notifyThreadPool");
        log.info("线程池已关闭");
    }

    private void shutdownPool(ExecutorService pool, String name) {
        if (pool != null && !pool.isShutdown()) {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.warn("线程池 {} 未能在10秒内正常关闭，强制关闭", name);
                    pool.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.warn("等待线程池 {} 关闭时被中断，强制关闭", name);
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}