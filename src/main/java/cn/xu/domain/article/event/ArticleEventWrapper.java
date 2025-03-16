package cn.xu.domain.article.event;

import com.lmax.disruptor.EventFactory;
import lombok.Getter;

/**
 * 文章领域事件包装器
 * 使用工厂模式创建实例
 */
public class ArticleEventWrapper {

    @Getter
    private ArticleEvent event;

    // 私有构造函数，防止外部直接创建实例
    private ArticleEventWrapper() {
    }

    /**
     * 设置领域事件
     *
     * @param event 领域事件
     */
    public void setEvent(ArticleEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("领域事件不能为空");
        }
        this.event = event;
    }

    /**
     * 事件包装器工厂
     * 负责创建事件包装器实例
     */
    public static class Factory {
        private static final EventFactory<ArticleEventWrapper> INSTANCE = new EventFactory<ArticleEventWrapper>() {
            @Override
            public ArticleEventWrapper newInstance() {
                return new ArticleEventWrapper();
            }
        };

        public static EventFactory<ArticleEventWrapper> getInstance() {
            return INSTANCE;
        }
    }
} 