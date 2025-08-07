package cn.xu.domain.like.event;

import cn.xu.domain.article.service.IArticleService;
import cn.xu.domain.like.model.LikeType;
import cn.xu.infrastructure.persistent.dao.ArticleMapper;
import cn.xu.infrastructure.persistent.dao.CommentMapper;
import cn.xu.infrastructure.persistent.dao.LikeMapper;
import cn.xu.infrastructure.persistent.po.Like;
import cn.xu.infrastructure.persistent.repository.LikeRepository;
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
    private LikeRepository likeRepository;
    @Resource
    private IArticleService articleService;

    @Override
    public void onEvent(LikeEvent event, long sequence, boolean endOfBatch) {
        log.info("处理点赞事件: {}", event);
        try {
            if (event.getStatus()) {
                handleLike(event);
            } else {
                handleUnlike(event);
            }
        } catch (Exception e) {
            log.error("处理点赞事件失败: {}", e.getMessage(), e);
        }
    }

    private void handleLike(LikeEvent event) {
        // 1. 写入点赞数据（比如存入数据库或缓存）
        likeRepository.saveLike(event.getUserId(), event.getTargetId(), event.getType().getCode());

        // 2. 点赞计数增加
        likeRepository.incrementLikeCount(event.getTargetId(), event.getType().getCode());

        // 3. 文章热度排行榜更新
        if (event.getType() == LikeType.ARTICLE) {
            articleService.updateArticleHotScore(event.getTargetId());
        }
    }

    private void handleUnlike(LikeEvent event) {
        // 1. 删除点赞数据
        likeRepository.remove(event.getUserId(), event.getTargetId(), event.getType().getCode());

        // 2. 点赞计数减少
        likeRepository.decrementLikeCount(event.getTargetId(), event.getType().getCode());

        // 3. 文章热度排行榜更新
        if (event.getType() == LikeType.ARTICLE) {
            articleService.updateArticleHotScore(event.getTargetId());
        }
    }
}