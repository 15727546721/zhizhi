package cn.xu.application.task;

import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.model.valueobject.CommentType;
import cn.xu.domain.comment.repository.ICommentRepository;
import cn.xu.domain.comment.service.CommentCacheDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 评论热门缓存预热任务
 * 定时预加载热门评论数据到缓存中，提升用户访问体验
 * 
 * @author Lily
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommentHotCachePreloadTask {

    private final ICommentRepository commentRepository;
    private final CommentCacheDomainService commentCacheDomainService;

    /**
     * 每天凌晨2点执行缓存预热任务
     * 预加载热门评论数据到Redis缓存中
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void preloadHotCommentCache() {
        log.info("开始执行评论热门缓存预热任务");
        
        try {
            // 预加载文章评论热门数据
            preloadHotComments(CommentType.ARTICLE, 100);
            
            // 预加载随笔评论热门数据
            preloadHotComments(CommentType.ESSAY, 100);
            
            log.info("评论热门缓存预热任务执行完成");
        } catch (Exception e) {
            log.error("评论热门缓存预热任务执行失败", e);
        }
    }

    /**
     * 预加载指定类型的热门评论
     * 
     * @param commentType 评论类型
     * @param batchSize 批量大小
     */
    private void preloadHotComments(CommentType commentType, int batchSize) {
        log.info("开始预加载{}类型热门评论，批量大小: {}", commentType.getDescription(), batchSize);
        
        int offset = 0;
        int loadedCount = 0;
        
        try {
            while (true) {
                // 分批查询热门评论
                List<CommentEntity> comments = commentRepository.findCommentBatch(offset, batchSize);
                
                // 如果没有更多数据，跳出循环
                if (comments == null || comments.isEmpty()) {
                    break;
                }
                
                // 按目标ID分组缓存
                comments.stream()
                    .collect(java.util.stream.Collectors.groupingBy(CommentEntity::getTargetId))
                    .forEach((targetId, targetComments) -> {
                        commentCacheDomainService.cacheHotCommentRank(commentType, targetId, targetComments);
                    });
                
                loadedCount += comments.size();
                offset += batchSize;
                
                // 避免一次性加载过多数据
                if (comments.size() < batchSize) {
                    break;
                }
                
                // 添加短暂延迟，避免对数据库造成过大压力
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            log.info("预加载{}类型热门评论完成，共加载{}条评论", commentType.getDescription(), loadedCount);
        } catch (Exception e) {
            log.error("预加载{}类型热门评论失败", commentType.getDescription(), e);
        }
    }
}