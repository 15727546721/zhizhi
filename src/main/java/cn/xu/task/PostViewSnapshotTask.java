package cn.xu.task;

import cn.xu.repository.mapper.PostMapper;
import cn.xu.service.statistics.PostViewStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 帖子浏览量快照定时任务
 * <p>每天凌晨记录帖子浏览量快照，用于计算环比</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostViewSnapshotTask {

    private final PostMapper postMapper;
    private final PostViewStatisticsService postViewStatisticsService;

    /**
     * 每天23:55记录当日浏览量快照
     * <p>在日期切换前记录，确保数据准确</p>
     */
    @Scheduled(cron = "0 55 23 * * ?")
    public void recordDailyViewSnapshot() {
        log.info("[定时任务] 开始记录帖子浏览量快照...");
        
        try {
            // 获取热门帖子的浏览量（只记录热门帖子，减少存储）
            List<Map<String, Object>> hotPosts = postMapper.selectHotPosts(100);
            
            if (hotPosts == null || hotPosts.isEmpty()) {
                log.info("[定时任务] 没有帖子需要记录快照");
                return;
            }
            
            Map<Long, Long> viewCounts = new HashMap<>();
            for (Map<String, Object> post : hotPosts) {
                Long postId = ((Number) post.get("id")).longValue();
                Long viewCount = post.get("viewCount") != null 
                        ? ((Number) post.get("viewCount")).longValue() : 0L;
                viewCounts.put(postId, viewCount);
            }
            
            postViewStatisticsService.recordDailySnapshots(viewCounts);
            
            log.info("[定时任务] 帖子浏览量快照记录完成，共 {} 条", viewCounts.size());
        } catch (Exception e) {
            log.error("[定时任务] 记录帖子浏览量快照失败", e);
        }
    }
}
