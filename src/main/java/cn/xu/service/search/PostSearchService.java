package cn.xu.service.search;

import cn.xu.common.ResponseCode;
import cn.xu.integration.search.strategy.ElasticsearchSearchStrategy;
import cn.xu.integration.search.strategy.MysqlSearchStrategy;
import cn.xu.model.dto.search.SearchFilter;
import cn.xu.model.entity.Post;
import cn.xu.model.entity.User;
import cn.xu.model.vo.post.PostSearchResponseVO;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 帖子搜索服务（统一入口）
 *
 * 功能：
 * 1. 搜索查询（ES优先，MySQL兜底）
 * 2. 索引管理（存储、更新、删除）
 * 3. 搜索统计（热词、建议）
 * 4. 热度排序
 * 5. 自动降级（ES故障时自动切换MySQL）
 *
 * 架构：
 * Controller → PostSearchService → [ES策略 / MySQL策略]
 */
@Slf4j
@Service
public class PostSearchService {

    // ==================== 依赖注入 ====================

    @Autowired(required = false)
    private ElasticsearchSearchStrategy esStrategy;

    @Autowired
    private MysqlSearchStrategy mysqlStrategy;

    @Autowired(required = false)
    private ISearchStatisticsService statisticsService;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private IUserService userService;

    @Value("${app.post.query.strategy:auto}")
    private String preferredStrategy; // auto/elasticsearch/mysql

    @Value("${app.post.query.cache.enabled:true}")
    private boolean cacheEnabled;

    @Value("${app.post.query.cache.ttl:300}")
    private int cacheTtlSeconds; // 缓存过期时间（秒）

    private ISearchStrategy currentStrategy;
    private boolean esAvailable = false;

    // ==================== 初始化 ====================

    @PostConstruct
    public void init() {
        esAvailable = (esStrategy != null && esStrategy.isAvailable());
        selectStrategy();

        log.info("PostSearchService初始化完成");
        log.info("  - ES可用: {}", esAvailable);
        log.info("  - 配置策略: {}", preferredStrategy);
        log.info("  - 当前策略: {}", currentStrategy.getStrategyName());
        log.info("  - 缓存启用: {}", cacheEnabled);
    }

    private void selectStrategy() {
        if ("mysql".equals(preferredStrategy)) {
            currentStrategy = mysqlStrategy;
            log.info("使用MySQL搜索策略（配置强制）");
            return;
        }

        if ("elasticsearch".equals(preferredStrategy) && esAvailable) {
            currentStrategy = esStrategy;
            log.info("使用Elasticsearch搜索策略（配置强制）");
            return;
        }

        // auto模式：ES优先，MySQL兜底
        if (esAvailable) {
            currentStrategy = esStrategy;
            log.info("使用Elasticsearch搜索策略（自动选择）");
        } else {
            currentStrategy = mysqlStrategy;
            log.info("使用MySQL搜索策略（ES不可用，自动降级）");
        }
    }

    // ==================== 搜索查询 ====================

    /**
     * 搜索帖子（带缓存和自动降级）
     *
     * @param keyword 搜索关键词
     * @param filter 过滤条件
     * @param pageable 分页参数
     * @return 搜索结果
     */
    public Page<Post> search(String keyword, SearchFilter filter, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }

        String normalizedKeyword = normalizeKeyword(keyword);

        // 1. 尝试从缓存获取
        if (cacheEnabled && redisTemplate != null) {
            Page<Post> cached = getFromCache(normalizedKeyword, filter, pageable);
            if (cached != null) {
                log.debug("命中搜索缓存: keyword={}", normalizedKeyword);
                return cached;
            }
        }

        // 2. 执行搜索（带自动降级）
        Page<Post> result = searchWithFallback(normalizedKeyword, filter, pageable);

        // 3. 写入缓存
        if (cacheEnabled && redisTemplate != null && result != null) {
            saveToCache(normalizedKeyword, filter, pageable, result);
        }

        // 4. 记录搜索统计
        recordSearchStatistics(normalizedKeyword, result);

        return result;
    }

    /**
     * 搜索帖子（带自动降级）
     */
    private Page<Post> searchWithFallback(String keyword, SearchFilter filter, Pageable pageable) {
        try {
            // 优先使用当前策略
            Page<Post> result = currentStrategy.search(keyword, filter, pageable);
            log.debug("搜索成功: strategy={}, keyword={}, total={}",
                    currentStrategy.getStrategyName(), keyword, result.getTotalElements());
            return result;

        } catch (Exception e) {
            log.error("搜索失败: strategy={}, keyword={}", currentStrategy.getStrategyName(), keyword, e);

            // 如果是ES策略失败，尝试降级到MySQL
            if (currentStrategy == esStrategy && mysqlStrategy.isAvailable()) {
                log.warn("ES搜索失败，自动降级到MySQL: keyword={}", keyword);
                try {
                    Page<Post> result = mysqlStrategy.search(keyword, filter, pageable);
                    log.info("MySQL降级搜索成功: keyword={}, total={}", keyword, result.getTotalElements());

                    // 标记ES不可用
                    esAvailable = false;
                    currentStrategy = mysqlStrategy;

                    return result;
                } catch (Exception fallbackException) {
                    log.error("MySQL降级搜索也失败: keyword={}", keyword, fallbackException);
                    throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "搜索服务不可用");
                }
            }

            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "搜索失败: " + e.getMessage());
        }
    }

    // ==================== 索引管理 ====================

    /**
     * 索引帖子（仅ES）
     */
    public void indexPost(Post post) {
        if (esStrategy == null || !esAvailable) {
            log.debug("ES不可用，跳过索引: postId={}", post != null ? post.getId() : null);
            return;
        }
        esStrategy.indexPost(post);
    }

    /**
     * 索引帖子（带重试，仅ES）
     */
    public boolean indexPostWithRetry(Post post) {
        if (esStrategy == null || !esAvailable) {
            log.debug("ES不可用，跳过索引: postId={}", post != null ? post.getId() : null);
            return false;
        }
        return esStrategy.indexPostWithRetry(post);
    }

    /**
     * 更新索引（仅ES）
     */
    public void updateIndex(Post post) {
        if (esStrategy == null || !esAvailable) {
            log.debug("ES不可用，跳过更新索引: postId={}", post != null ? post.getId() : null);
            return;
        }
        esStrategy.updateIndexedPost(post);
    }

    /**
     * 删除索引（仅ES）
     */
    public void removeIndex(Long postId) {
        if (esStrategy == null || !esAvailable) {
            log.debug("ES不可用，跳过删除索引: postId={}", postId);
            return;
        }
        esStrategy.removeIndexedPost(postId);
    }

    // ==================== 热度排序 ====================

    /**
     * 获取热度排序（日榜、周榜、月榜）
     */
    public Page<Post> getHotRank(String rankType, Pageable pageable) {
        if (esStrategy == null || !esAvailable) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "热度排序功能需要Elasticsearch支持");
        }
        return esStrategy.getHotRank(rankType, pageable);
    }

    // ==================== 搜索统计 ====================

    /**
     * 获取搜索建议
     */
    public List<String> getSearchSuggestions(String keyword, int limit) {
        if (statisticsService == null) {
            return java.util.Collections.emptyList();
        }
        return statisticsService.getSearchSuggestions(keyword, limit);
    }

    /**
     * 获取热门关键词
     */
    public List<String> getHotKeywords(int limit) {
        if (statisticsService == null) {
            return java.util.Collections.emptyList();
        }
        return statisticsService.getHotKeywords(limit);
    }

    /**
     * 获取搜索统计
     */
    public ISearchStatisticsService.SearchStatistics getSearchStatistics(String date) {
        if (statisticsService == null) {
            return null;
        }
        return statisticsService.getSearchStatistics(date);
    }

    /**
     * 获取热门关键词详情
     */
    public List<ISearchStatisticsService.HotKeyword> getHotKeywordsDetailed(int limit) {
        if (statisticsService == null) {
            return java.util.Collections.emptyList();
        }
        return statisticsService.getHotKeywordsWithCount(limit);
    }

    // ==================== 私有方法 ====================

    /**
     * 关键词标准化
     */
    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }
        return keyword.trim();
    }

    /**
     * 记录搜索统计
     */
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

    /**
     * 从缓存获取搜索结果
     */
    @SuppressWarnings("unchecked")
    private Page<Post> getFromCache(String keyword, SearchFilter filter, Pageable pageable) {
        try {
            String cacheKey = buildCacheKey(keyword, filter, pageable);
            return (Page<Post>) redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            log.warn("从缓存获取失败: keyword={}", keyword, e);
            return null;
        }
    }

    /**
     * 保存搜索结果到缓存
     */
    private void saveToCache(String keyword, SearchFilter filter, Pageable pageable, Page<Post> result) {
        try {
            String cacheKey = buildCacheKey(keyword, filter, pageable);
            redisTemplate.opsForValue().set(cacheKey, result, cacheTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("写入缓存失败: keyword={}", keyword, e);
        }
    }

    /**
     * 构建缓存Key
     */
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

    // ==================== 健康检查 ====================

    /**
     * 获取当前使用的策略
     */
    public String getCurrentStrategyName() {
        return currentStrategy != null ? currentStrategy.getStrategyName() : "none";
    }

    /**
     * ES是否可用
     */
    public boolean isEsAvailable() {
        return esAvailable;
    }

    /**
     * 手动切换策略（调试用）
     */
    public void switchStrategy(String strategyName) {
        if ("elasticsearch".equals(strategyName) && esAvailable) {
            currentStrategy = esStrategy;
            log.info("手动切换到ES策略");
        } else if ("mysql".equals(strategyName)) {
            currentStrategy = mysqlStrategy;
            log.info("手动切换到MySQL策略");
        } else {
            throw new IllegalArgumentException("策略不存在或不可用: " + strategyName);
        }
    }

    /**
     * 执行搜索
     *
     * @param keyword 搜索关键词
     * @param filter 过滤条件
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 搜索结果
     */
    public SearchResult executeSearch(String keyword, SearchFilter filter, int page, int size) {
        long startTime = System.currentTimeMillis();

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }

        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword.length() > 100) {
            throw new IllegalArgumentException("搜索关键词长度不能超过100个字符");
        }

        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, Math.min(size, 100));

        try {
            // 执行搜索
            Pageable pageable = PageRequest.of(safePage - 1, safeSize);
            Page<Post> postsPage = search(normalizedKeyword, filter, pageable);

            // 转换为PostSearchResponseVO
            List<PostSearchResponseVO> searchResponses = convertToPostSearchResponseVOs(postsPage.getContent());

            long responseTime = System.currentTimeMillis() - startTime;
            log.info("搜索完成: keyword={}, total={}, results={}, responseTime={}ms",
                    normalizedKeyword, postsPage.getTotalElements(), searchResponses.size(), responseTime);

            return SearchResult.builder()
                    .posts(searchResponses)
                    .total(postsPage.getTotalElements())
                    .page(safePage)
                    .size(safeSize)
                    .build();
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("搜索失败: keyword={}, responseTime={}ms", normalizedKeyword, responseTime, e);
            throw e;
        }
    }

    /**
     * 将Post列表转换为PostSearchResponseVO列表
     */
    private List<PostSearchResponseVO> convertToPostSearchResponseVOs(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // 收集用户ID
        Set<Long> userIds = posts.stream()
                .map(Post::getUserId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        // 批量获取用户信息
        List<User> users = new java.util.ArrayList<>();
        try {
            users = userService.batchGetUserInfo(userIds.stream().collect(Collectors.toList()));
        } catch (Exception e) {
            log.warn("批量获取用户信息失败", e);
        }

        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user, (existing, replacement) -> existing));

        // 转换为PostSearchResponseVO
        return posts.stream()
                .map(post -> {
                    User user = post.getUserId() != null ? userMap.get(post.getUserId()) : null;

                    String authorName = "匿名用户";
                    if (user != null) {
                        if (user.getNickname() != null && !user.getNickname().trim().isEmpty()) {
                            authorName = user.getNickname();
                        } else if (user.getUsername() != null) {
                            authorName = user.getUsername();
                        }
                    }

                    String avatar = user != null ? user.getAvatar() : null;
                    String summary = generateSummary(post.getDescription(), post.getContent());

                    return PostSearchResponseVO.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .description(post.getDescription())
                            .content(summary)
                            .coverUrl(post.getCoverUrl())
                            .userId(post.getUserId())
                            .authorName(authorName)
                            .avatar(avatar)
                            .viewCount(post.getViewCount() != null ? post.getViewCount() : 0L)
                            .likeCount(post.getLikeCount() != null ? post.getLikeCount() : 0L)
                            .commentCount(post.getCommentCount() != null ? post.getCommentCount() : 0L)
                            .favoriteCount(post.getFavoriteCount() != null ? post.getFavoriteCount() : 0L)
                            .shareCount(post.getShareCount() != null ? post.getShareCount() : 0L)
                            .isFeatured(post.getIsFeatured() != null && post.getIsFeatured() != 0)
                            .createTime(post.getCreateTime())
                            .updateTime(post.getUpdateTime())
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 生成帖子摘要
     */
    private String generateSummary(String description, String content) {
        if (description != null && !description.trim().isEmpty()) {
            return description.length() > 200 ? description.substring(0, 200) + "..." : description;
        }

        if (content != null && !content.trim().isEmpty()) {
            String plainText = content.replaceAll("<[^>]+>", "").trim();
            if (plainText.length() > 200) {
                return plainText.substring(0, 200) + "...";
            }
            return plainText;
        }

        return "";
    }

    /**
     * 搜索结果包装类
     */
    @lombok.Data
    @lombok.Builder
    public static class SearchResult {
        private List<PostSearchResponseVO> posts;
        private long total;
        private int page;
        private int size;
    }
}