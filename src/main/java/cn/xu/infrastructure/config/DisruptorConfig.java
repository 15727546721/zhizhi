package cn.xu.infrastructure.config;

import cn.xu.domain.like.event.LikeEvent;
import cn.xu.domain.like.event.LikeEventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Disruptor 配置类 - 用于高性能点赞事件处理
 */
@Configuration
public class DisruptorConfig {

    @Resource
    private LikeEventHandler likeEventHandler;

    @Bean
    public RingBuffer<LikeEvent> likeEventRingBuffer() {
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        LikeEventFactory factory = new LikeEventFactory();
        int bufferSize = 1024;

        Disruptor<LikeEvent> disruptor = new Disruptor<>(
                factory,
                bufferSize,
                threadFactory
        );

        disruptor.handleEventsWith(likeEventHandler);
        disruptor.start();

        return disruptor.getRingBuffer();
    }

    /**
     * 点赞事件工厂
     */
    private static class LikeEventFactory implements com.lmax.disruptor.EventFactory<LikeEvent> {
        @Override
        public LikeEvent newInstance() {
            return LikeEvent.builder()
                    .userId(null)
                    .targetId(null)
                    .type(null)
                    .liked(false)
                    .occurredTime(null)
                    .build();
        }
    }
} 