package cn.xu.infrastructure.repository.redis;

import cn.xu.domain.like.repository.ILikeRepository;
import cn.xu.infrastructure.general.config.HotDataProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Redis点赞热点数据管理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLikeHotDataManager {

    private final HotDataProperties hotDataProperties;
    private final ILikeRepository likeRepository;

    /**
     * 应用启动时预热热点数据
     */
    @EventListener(ApplicationStartedEvent.class)
    public void warmupOnStartup() {
        log.info("开始预热点赞热点数据...");
        hotDataProperties.getItems().stream()
                .filter(item -> item.isNeedWarmup())
                .forEach(item -> {
                    try {
                        likeRepository.syncToCache(item.getTargetId(), item.getType());
                        log.info("预热点赞数据成功: targetId={}, type={}",
                                item.getTargetId(), item.getType());
                    } catch (Exception e) {
                        log.error("预热点赞数据失败: targetId={}, type={}, error={}",
                                item.getTargetId(), item.getType(), e.getMessage());
                    }
                });
        log.info("点赞热点数据预热完成");
    }

    /**
     * 定时重建热点数据
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void rebuildHotData() {
        log.info("开始重建点赞热点数据...");
        hotDataProperties.getItems().stream()
                .filter(item -> item.isNeedRebuild())
                .forEach(item -> {
                    try {
                        likeRepository.syncToCache(item.getTargetId(), item.getType());
                        log.info("重建点赞数据成功: targetId={}, type={}",
                                item.getTargetId(), item.getType());
                    } catch (Exception e) {
                        log.error("重建点赞数据失败: targetId={}, type={}, error={}",
                                item.getTargetId(), item.getType(), e.getMessage());
                    }
                });
        log.info("点赞热点数据重建完成");
    }
} 