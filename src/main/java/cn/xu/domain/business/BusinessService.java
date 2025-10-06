package cn.xu.domain.business;

import cn.xu.domain.comment.event.CommentCreatedEvent;
import cn.xu.domain.like.event.LikeEvent;
import cn.xu.domain.post.event.PostCreatedEvent;
import cn.xu.infrastructure.transaction.DistributedTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 综合业务服务
 * 处理跨领域的复杂业务逻辑，使用分布式事务保证数据一致性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessService {
    
    private final DataConsistencyService dataConsistencyService;
    
    /**
     * 处理用户活跃度统计
     * 使用分布式事务保证数据一致性
     */
    @DistributedTransactional
    public void handleUserActivity(PostCreatedEvent event) {
        try {
            log.info("处理用户发帖活跃度统计: {}", event);
            // 处理帖子创建后的数据一致性
            dataConsistencyService.handlePostCreation(event);
        } catch (Exception e) {
            log.error("处理用户活跃度统计失败", e);
            throw e; // 重新抛出异常以触发事务回滚
        }
    }
    
    /**
     * 处理用户活跃度统计
     */
    @DistributedTransactional
    public void handleUserActivity(CommentCreatedEvent event) {
        try {
            log.info("处理用户评论活跃度统计: {}", event);
            // 处理评论创建后的数据一致性
            dataConsistencyService.handleCommentCreation(event);
        } catch (Exception e) {
            log.error("处理用户活跃度统计失败", e);
            throw e; // 重新抛出异常以触发事务回滚
        }
    }
    
    /**
     * 处理用户活跃度统计
     */
    @DistributedTransactional
    public void handleUserActivity(LikeEvent event) {
        try {
            log.info("处理用户点赞活跃度统计: {}", event);
            // 处理点赞后的数据一致性
            dataConsistencyService.handleLikeOperation(event);
        } catch (Exception e) {
            log.error("处理用户活跃度统计失败", e);
            throw e; // 重新抛出异常以触发事务回滚
        }
    }
}