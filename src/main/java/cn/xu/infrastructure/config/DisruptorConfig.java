package cn.xu.infrastructure.config;

import cn.xu.domain.article.event.ArticleEvent;
import cn.xu.domain.article.event.ArticleEventHandler;
import cn.xu.domain.article.event.ArticleEventWrapper;
import cn.xu.domain.article.event.factory.ArticleEventFactory;
import cn.xu.domain.comment.event.CommentCountEvent;
import cn.xu.domain.comment.event.CommentEventHandler;
import cn.xu.domain.follow.event.FollowEvent;
import cn.xu.domain.follow.event.FollowEventHandler;
import cn.xu.domain.like.event.LikeEvent;
import cn.xu.domain.like.event.LikeEventHandler;
import cn.xu.domain.logging.event.OperationLogEvent;
import cn.xu.domain.logging.event.factory.OperationLogEventFactory;
import cn.xu.domain.logging.event.handler.OperationLogEventHandler;
import cn.xu.domain.message.event.BaseMessageEvent;
import cn.xu.domain.message.event.factory.MessageEventFactory;
import cn.xu.domain.message.event.handler.MessageEventHandler;
import cn.xu.domain.notification.event.NotificationEvent;
import cn.xu.domain.notification.event.NotificationEventHandler;
import cn.xu.domain.notification.event.factory.NotificationEventFactory;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Disruptor配置类
 */
@Slf4j
@Configuration
public class DisruptorConfig {

    @Resource
    private LikeEventHandler likeEventHandler;
    @Resource
    private CommentEventHandler commentEventHandler;
    @Resource
    private FollowEventHandler followEventHandler;

    @Resource
    private ArticleEventHandler articleEventHandler;

    @Resource
    private OperationLogEventHandler operationLogEventHandler;

    @Resource
    private MessageEventHandler messageEventHandler;

    @Resource
    private NotificationEventHandler notificationEventHandler;

    @Bean
    public RingBuffer<LikeEvent> likeRingBuffer() {
        EventFactory<LikeEvent> factory = LikeEvent::new;
        int bufferSize = 16384; // 16k
        ThreadFactory threadFactory = DaemonThreadFactory.INSTANCE;
        Disruptor<LikeEvent> disruptor = new Disruptor<>(
                factory,
                bufferSize,
                threadFactory,
                ProducerType.MULTI,
                new YieldingWaitStrategy()
        );

        disruptor.handleEventsWith(likeEventHandler);

        disruptor.setDefaultExceptionHandler(new ExceptionHandler<LikeEvent>() {
            @Override
            public void handleEventException(Throwable ex, long sequence, LikeEvent event) {
                log.error("LikeEvent处理异常，sequence={}, event={}", sequence, event, ex);
            }
            @Override
            public void handleOnStartException(Throwable ex) { log.error("LikeEvent启动异常", ex); }
            @Override
            public void handleOnShutdownException(Throwable ex) { log.error("LikeEvent关闭异常", ex); }
        });

        disruptor.start();
        return disruptor.getRingBuffer();
    }

    @Bean
    public RingBuffer<CommentCountEvent> commentRingBuffer() {
        // 定义事件工厂
        EventFactory<CommentCountEvent> factory = CommentCountEvent::new;

        // 环形缓冲区大小（必须是2的幂）
        int bufferSize = 1024;

        // 创建Disruptor（使用阻塞等待策略）
        Disruptor<CommentCountEvent> disruptor = new Disruptor<>(
                factory,
                bufferSize,
                Executors.defaultThreadFactory(),
                ProducerType.MULTI,  // 多生产者模式
                new BlockingWaitStrategy()
        );

        // 设置消费者组（评论成功后需要计数）
        disruptor.handleEventsWith(commentEventHandler);

        // 启动Disruptor
        disruptor.start();

        return disruptor.getRingBuffer();
    }

    @Bean
    public RingBuffer<FollowEvent> followRingBuffer() {
        // 定义事件工厂
        EventFactory<FollowEvent> factory = FollowEvent::new;

        // 环形缓冲区大小（必须是2的幂）
        int bufferSize = 1024;

        // 创建Disruptor（使用阻塞等待策略）
        Disruptor<FollowEvent> disruptor = new Disruptor<>(
                factory,
                bufferSize,
                Executors.defaultThreadFactory(),
                ProducerType.MULTI,  // 多生产者模式
                new BlockingWaitStrategy()
        );

        // 设置消费者组（评论成功后需要计数）
        disruptor.handleEventsWith(followEventHandler);

        // 启动Disruptor
        disruptor.start();

        return disruptor.getRingBuffer();
    }

    @Bean
    public RingBuffer<ArticleEventWrapper> articleEventWrapperRingBuffer() {
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

    @Bean
    public RingBuffer<ArticleEvent> articleEventRingBuffer() {
        Disruptor<ArticleEvent> disruptor = new Disruptor<>(
                new ArticleEventFactory(),
                1024 * 1024,
                DaemonThreadFactory.INSTANCE,
                ProducerType.MULTI,
                new BlockingWaitStrategy()
        );

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

    /**
     * 私信事件RingBuffer配置
     * @return
     */
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
     * 通知事件RingBuffer配置更大的缓冲区和多生产者模式，提高并发处理能力
     */
    @Bean(name = "notificationDisruptor")
    public RingBuffer<NotificationEvent> notificationDisruptor() {
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
        disruptor.handleEventsWith(notificationEventHandler);

        // 启动disruptor
        disruptor.start();

        return disruptor.getRingBuffer();
    }

}
