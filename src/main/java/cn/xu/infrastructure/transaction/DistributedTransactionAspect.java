package cn.xu.infrastructure.transaction;

import cn.xu.domain.comment.service.CommentCreationDomainService;
import cn.xu.domain.like.service.LikeDomainService;
import cn.xu.domain.post.service.PostService;
import cn.xu.domain.user.service.impl.UserPointServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 分布式事务切面
 * 处理带有@DistributedTransactional注解的方法
 */
@Slf4j
@Aspect
@Component
public class DistributedTransactionAspect {
    
    private final DistributedTransactionManager transactionManager;
    private final PostService postService;
    private final LikeDomainService likeDomainService;
    private final CommentCreationDomainService commentCreationDomainService;
    private final UserPointServiceImpl userPointService;
    
    @Autowired
    public DistributedTransactionAspect(
            DistributedTransactionManager transactionManager,
            PostService postService,
            LikeDomainService likeDomainService,
            CommentCreationDomainService commentCreationDomainService,
            UserPointServiceImpl userPointService) {
        this.transactionManager = transactionManager;
        this.postService = postService;
        this.likeDomainService = likeDomainService;
        this.commentCreationDomainService = commentCreationDomainService;
        this.userPointService = userPointService;
    }
    
    /**
     * 环绕通知处理分布式事务
     */
    @Around("@annotation(distributedTransactional)")
    public Object handleDistributedTransaction(ProceedingJoinPoint joinPoint, 
                                             DistributedTransactional distributedTransactional) throws Throwable {
        log.debug("开始处理分布式事务: {}", joinPoint.getSignature().getName());
        
        // 开始分布式事务
        transactionManager.begin();
        
        // 注册事务参与者
        transactionManager.registerParticipant(postService);
        transactionManager.registerParticipant(likeDomainService);
        transactionManager.registerParticipant(commentCreationDomainService);
        transactionManager.registerParticipant(userPointService);
        
        try {
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            // 提交事务
            transactionManager.commit();
            
            log.debug("分布式事务执行成功: {}", joinPoint.getSignature().getName());
            return result;
        } catch (Exception e) {
            log.error("分布式事务执行失败: {}", joinPoint.getSignature().getName(), e);
            
            // 标记为回滚并执行回滚
            transactionManager.setRollbackOnly();
            transactionManager.rollback();
            
            // 重新抛出异常
            throw e;
        } catch (Throwable t) {
            log.error("分布式事务执行出现未知错误: {}", joinPoint.getSignature().getName(), t);
            
            // 标记为回滚并执行回滚
            transactionManager.setRollbackOnly();
            transactionManager.rollback();
            
            // 重新抛出错误
            throw t;
        }
    }
}