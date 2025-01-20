package cn.xu.infrastructure.general.config;

import cn.xu.domain.article.event.ArticleEvent;
import cn.xu.domain.article.event.DefaultArticleEvent;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文章事件Disruptor配置类
 */
@Configuration
public class ArticleEventDisruptorConfig {

    @Bean
    public RingBuffer<ArticleEvent> articleEventBuffer() {
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

    private static class ArticleEventFactory implements com.lmax.disruptor.EventFactory<ArticleEvent> {
        @Override
        public ArticleEvent newInstance() {
            return new DefaultArticleEvent();
        }
    }
} 