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
import cn.xu.domain.notification.event.NotificationEvent;
import cn.xu.domain.notification.event.factory.NotificationEventFactory;
//import cn.xu.domain.notification.handler.NotificationEventHandler;
import cn.xu.domain.notification.handler.NotificationExceptionHandler;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * Disruptor配置类
 */
@Configuration
@RequiredArgsConstructor
public class DisruptorConfig {

    @Resource
    private LikeEventHandler likeEventHandler;
    
    @Resource
    private ArticleDomainEventHandler articleEventHandler;

    @Resource
    private OperationLogEventHandler operationLogEventHandler;

    @Resource
    private MessageEventHandler messageEventHandler;

//    @Resource
//    private NotificationEventHandler notificationEventHandler;

    @Resource
    private NotificationExceptionHandler notificationExceptionHandler;

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

    /**
     * 通知事件Disruptor配置
     * 使用更大的缓冲区和多生产者模式，提高并发处理能力
     */
    @Bean(name = "notificationDisruptor")
    public Disruptor<NotificationEvent> notificationDisruptor() {
        // 使用更大的缓冲区大小，2的次幂
        int bufferSize = 2048;
        
        // 创建disruptor，使用自定义的事件工厂
        Disruptor<NotificationEvent> disruptor = new Disruptor<>(
                NotificationEventFactory.getInstance(),
                bufferSize,
                DaemonThreadFactory.INSTANCE,
                ProducerType.MULTI,  // 使用多生产者模式
                new BlockingWaitStrategy()  // 使用阻塞等待策略，保证消息不丢失
        );

        // 配置事件处理器
        disruptor.setDefaultExceptionHandler(notificationExceptionHandler);
//        disruptor.handleEventsWith(notificationEventHandler);

        // 启动disruptor
        disruptor.start();
        
        return disruptor;
    }

    /**
     * 通知事件RingBuffer配置
     * 用于生产通知事件
     */
    @Bean(name = "notificationRingBuffer")
    public RingBuffer<NotificationEvent> notificationRingBuffer(Disruptor<NotificationEvent> notificationDisruptor) {
        return notificationDisruptor.getRingBuffer();
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