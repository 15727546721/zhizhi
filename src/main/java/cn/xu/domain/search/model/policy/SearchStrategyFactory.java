package cn.xu.domain.search.model.policy;

import cn.xu.infrastructure.search.strategy.CachedSearchStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 搜索策略工厂
 */
@Slf4j
@Component
public class SearchStrategyFactory {

    @Value("${app.post.query.strategy:mysql}")
    private String strategyName;

    @Value("${app.post.query.cache.enabled:true}")
    private boolean cacheEnabled;

    private final List<ISearchStrategy> strategies;
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    private Map<String, ISearchStrategy> strategyMap;
    private ISearchStrategy defaultStrategy;

    @Autowired(required = false)
    public SearchStrategyFactory(List<ISearchStrategy> strategies) {
        this.strategies = strategies;
    }

    @PostConstruct
    public void init() {
        if (strategies == null || strategies.isEmpty()) {
            log.warn("未找到任何搜索策略实现");
            return;
        }

        strategyMap = strategies.stream()
                .filter(s -> !s.getStrategyName().startsWith("cached-"))
                .filter(ISearchStrategy::isAvailable)
                .collect(Collectors.toMap(
                        ISearchStrategy::getStrategyName,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        log.info("可用的搜索策略: {}", strategyMap.keySet());
        selectDefaultStrategy();
    }

    public ISearchStrategy getStrategy() {
        if (defaultStrategy == null) {
            throw new IllegalStateException("没有可用的搜索策略");
        }
        
        if (cacheEnabled && redisTemplate != null && !(defaultStrategy instanceof CachedSearchStrategy)) {
            return new CachedSearchStrategy(defaultStrategy, redisTemplate);
        }
        
        return defaultStrategy;
    }

    public ISearchStrategy getStrategy(String strategyName) {
        ISearchStrategy strategy = strategyMap.get(strategyName);
        if (strategy == null) {
            log.warn("策略 {} 不存在或不可用，使用默认策略", strategyName);
            return getStrategy();
        }
        return strategy;
    }

    private void selectDefaultStrategy() {
        if (strategyName != null && strategyMap.containsKey(strategyName)) {
            defaultStrategy = strategyMap.get(strategyName);
            log.info("使用配置的搜索策略: {}", strategyName);
            return;
        }

        if (strategyMap.containsKey("elasticsearch")) {
            defaultStrategy = strategyMap.get("elasticsearch");
            log.info("使用Elasticsearch搜索策略");
        } else if (strategyMap.containsKey("mysql")) {
            defaultStrategy = strategyMap.get("mysql");
            log.info("使用MySQL搜索策略");
        } else {
            throw new IllegalStateException("没有可用的搜索策略");
        }
    }

    public List<String> getAvailableStrategies() {
        return strategyMap.keySet().stream().collect(Collectors.toList());
    }
}

