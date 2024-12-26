package cn.xu.config.disruptor;

import cn.xu.domain.article.event.ArticleEvent;
import cn.xu.domain.article.event.ArticleEventFactory;
import cn.xu.domain.article.event.ArticleEventHandler;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

@Configuration
public class DisruptorConfig {

    @Bean
    public Disruptor<ArticleEvent> disruptor(ArticleEventFactory factory, ArticleEventHandler handler) {
        Disruptor<ArticleEvent> disruptor = new Disruptor<>(
                factory,
                1024,
                Executors.defaultThreadFactory(),
                ProducerType.SINGLE,
                new BlockingWaitStrategy()
        );

        disruptor.handleEventsWith(handler);
        disruptor.start();
        return disruptor;
    }
}