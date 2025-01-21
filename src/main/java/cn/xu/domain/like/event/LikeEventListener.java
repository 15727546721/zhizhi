package cn.xu.domain.like.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LikeEventListener {

    @EventListener
    public void handleLikeEvent(LikeEvent event) {
        if (event.isLiked()) {
            log.info("用户[{}]点赞了{}[{}]", event.getUserId(),
                    event.getType().getDescription(), event.getTargetId());
        } else {
            log.info("用户[{}]取消点赞了{}[{}]", event.getUserId(),
                    event.getType().getDescription(), event.getTargetId());
        }
        // TODO: 这里可以添加其他业务逻辑，比如：
        // 1. 发送通知
        // 2. 更新统计数据
        // 3. 触发其他业务流程
    }
} 