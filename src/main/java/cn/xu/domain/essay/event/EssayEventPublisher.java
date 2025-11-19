package cn.xu.domain.essay.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 随笔事件发布器
 * 使用Spring Event机制发布随笔相关事件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EssayEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 发布随笔删除事件
     */
    public void publishEssayDeletedEvent(EssayDeletedEvent event) {
        log.debug("发布随笔删除事件: essayId={}", event.getEssayId());
        eventPublisher.publishEvent(event);
    }
}