package cn.xu.infrastructure.config;

import cn.xu.domain.article.event.ArticleEventWrapper;
import cn.xu.domain.article.service.impl.ArticleDomainEventHandler;
import cn.xu.domain.like.event.LikeEvent;
import cn.xu.domain.like.event.LikeEventHandler;
import cn.xu.domain.logging.event.OperationLogEvent;
import cn.xu.domain.logging.event.factory.OperationLogEventFactory;
import cn.xu.domain.logging.event.handler.OperationLogEventHandler;
import cn.xu.domain.message.event.BaseMessageEvent;
import cn.xu.domain.message.event.SystemMessageEvent;
import cn.xu.domain.message.event.handler.MessageEventHandler;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class DisruptorConfig {

    @Resource
    private LikeEventHandler likeEventHandler;
    
    @Resource
    private ArticleDomainEventHandler articleEventHandler;

    @Resource
    private OperationLogEventHandler operationLogEventHandler;

    @Resource
    private MessageEventHandler messageEventHandler;

    @Bean
    public RingBuffer<LikeEvent> likeEventRingBuffer() {
        Disruptor<LikeEvent> disruptor = new Disruptor<>(
                new LikeEventFactory(),
                1024,
                DaemonThreadFactory.INSTANCE
        );

        disruptor.handleEventsWith(likeEventHandler);
        disruptor.start();

        return disruptor.getRingBuffer();
    }

    @Bean
    public RingBuffer<ArticleEventWrapper> articleEventRingBuffer() {
        Disruptor<ArticleEventWrapper> disruptor = new Disruptor<>(
                ArticleEventWrapper.Factory.getInstance(),
                1024 * 1024,
                DaemonThreadFactory.INSTANCE,
                ProducerType.MULTI,
                new BlockingWaitStrategy()
        );
        
        disruptor.handleEventsWith(articleEventHandler);
        disruptor.start();
        
        return disruptor.getRingBuffer();
    }

    @Bean(name = "operationLogRingBuffer")
    public RingBuffer<OperationLogEvent> operationLogRingBuffer() {
        Disruptor<OperationLogEvent> disruptor = new Disruptor<>(
                new OperationLogEventFactory(),
                1024,
                DaemonThreadFactory.INSTANCE,
                ProducerType.MULTI,
                new BlockingWaitStrategy()
        );

        disruptor.handleEventsWith(operationLogEventHandler);
        disruptor.start();

        return disruptor.getRingBuffer();
    }

    @Bean(name = "messageRingBuffer")
    public RingBuffer<BaseMessageEvent> messageRingBuffer() {
        Disruptor<BaseMessageEvent> disruptor = new Disruptor<>(
                new MessageEventFactory(),
                2048,
                DaemonThreadFactory.INSTANCE,
                ProducerType.MULTI,
                new BlockingWaitStrategy()
        );

        disruptor.handleEventsWith(messageEventHandler);
        disruptor.start();

        return disruptor.getRingBuffer();
    }

    private static class LikeEventFactory implements EventFactory<LikeEvent> {
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

    private static class MessageEventFactory implements EventFactory<BaseMessageEvent> {
        @Override
        public BaseMessageEvent newInstance() {
            return SystemMessageEvent.builder()
                    .build();  // 简化初始化，所有字段默认为null
        }
    }
} 