package cn.xu.domain.comment.event;

import cn.xu.infrastructure.persistent.dao.IArticleDao;
import cn.xu.infrastructure.persistent.dao.ICommentDao;
import cn.xu.infrastructure.persistent.dao.IEssayDao;
import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;

@Slf4j
@Component
public class CommentEventHandler implements EventHandler<CommentCountEvent> {

    @Resource
    private IArticleDao articleDao;
    @Resource
    private IEssayDao essayDao;
    @Resource
    private ICommentDao commentDao;
    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public void onEvent(CommentCountEvent commentEvent, long l, boolean b) throws Exception {
        log.info("[评论事件]接收到事件: {}", commentEvent);
        switch (commentEvent.getTargetType()) {
            case ARTICLE:
                handleArticleComment(commentEvent);
                break;
            case ESSAY:
                handleEssayComment(commentEvent);
                break;
            default:
                log.error("[评论事件]未知的评论类型: {}", commentEvent.getTargetType());
                break;
        }

    }

    private void handleArticleComment(CommentCountEvent commentEvent) {
        // 一级评论
        articleDao.updateCommentCount(commentEvent.getTargetId(), commentEvent.getCount());

        if (commentEvent.getLevel() == 2) { // 回复评论需要再处理一次计数
            transactionTemplate.execute(status -> {
                try {
                    articleDao.updateCommentCount(commentEvent.getTargetId(), commentEvent.getCount());
                    commentDao.updateCommentCount(commentEvent.getCommentId(), commentEvent.getCount());
                } catch (Exception e) {
                    log.error("[评论计数]更新失败", e);
                }
                return 1;
            });
        }
    }

    private void handleEssayComment(CommentCountEvent commentEvent) {
        // 一级评论
        essayDao.updateCommentCount(commentEvent.getTargetId(), commentEvent.getCount());

        if (commentEvent.getLevel() == 2) {
            transactionTemplate.execute(status -> { // 回复评论需要再处理一次计数
                try {
                    essayDao.updateCommentCount(commentEvent.getTargetId(), commentEvent.getCount());
                    commentDao.updateCommentCount(commentEvent.getCommentId(), commentEvent.getCount());
                } catch (Exception e) {
                    log.error("[评论计数]更新失败", e);
                }
                return 1;
            });
        }
    }
}
