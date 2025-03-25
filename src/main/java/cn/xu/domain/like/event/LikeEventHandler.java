package cn.xu.domain.like.event;

import cn.xu.infrastructure.persistent.dao.IArticleDao;
import cn.xu.infrastructure.persistent.dao.ICommentDao;
import cn.xu.infrastructure.persistent.dao.ILikeDao;
import cn.xu.infrastructure.persistent.po.Like;
import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * 点赞事件处理器
 */
@Slf4j
@Component
public class LikeEventHandler implements EventHandler<LikeEvent> {

    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private ILikeDao likeDao;
    @Resource
    private IArticleDao articleDao;
    @Resource
    private ICommentDao commentDao;

    @Override
    public void onEvent(LikeEvent event, long sequence, boolean endOfBatch) {
        log.info("处理点赞事件: {}", event);
        try {
            // 更新MySQL点赞记录
            Like like = likeDao.findByUserIdAndTypeAndTargetId(
                    event.getUserId(), event.getType().getValue(), event.getTargetId());
            int status = event.getStatus() == null ? 1 : (event.getStatus() ? 1 : 0);
            if (like == null) {
                likeDao.save(Like.builder()
                        .userId(event.getUserId())
                        .targetId(event.getTargetId())
                        .type(event.getType().getValue())
                        .status(status)
                        .createTime(event.getCreateTime())
                        .build());
            } else {
                // 更新点赞状态
                likeDao.updateStatus(event.getUserId(),
                        event.getType().getValue(),
                        event.getTargetId(),
                        status);
            }
            switch (event.getType()) {
                case ARTICLE:
                    articleDao.updateLikeCount(event.getTargetId(), status == 1 ? 1 : -1);
                    break;
                case COMMENT:
                    commentDao.updateLikeCount(event.getTargetId(), status == 1 ? 1 : -1);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("点赞记录查询失败", e);
        }
    }

}