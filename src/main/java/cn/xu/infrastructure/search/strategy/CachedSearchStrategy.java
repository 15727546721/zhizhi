package cn.xu.infrastructure.search.strategy;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.search.model.policy.ISearchStrategy;
import cn.xu.domain.search.model.valobj.SearchFilter;
import cn.xu.infrastructure.cache.RedisKeyManager;
import cn.xu.infrastructure.search.model.CachedSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 缓存搜索策略装饰器
 */
@Slf4j
@RequiredArgsConstructor
public class CachedSearchStrategy implements ISearchStrategy {

    private final ISearchStrategy delegate;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final int HOT_KEYWORD_CACHE_TTL = 3600;
    private static final int NORMAL_KEYWORD_CACHE_TTL = 300;
    private static final int EMPTY_RESULT_CACHE_TTL = 60;
    private static final int HOT_EMPTY_RESULT_CACHE_TTL = 300;
    private static final int HOT_KEYWORD_THRESHOLD = 10;
    
    private final java.util.Random random = new java.util.Random();

    @Override
    public Page<PostEntity> search(String keyword, Pageable pageable) {
        return search(keyword, null, pageable);
    }

    @Override
    public Page<PostEntity> search(String keyword, SearchFilter filter, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return delegate.search(keyword, filter, pageable);
        }

        String normalizedKeyword = keyword.trim();
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();

        String filterHash = generateFilterHash(filter);
        String cacheKey = RedisKeyManager.postSearchResultKey(normalizedKeyword, filterHash, page, size);
        
        try {
            Page<PostEntity> cachedResult = getFromCache(cacheKey);
            if (cachedResult != null) {
                log.debug("从缓存获取搜索结果: keyword={}", normalizedKeyword);
                recordHotKeyword(normalizedKeyword);
                return cachedResult;
            }
        } catch (Exception e) {
            log.warn("从缓存获取搜索结果失败: keyword={}", normalizedKeyword, e);
        }

        String emptyCacheKey = cacheKey + ":empty";
        try {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(emptyCacheKey))) {
                return new PageImpl<>(Collections.emptyList(), pageable, 0);
            }
        } catch (Exception e) {
            log.warn("检查空结果缓存失败: keyword={}", normalizedKeyword, e);
        }

        Page<PostEntity> result = delegate.search(normalizedKeyword, filter, pageable);

        try {
            if (result.getContent().isEmpty()) {
                int baseEmptyTtl = isHotKeyword(normalizedKeyword) ? HOT_EMPTY_RESULT_CACHE_TTL : EMPTY_RESULT_CACHE_TTL;
                int emptyTtl = baseEmptyTtl + random.nextInt(baseEmptyTtl / 10) - (baseEmptyTtl / 20);
                cacheEmptyResult(emptyCacheKey, Math.max(emptyTtl, 1));
            } else {
                int baseTtl = isHotKeyword(normalizedKeyword) ? HOT_KEYWORD_CACHE_TTL : NORMAL_KEYWORD_CACHE_TTL;
                int ttl = baseTtl + random.nextInt(baseTtl / 10) - (baseTtl / 20);
                cacheResult(cacheKey, result, Math.max(ttl, 1));
                recordHotKeyword(normalizedKeyword);
            }
        } catch (Exception e) {
            log.warn("缓存搜索结果失败: keyword={}", normalizedKeyword, e);
        }

        return result;
    }
    
    private String generateFilterHash(SearchFilter filter) {
        if (filter == null) {
            return "";
        }
        
        List<String> typeCodes = null;
        if (filter.getTypes() != null && !filter.getTypes().isEmpty()) {
            typeCodes = filter.getTypes().stream()
                    .map(type -> type.getCode())
                    .sorted()
                    .collect(Collectors.toList());
        }
        
        String timeRange = null;
        if (filter.getStartTime() != null || filter.getEndTime() != null) {
            StringBuilder timeRangeBuilder = new StringBuilder();
            if (filter.getStartTime() != null) {
                timeRangeBuilder.append("start:").append(filter.getStartTime().toString());
            }
            if (filter.getEndTime() != null) {
                if (timeRangeBuilder.length() > 0) {
                    timeRangeBuilder.append("|");
                }
                timeRangeBuilder.append("end:").append(filter.getEndTime().toString());
            }
            timeRange = timeRangeBuilder.toString();
        }
        
        String sortOption = null;
        if (filter.getSortOption() != null) {
            sortOption = filter.getSortOption().name().toLowerCase();
        }
        
        return RedisKeyManager.generateFilterHash(typeCodes, timeRange, sortOption);
    }

    @Override
    public boolean isAvailable() {
        return delegate.isAvailable();
    }

    @Override
    public String getStrategyName() {
        return "cached-" + delegate.getStrategyName();
    }

    private Page<PostEntity> getFromCache(String cacheKey) {
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof CachedSearchResult) {
                CachedSearchResult cachedResult = (CachedSearchResult) cached;
                Pageable pageable = org.springframework.data.domain.PageRequest.of(
                        cachedResult.getPageNumber(), 
                        cachedResult.getPageSize());
                return new PageImpl<>(
                        cachedResult.getContent() != null ? cachedResult.getContent() : Collections.emptyList(),
                        pageable,
                        cachedResult.getTotalElements());
            }
        } catch (Exception e) {
            log.error("从缓存获取搜索结果失败: key={}", cacheKey, e);
        }
        return null;
    }

    private void cacheResult(String cacheKey, Page<PostEntity> result, int ttl) {
        try {
            CachedSearchResult cachedResult = new CachedSearchResult(
                    result.getContent(),
                    result.getTotalElements(),
                    result.getNumber(),
                    result.getSize(),
                    result.getTotalPages()
            );
            redisTemplate.opsForValue().set(cacheKey, cachedResult, ttl, TimeUnit.SECONDS);
            log.debug("缓存搜索结果成功: key={}, ttl={}s", cacheKey, ttl);
        } catch (Exception e) {
            log.error("缓存搜索结果失败: key={}", cacheKey, e);
        }
    }

    private void cacheEmptyResult(String emptyCacheKey, int ttl) {
        try {
            redisTemplate.opsForValue().set(emptyCacheKey, "1", ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("缓存空结果失败: key={}", emptyCacheKey, e);
        }
    }

    private boolean isHotKeyword(String keyword) {
        try {
            String hotKeywordsKey = RedisKeyManager.postSearchHotKeywordsKey();
            Double score = redisTemplate.opsForZSet().score(hotKeywordsKey, keyword);
            return score != null && score >= HOT_KEYWORD_THRESHOLD;
        } catch (Exception e) {
            log.warn("判断热门关键词失败: keyword={}", keyword, e);
            return false;
        }
    }

    private void recordHotKeyword(String keyword) {
        try {
            String hotKeywordsKey = RedisKeyManager.postSearchHotKeywordsKey();
            redisTemplate.opsForZSet().incrementScore(hotKeywordsKey, keyword, 1);
            redisTemplate.expire(hotKeywordsKey, 7, TimeUnit.DAYS);
            redisTemplate.opsForZSet().removeRange(hotKeywordsKey, 0, -1001);
        } catch (Exception e) {
            log.warn("记录热门搜索关键词失败: keyword={}", keyword, e);
        }
    }
}

