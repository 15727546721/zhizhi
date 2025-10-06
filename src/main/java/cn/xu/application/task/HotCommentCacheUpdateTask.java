package cn.xu.application.task;

import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.model.valueobject.CommentType;
import cn.xu.domain.comment.repository.ICommentRepository;
import cn.xu.domain.comment.service.CommentCacheDomainService;
import cn.xu.domain.comment.service.HotCommentDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 热点评论缓存更新任务
 * 定时更新热门评论数据到缓存中，保持数据的新鲜度
 * 
 * @author Lily
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HotCommentCacheUpdateTask {

    private final ICommentRepository commentRepository;
    private final CommentCacheDomainService commentCacheDomainService;
    private final HotCommentDomainService hotCommentDomainService;

    /**
     * 每30分钟更新一次热门评论缓存
     * 保持热门评论数据的新鲜度
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // 30分钟
    public void updateHotCommentCache() {
        log.info("开始执行热门评论缓存更新任务");
        
        try {
            // 更新文章评论热门数据
            updateHotCommentsForType(CommentType.POST);
            
            // 更新随笔评论热门数据
            updateHotCommentsForType(CommentType.ESSAY);
            
            log.info("热门评论缓存更新任务执行完成");
        } catch (Exception e) {
            log.error("热门评论缓存更新任务执行失败", e);
        }
    }

    /**
     * 更新指定类型的热门评论
     * 
     * @param commentType 评论类型
     */
    private void updateHotCommentsForType(CommentType commentType) {
        log.info("开始更新{}类型热门评论缓存", commentType.getDescription());
        
        try {
            // 获取需要更新的目标ID列表
            // 这里简化处理，实际项目中可能需要从其他地方获取热门目标ID
            Set<Long> targetIds = getHotTargetIds(commentType);
            
            // 更新每个目标的热门评论缓存
            for (Long targetId : targetIds) {
                try {
                    hotCommentDomainService.refreshHotCommentCache(commentType.getValue(), targetId);
                    
                    // 添加短暂延迟，避免对系统造成过大压力
                    Thread.sleep(50);
                } catch (Exception e) {
                    log.error("更新{}类型目标{}的热门评论缓存失败", 
                             commentType.getDescription(), targetId, e);
                }
            }
            
            log.info("更新{}类型热门评论缓存完成，共更新{}个目标", 
                    commentType.getDescription(), targetIds.size());
        } catch (Exception e) {
            log.error("更新{}类型热门评论缓存失败", commentType.getDescription(), e);
        }
    }

    /**
     * 获取热门目标ID列表
     * 
     * @param commentType 评论类型
     * @return 目标ID集合
     */
    private Set<Long> getHotTargetIds(CommentType commentType) {
        // 这里简化处理，实际项目中可能需要从统计数据或其他地方获取热门目标
        // 例如：从Redis中获取评论数最多的文章ID，或者从数据库中查询等
        
        try {
            // 查询一批热门评论，提取目标ID
            List<CommentEntity> hotComments = commentRepository.findCommentBatch(0, 50);
            
            return hotComments.stream()
                    .filter(comment -> comment.getTargetType().equals(commentType.getValue()))
                    .map(CommentEntity::getTargetId)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.warn("获取{}类型热门目标ID列表失败，使用空列表", commentType.getDescription(), e);
            return java.util.Collections.emptySet();
        }
    }
}