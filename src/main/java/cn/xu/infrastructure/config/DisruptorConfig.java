package cn.xu.infrastructure.config;

import cn.xu.domain.article.event.ArticleEventWrapper;
import cn.xu.domain.article.service.article.ArticleDomainEventHandler;
import cn.xu.domain.like.event.LikeEvent;
import cn.xu.domain.like.event.LikeEventHandler;
import cn.xu.domain.logging.event.OperationLogEvent;
import cn.xu.domain.logging.event.factory.OperationLogEventFactory;
import cn.xu.domain.logging.event.handler.OperationLogEventHandler;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Configuration
public class DisruptorConfig {

    @Resource
    private LikeEventHandler likeEventHandler;
    
    @Resource
    private ArticleDomainEventHandler articleEventHandler;

    @Resource
    private OperationLogEventHandler operationLogEventHandler;

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