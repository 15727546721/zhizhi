package cn.xu.integration.search.service;

import cn.xu.cache.RedisKeyManager;
import cn.xu.cache.core.RedisOperations;
import cn.xu.service.search.SearchStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 搜索统计服务实现
 * <p>记录搜索行为、统计热门关键词、提供搜索建议</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchStatisticsServiceImpl implements SearchStatisticsService {

    private final RedisOperations redisOps;
    private static final int ANTI_SPAM_WINDOW_SECONDS = 60;

    @Override
    public void recordSearch(String keyword, long resultCount, boolean hasResults) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        try {
            String normalizedKeyword = keyword.trim().toLowerCase();

            String antiSpamKey = "post:search:antispam:" + normalizedKeyword;
            boolean setSuccess = redisOps.setIfAbsent(antiSpamKey, "1", ANTI_SPAM_WINDOW_SECONDS);

            if (!setSuccess) {
                log.debug("[搜索] 搜索统计已记录（防刷）- keyword: {}", normalizedKeyword);
                return;
            }

            String hotKeywordsKey = RedisKeyManager.postSearchHotKeywordsKey();
            redisOps.zIncrementScore(hotKeywordsKey, normalizedKeyword, 1);
            redisOps.expire(hotKeywordsKey, 7 * 24 * 3600);

            String today = LocalDateTime.now().toLocalDate().toString();
            String searchStatsKey = "post:search:stats:" + today;

            redisOps.increment(searchStatsKey + ":total", 1);
            if (hasResults) {
                redisOps.increment(searchStatsKey + ":success", 1);
            } else {
                redisOps.increment(searchStatsKey + ":empty", 1);
            }

            redisOps.expire(searchStatsKey + ":total", 30 * 24 * 3600);
            redisOps.expire(searchStatsKey + ":success", 30 * 24 * 3600);
            redisOps.expire(searchStatsKey + ":empty", 30 * 24 * 3600);
        } catch (Exception e) {
            log.warn("[搜索] 记录搜索统计失败 - keyword: {}", keyword, e);
        }
    }

    @Override
    public SearchStatistics getSearchStatistics(String date) {
        if (date == null || date.isEmpty()) {
            date = LocalDateTime.now().toLocalDate().toString();
        }

        try {
            String searchStatsKey = "post:search:stats:" + date;

            Object totalObj = redisOps.get(searchStatsKey + ":total");
            Object successObj = redisOps.get(searchStatsKey + ":success");
            Object emptyObj = redisOps.get(searchStatsKey + ":empty");

            long total = totalObj != null ? Long.parseLong(totalObj.toString()) : 0;
            long success = successObj != null ? Long.parseLong(successObj.toString()) : 0;
            long empty = emptyObj != null ? Long.parseLong(emptyObj.toString()) : 0;

            return new SearchStatisticsImpl(date, total, success, empty, total > 0 ? (double) success / total : 0.0);
        } catch (Exception e) {
            log.error("[搜索] 获取搜索统计失败 - date: {}", date, e);
            return new SearchStatisticsImpl(date, 0, 0, 0, 0.0);
        }
    }

    @Override
    public List<HotKeyword> getHotKeywordsWithCount(int limit) {
        try {
            String hotKeywordsKey = RedisKeyManager.postSearchHotKeywordsKey();
            Set<ZSetOperations.TypedTuple<Object>> hotKeywords =
                    redisOps.getRedisTemplate().opsForZSet().reverseRangeWithScores(hotKeywordsKey, 0, limit - 1);

            List<HotKeyword> result = new ArrayList<>();
            if (hotKeywords != null && !hotKeywords.isEmpty()) {
                for (ZSetOperations.TypedTuple<Object> tuple : hotKeywords) {
                    if (tuple != null && tuple.getValue() != null) {
                        String keyword = tuple.getValue().toString();
                        if (keyword != null && !keyword.trim().isEmpty()) {
                            Double score = tuple.getScore();
                            result.add(new HotKeywordImpl(keyword, score != null ? score.longValue() : 0));
                        }
                    }
                }
            }

            return result;
        } catch (Exception e) {
            log.error("[搜索] 获取热门搜索词失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> getHotKeywords(int limit) {
        try {
            if (limit <= 0) {
                limit = 10;
            }
            if (limit > 100) {
                limit = 100;
            }

            String hotKeywordsKey = RedisKeyManager.postSearchHotKeywordsKey();
            Set<Object> hotKeywords = redisOps.zReverseRange(hotKeywordsKey, 0, limit - 1);

            List<String> result = new ArrayList<>();
            if (hotKeywords != null && !hotKeywords.isEmpty()) {
                for (Object keywordObj : hotKeywords) {
                    try {
                        if (keywordObj != null) {
                            String keyword = keywordObj.toString();
                            if (keyword != null && !keyword.trim().isEmpty()) {
                                result.add(keyword);
                            }
                        }
                    } catch (Exception e) {
                        log.debug("[搜索] 处理热门搜索词失败 - keywordObj: {}", keywordObj, e);
                    }
                }
            }

            return result;
        } catch (org.springframework.data.redis.RedisConnectionFailureException e) {
            log.error("[搜索] Redis连接失败，返回空列表", e);
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("[搜索] 获取热门搜索词失败 - limit: {}", limit, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> getSearchSuggestions(String keyword, int limit) {
        try {
            List<String> suggestions = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String keywordTrimmed = keyword.trim().toLowerCase();
                String hotKeywordsKey = RedisKeyManager.postSearchHotKeywordsKey();
                Set<Object> hotKeywords = redisOps.zReverseRange(hotKeywordsKey, 0, 100);

                if (hotKeywords != null && !hotKeywords.isEmpty()) {
                    for (Object hotKeywordObj : hotKeywords) {
                        if (suggestions.size() >= limit) {
                            break;
                        }
                        if (hotKeywordObj != null) {
                            String hotKeywordStr = hotKeywordObj.toString();
                            if (hotKeywordStr != null && !hotKeywordStr.trim().isEmpty()) {
                                String hotKeyword = hotKeywordStr.toLowerCase();
                                if (hotKeyword.contains(keywordTrimmed)) {
                                    suggestions.add(hotKeywordStr);
                                }
                            }
                        }
                    }
                }
            } else {
                return getHotKeywords(limit);
            }

            return suggestions;
        } catch (Exception e) {
            log.error("[搜索] 获取搜索建议失败 - keyword: {}", keyword, e);
            return new ArrayList<>();
        }
    }

    private static class SearchStatisticsImpl implements SearchStatistics {
        private final String date;
        private final long totalSearches;
        private final long successfulSearches;
        private final long emptySearches;
        private final double successRate;

        public SearchStatisticsImpl(String date, long totalSearches, long successfulSearches,
                                    long emptySearches, double successRate) {
            this.date = date;
            this.totalSearches = totalSearches;
            this.successfulSearches = successfulSearches;
            this.emptySearches = emptySearches;
            this.successRate = successRate;
        }

        @Override
        public String getDate() {
            return date;
        }

        @Override
        public long getTotalSearches() {
            return totalSearches;
        }

        @Override
        public long getSuccessfulSearches() {
            return successfulSearches;
        }

        @Override
        public long getEmptySearches() {
            return emptySearches;
        }

        @Override
        public double getSuccessRate() {
            return successRate;
        }
    }

    private static class HotKeywordImpl implements HotKeyword {
        private final String keyword;
        private final long count;

        public HotKeywordImpl(String keyword, long count) {
            this.keyword = keyword;
            this.count = count;
        }

        @Override
        public String getKeyword() {
            return keyword;
        }

        @Override
        public long getCount() {
            return count;
        }
    }
}
