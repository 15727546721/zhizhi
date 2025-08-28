package cn.xu.domain.like.event;

import cn.xu.domain.article.service.IArticleService;
import cn.xu.domain.like.model.LikeType;
import cn.xu.infrastructure.persistent.repository.LikeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class LikeEventListener {

    @Resource
    private LikeRepository likeRepository;

    @Resource
    private IArticleService articleService;

    @Async
    @EventListener
    public void onLikeEvent(LikeEvent event) {
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
        likeRepository.saveLike(event.getUserId(), event.getTargetId(), event.getType().getCode());

        if (event.getType() == LikeType.ARTICLE) {
            articleService.updateArticleHotScore(event.getTargetId());
        }
    }

    private void handleUnlike(LikeEvent event) {
        likeRepository.remove(event.getUserId(), event.getTargetId(), event.getType().getCode());

        if (event.getType() == LikeType.ARTICLE) {
            articleService.updateArticleHotScore(event.getTargetId());
        }
    }
}

