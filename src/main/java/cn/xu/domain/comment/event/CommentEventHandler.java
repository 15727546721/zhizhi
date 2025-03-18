package cn.xu.domain.comment.event;

import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.persistent.dao.IArticleDao;
import cn.xu.infrastructure.persistent.dao.ICommentDao;
import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;

@Slf4j
@Component
public class CommentEventHandler implements EventHandler<CommentEvent> {

    @Resource
    private IArticleDao articleDao;
    @Resource
    private ICommentDao commentDao;
    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public void onEvent(CommentEvent commentEvent, long l, boolean b) throws Exception {
        log.info("Received comment event: {}", commentEvent);
        if (commentEvent.getCommentId() == null) { // 新增评论
            articleDao.updateCommentCount(commentEvent.getTargetId(), 1);
        } else { // 回复评论
            transactionTemplate.execute(status -> {
                try {
                    articleDao.updateCommentCount(commentEvent.getTargetId(), 1);
                    commentDao.updateCommentCount(commentEvent.getCommentId(), 1);
                } catch (Exception e) {
                    throw new BusinessException("评论计数更新失败");
                }
                return 1;
            });
        }
    }
}
