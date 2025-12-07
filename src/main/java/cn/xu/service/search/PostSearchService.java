package cn.xu.service.search;

import cn.xu.common.ResponseCode;
import cn.xu.integration.search.strategy.ElasticsearchSearchStrategy;
import cn.xu.integration.search.strategy.MysqlSearchStrategy;
import cn.xu.model.dto.search.SearchFilter;
import cn.xu.model.entity.Post;
import cn.xu.model.entity.User;
import cn.xu.model.vo.post.PostSearchResponseVO;
import java.util.List;
import cn.xu.service.user.IUserService;
import cn.xu.support.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 帖子搜索服务（支持ES/MySQL自动降级）
 */
@Slf4j
@Service
public class PostSearchService {

    // ==================== 依赖注入 ====================

    // 必需依赖 - 构造器注入
    private final MysqlSearchStrategy mysqlStrategy;
    private final IUserService userService;

    // 可选依赖 - setter 注入
    private ElasticsearchSearchStrategy esStrategy;
    private SearchStatisticsService statisticsService;
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${app.post.query.strategy:auto}")
    private String preferredStrategy; // auto/elasticsearch/mysql

    @Value("${app.post.query.cache.enabled:true}")
    private boolean cacheEnabled;

    @Value("${app.post.query.cache.ttl:300}")
    private int cacheTtlSeconds; // 缓存失效时间，单位秒

    private ISearchStrategy currentStrategy;
    private boolean esAvailable = false;

    // ==================== 构造方法 ====================

    public PostSearchService(
            MysqlSearchStrategy mysqlStrategy,
            IUserService userService
    ) {
        this.mysqlStrategy = mysqlStrategy;
        this.userService = userService;
    }

    // ==================== Setter 注入 ====================

    @Autowired(required = false)
    public void setEsStrategy(ElasticsearchSearchStrategy esStrategy) {
        this.esStrategy = esStrategy;
    }

    @Autowired(required = false)
    public void setStatisticsService(SearchStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Autowired(required = false)
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ==================== 初始化方法 ====================

    @PostConstruct
    public void init() {
        esAvailable = (esStrategy != null && esStrategy.isAvailable());
        selectStrategy();

        log.info("PostSearchService 初始化成功");
        log.info("  - ES可用: {}", esAvailable);
        log.info("  - 配置策略: {}", preferredStrategy);
        log.info("  - 当前策略: {}", currentStrategy.getStrategyName());
        log.info("  - 缓存启用: {}", cacheEnabled);
    }

    private void selectStrategy() {
        if ("mysql".equals(preferredStrategy)) {
            currentStrategy = mysqlStrategy;
            log.info("使用MySQL搜索策略");
            return;
        }

        if ("elasticsearch".equals(preferredStrategy) && esAvailable) {
            currentStrategy = esStrategy;
            log.info("使用Elasticsearch搜索策略");
            return;
        }

        // auto模式，优先选择ES，如果不可用，则退回MySQL
        if (esAvailable) {
            currentStrategy = esStrategy;
            log.info("自动选择Elasticsearch搜索策略");
        } else {
            currentStrategy = mysqlStrategy;
            log.info("自动选择MySQL搜索策略");
        }
    }

    // ==================== 搜索查询 ====================

    /**
     * 执行搜索查询，并支持缓存
     *
     * @param keyword 搜索关键字
     * @param filter 搜索过滤器
     * @param pageable 分页参数
     * @return 搜索结果
     */
    public Page<Post> search(String keyword, SearchFilter filter, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("搜索关键字不能为空");
        }

        String normalizedKeyword = normalizeKeyword(keyword);

        // 1. 检查缓存
        if (cacheEnabled && redisTemplate != null) {
            Page<Post> cached = getFromCache(normalizedKeyword, filter, pageable);
            if (cached != null) {
                log.debug("使用缓存查询: keyword={}", normalizedKeyword);
                return cached;
            }
        }

        // 2. 执行搜索查询
        Page<Post> result = searchWithFallback(normalizedKeyword, filter, pageable);

        // 3. 保存缓存
        if (cacheEnabled && redisTemplate != null && result != null) {
            saveToCache(normalizedKeyword, filter, pageable, result);
        }

        // 4. 记录搜索统计
        recordSearchStatistics(normalizedKeyword, result);

        return result;
    }

    /**
     * 执行搜索查询，并支持降级
     */
    private Page<Post> searchWithFallback(String keyword, SearchFilter filter, Pageable pageable) {
        try {
            // 使用当前策略进行搜索
            Page<Post> result = currentStrategy.search(keyword, filter, pageable);
            log.debug("搜索成功: strategy={}, keyword={}, total={}",
                    currentStrategy.getStrategyName(), keyword, result.getTotalElements());
            return result;

        } catch (Exception e) {
            log.error("搜索失败: strategy={}, keyword={}", currentStrategy.getStrategyName(), keyword, e);

            // 如果ES策略失败，尝试回退到MySQL
            if (currentStrategy == esStrategy && mysqlStrategy.isAvailable()) {
                log.warn("ES搜索失败，回退到MySQL: keyword={}", keyword);
                try {
                    Page<Post> result = mysqlStrategy.search(keyword, filter, pageable);
                    log.info("MySQL搜索成功: keyword={}, total={}", keyword, result.getTotalElements());

                    // 禁用ES并切换为MySQL
                    esAvailable = false;
                    currentStrategy = mysqlStrategy;

                    return result;
                } catch (Exception fallbackException) {
                    log.error("MySQL搜索失败: keyword={}", keyword, fallbackException);
                    throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "搜索服务失败");
                }
            }

            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "搜索失败: " + e.getMessage());
        }
    }

    // ==================== 缓存处理 ====================

    private Page<Post> getFromCache(String keyword, SearchFilter filter, Pageable pageable) {
        try {
            String cacheKey = buildCacheKey(keyword, filter, pageable);
            return (Page<Post>) redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            log.warn("获取缓存失败: keyword={}", keyword, e);
            return null;
        }
    }

    private void saveToCache(String keyword, SearchFilter filter, Pageable pageable, Page<Post> result) {
        try {
            String cacheKey = buildCacheKey(keyword, filter, pageable);
            redisTemplate.opsForValue().set(cacheKey, result, cacheTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("保存缓存失败: keyword={}", keyword, e);
        }
    }

    private String buildCacheKey(String keyword, SearchFilter filter, Pageable pageable) {
        StringBuilder sb = new StringBuilder("post:search:");
        sb.append(keyword);

        if (filter != null) {
            if (filter.getStartTime() != null) {
                sb.append(":start:").append(filter.getStartTime());
            }
            if (filter.getEndTime() != null) {
                sb.append(":end:").append(filter.getEndTime());
            }
            if (filter.getSortOption() != null) {
                sb.append(":sort:").append(filter.getSortOption());
            }
        }

        sb.append(":page:").append(pageable.getPageNumber());
        sb.append(":size:").append(pageable.getPageSize());

        return sb.toString();
    }

    // ==================== 搜索统计 ====================

    private void recordSearchStatistics(String keyword, Page<Post> result) {
        if (statisticsService == null) {
            return;
        }
        try {
            long count = result != null ? result.getTotalElements() : 0;
            statisticsService.recordSearch(keyword, count, count > 0);
        } catch (Exception e) {
            log.warn("记录搜索统计失败: keyword={}", keyword, e);
        }
    }

    // ==================== 辅助方法 ====================

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }
        return keyword.trim();
    }

    // ==================== 对外接口 ====================

    public String getCurrentStrategyName() {
        return currentStrategy != null ? currentStrategy.getStrategyName() : "none";
    }

    public boolean isEsAvailable() {
        return esAvailable;
    }

    public void switchStrategy(String strategyName) {
        if ("elasticsearch".equals(strategyName) && esAvailable) {
            currentStrategy = esStrategy;
            log.info("切换到Elasticsearch策略");
        } else if ("mysql".equals(strategyName)) {
            currentStrategy = mysqlStrategy;
            log.info("切换到MySQL策略");
        } else {
            throw new IllegalArgumentException("策略无效: " + strategyName);
        }
    }

    // ==================== 搜索扩展方法 ====================

    /**
     * 执行搜索并返回搜索结果
     *
     * @param keyword 搜索关键字
     * @param filter 搜索过滤器
     * @param page 页码
     * @param size 每页数量
     * @return 搜索结果
     */
    public SearchResult executeSearch(String keyword, SearchFilter filter, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Post> result = search(keyword, filter, pageable);
        
        // 转换为VO列表
        List<PostSearchResponseVO> postVOs = result.getContent().stream()
                .map(post -> {
                    PostSearchResponseVO vo = new PostSearchResponseVO();
                    vo.setId(post.getId());
                    vo.setTitle(post.getTitle());
                    vo.setDescription(post.getDescription());
                    vo.setViewCount(post.getViewCount());
                    vo.setLikeCount(post.getLikeCount());
                    vo.setCommentCount(post.getCommentCount());
                    vo.setCreateTime(post.getCreateTime());
                    vo.setUserId(post.getUserId());
                    
                    // 获取用户信息
                    try {
                        User user = userService.getUserById(post.getUserId());
                        if (user != null) {
                            vo.setAuthorName(user.getNickname());
                            vo.setAvatar(user.getAvatar());
                        }
                    } catch (Exception e) {
                        log.warn("获取用户信息失败: userId={}", post.getUserId());
                    }
                    
                    return vo;
                })
                .collect(java.util.stream.Collectors.toList());
        
        return new SearchResult(page, size, result.getTotalElements(), postVOs);
    }

    /**
     * 获取搜索建议
     *
     * @param keyword 关键词前缀
     * @param limit 返回数量
     * @return 建议列表
     */
    public List<String> getSearchSuggestions(String keyword, int limit) {
        if (statisticsService == null) {
            return Collections.emptyList();
        }
        return statisticsService.getSearchSuggestions(keyword, limit);
    }

    /**
     * 获取热门搜索词
     *
     * @param limit 返回数量
     * @return 热词列表
     */
    public List<String> getHotKeywords(int limit) {
        if (statisticsService == null) {
            return Collections.emptyList();
        }
        return statisticsService.getHotKeywords(limit);
    }

    /**
     * 获取搜索统计
     *
     * @param date 日期
     * @return 统计信息
     */
    public SearchStatisticsService.SearchStatistics getSearchStatistics(String date) {
        if (statisticsService == null) {
            return null;
        }
        return statisticsService.getSearchStatistics(date);
    }

    /**
     * 获取热门搜索词详情（带搜索次数）
     *
     * @param limit 返回数量
     * @return 热词详情列表
     */
    public List<SearchStatisticsService.HotKeyword> getHotKeywordsDetailed(int limit) {
        if (statisticsService == null) {
            return Collections.emptyList();
        }
        return statisticsService.getHotKeywordsWithCount(limit);
    }

    // ==================== 内部类 ====================

    /**
     * 搜索结果
     */
    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class SearchResult {
        private final int page;
        private final int size;
        private final long total;
        private final List<PostSearchResponseVO> posts;
    }
}
