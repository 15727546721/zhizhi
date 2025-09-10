package cn.xu.test;

import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.service.ArticleQueryStrategy;
import cn.xu.domain.article.service.ArticleQueryStrategyFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
    classes = cn.xu.ZhizhiApplication.class,
    properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration,org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration"
    }
)
@ActiveProfiles("test") // 激活测试环境配置
public class ArticleQueryIntegrationTest {

    @Autowired
    private ArticleQueryStrategyFactory strategyFactory;

    @Test
    public void testGetCurrentStrategy() {
        ArticleQueryStrategy strategy = strategyFactory.getCurrentStrategy();
        assertNotNull(strategy, "策略不应该为null");
        System.out.println("当前使用的策略: " + strategy.getClass().getSimpleName());
    }

    @Test
    public void testSearchByTitle() {
        ArticleQueryStrategy strategy = strategyFactory.getCurrentStrategy();
        Pageable pageable = PageRequest.of(0, 10);
        Page<ArticleEntity> result = strategy.searchByTitle("test", pageable);
        assertNotNull(result, "搜索结果不应该为null");
        System.out.println("搜索结果数量: " + result.getTotalElements());
    }

    @Test
    public void testGetHotRank() {
        ArticleQueryStrategy strategy = strategyFactory.getCurrentStrategy();
        Pageable pageable = PageRequest.of(0, 10);
        Page<ArticleEntity> result = strategy.getHotRank("day", pageable);
        assertNotNull(result, "热门排行结果不应该为null");
        System.out.println("热门排行结果数量: " + result.getTotalElements());
    }
}