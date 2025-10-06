package cn.xu.domain.essay.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 随笔领域事件发布器
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
        eventPublisher.publishEvent(event);
        log.info("发布随笔删除事件: {}", event);
    }
}