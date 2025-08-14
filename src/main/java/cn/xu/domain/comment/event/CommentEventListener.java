package cn.xu.domain.comment.event;


import cn.xu.infrastructure.persistent.dao.ArticleMapper;
import cn.xu.infrastructure.persistent.dao.CommentMapper;
import cn.xu.infrastructure.persistent.dao.EssayMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;

@Slf4j
@Component
public class CommentEventListener {

    @Resource
    private ArticleMapper articleDao;
    @Resource
    private EssayMapper essayDao;
    @Resource
    private CommentMapper commentDao;
    @Resource
    private TransactionTemplate transactionTemplate;

    @Async  // 可选：异步处理事件
    @EventListener
    public void onCommentCountEvent(CommentCountEvent commentEvent) {
        log.info("[评论事件]接收到事件: {}", commentEvent);

        switch (commentEvent.getTargetType().toString()) {
            case "ARTICLE":
                handleArticleComment(commentEvent);
                break;
            case "ESSAY":
                handleEssayComment(commentEvent);
                break;
            default:
                log.error("[评论事件]未知的评论类型: {}", commentEvent.getTargetType());
                break;
        }
    }

    @Async
    @EventListener
    public void onCommentCreated(CommentCreatedEvent event) {
        // 通知用户、统计分析、审核等等...
    }

    @Async
    @EventListener
    public void onCommentLiked(CommentLikedEvent event) {
        // 更新点赞计数、发送通知、写日志...
    }

    private void handleArticleComment(CommentCountEvent commentEvent) {
        articleDao.updateCommentCount(commentEvent.getTargetId(), commentEvent.getCount());

        if (commentEvent.getLevel() == 2) {
            transactionTemplate.execute(status -> {
                try {
                    articleDao.updateCommentCount(commentEvent.getTargetId(), commentEvent.getCount());
                    commentDao.updateCommentCount(commentEvent.getCommentId(), commentEvent.getCount());
                } catch (Exception e) {
                    log.error("[评论计数]更新失败", e);
                }
                return null;
            });
        }
    }

    private void handleEssayComment(CommentCountEvent commentEvent) {
        essayDao.updateCommentCount(commentEvent.getTargetId(), commentEvent.getCount());

        if (commentEvent.getLevel() == 2) {
            transactionTemplate.execute(status -> {
                try {
                    essayDao.updateCommentCount(commentEvent.getTargetId(), commentEvent.getCount());
                    commentDao.updateCommentCount(commentEvent.getCommentId(), commentEvent.getCount());
                } catch (Exception e) {
                    log.error("[评论计数]更新失败", e);
                }
                return null;
            });
        }
    }
}

