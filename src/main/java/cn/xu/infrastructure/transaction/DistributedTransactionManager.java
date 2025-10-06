package cn.xu.infrastructure.transaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 分布式事务管理器
 * 用于处理跨数据源的事务一致性
 */
@Slf4j
@Component
public class DistributedTransactionManager {
    
    private final ThreadLocal<List<TransactionParticipant>> participants = new ThreadLocal<>();
    private final ThreadLocal<AtomicBoolean> isRollbackOnly = new ThreadLocal<>();
    
    /**
     * 开始分布式事务
     */
    public void begin() {
        participants.set(new ArrayList<>());
        isRollbackOnly.set(new AtomicBoolean(false));
        log.debug("开始分布式事务");
    }
    
    /**
     * 注册事务参与者
     */
    public void registerParticipant(TransactionParticipant participant) {
        List<TransactionParticipant> participantList = participants.get();
        if (participantList != null) {
            participantList.add(participant);
            log.debug("注册事务参与者: {}", participant.getClass().getSimpleName());
            
            // 通知参与者开始事务
            try {
                // 使用类名进行更准确的类型检查
                String className = participant.getClass().getName();
                if (className.equals("cn.xu.domain.post.service.PostService") || 
                    className.endsWith(".PostService")) {
                    ((cn.xu.domain.post.service.PostService) participant).beginTransaction();
                } else if (className.equals("cn.xu.domain.like.service.LikeDomainService") || 
                           className.endsWith(".LikeDomainService")) {
                    ((cn.xu.domain.like.service.LikeDomainService) participant).beginTransaction();
                } else if (className.equals("cn.xu.domain.comment.service.CommentCreationDomainService") || 
                           className.endsWith(".CommentCreationDomainService")) {
                    ((cn.xu.domain.comment.service.CommentCreationDomainService) participant).beginTransaction();
                } else if (className.equals("cn.xu.domain.user.service.impl.UserPointServiceImpl") || 
                           className.endsWith(".UserPointServiceImpl")) {
                    ((cn.xu.domain.user.service.impl.UserPointServiceImpl) participant).beginTransaction();
                }
            } catch (Exception e) {
                log.error("通知参与者开始事务失败: {}", participant.getClass().getSimpleName(), e);
            }
        }
    }
    
    /**
     * 提交事务
     */
    public void commit() {
        List<TransactionParticipant> participantList = participants.get();
        if (participantList != null) {
            if (isRollbackOnly.get().get()) {
                rollback();
                return;
            }
            
            // 逆序提交所有参与者
            for (int i = participantList.size() - 1; i >= 0; i--) {
                TransactionParticipant participant = participantList.get(i);
                try {
                    participant.commit();
                    log.debug("提交事务参与者: {}", participant.getClass().getSimpleName());
                } catch (Exception e) {
                    log.error("提交事务参与者失败: {}", participant.getClass().getSimpleName(), e);
                    // 如果提交失败，标记为回滚并执行回滚
                    isRollbackOnly.get().set(true);
                    rollback();
                    return;
                }
            }
            
            cleanup();
            log.info("分布式事务提交成功");
        }
    }
    
    /**
     * 回滚事务
     */
    public void rollback() {
        List<TransactionParticipant> participantList = participants.get();
        if (participantList != null) {
            // 正序回滚所有参与者
            for (TransactionParticipant participant : participantList) {
                try {
                    participant.rollback();
                    log.debug("回滚事务参与者: {}", participant.getClass().getSimpleName());
                } catch (Exception e) {
                    log.error("回滚事务参与者失败: {}", participant.getClass().getSimpleName(), e);
                }
            }
            
            cleanup();
            log.info("分布式事务回滚完成");
        }
    }
    
    /**
     * 标记事务为回滚-only
     */
    public void setRollbackOnly() {
        AtomicBoolean rollbackOnly = isRollbackOnly.get();
        if (rollbackOnly != null) {
            rollbackOnly.set(true);
            log.debug("标记事务为回滚-only");
        }
    }
    
    /**
     * 清理资源
     */
    private void cleanup() {
        participants.remove();
        isRollbackOnly.remove();
    }
}