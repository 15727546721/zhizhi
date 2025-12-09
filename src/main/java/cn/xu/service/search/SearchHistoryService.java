package cn.xu.service.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 搜索历史服务
 * 
 * <p>基于Redis实现搜索历史和热词功能</p>
 * <p>用户搜索历史：Redis List，最多保存20条</p>
 * <p>热门搜索词：Redis ZSet，按搜索次数排序</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchHistoryService {

    private final StringRedisTemplate redisTemplate;

    /** 用户搜索历史前缀 */
    private static final String HISTORY_KEY_PREFIX = "search:history:";
    /** 热门搜索词Key */
    private static final String HOT_KEY = "search:hot";
    /** 每日热门搜索词Key */
    private static final String HOT_DAILY_KEY = "search:hot:daily";
    
    /** 搜索历史最大保存数量 */
    private static final int MAX_HISTORY_SIZE = 20;
    /** 热词返回数量 */
    private static final int HOT_WORD_LIMIT = 10;
    /** 搜索历史过期时间（天） */
    private static final int HISTORY_EXPIRE_DAYS = 30;
    /** 每日热词过期时间（天） */
    private static final int DAILY_HOT_EXPIRE_DAYS = 2;

    /**
     * 记录搜索（保存历史 + 更新热词）
     * 
     * @param userId 用户ID（可为null，未登录用户）
     * @param keyword 搜索关键词
     */
    public void recordSearch(Long userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        keyword = keyword.trim();
        
        try {
            // 1. 保存用户搜索历史（登录用户）
            if (userId != null) {
                saveHistory(userId, keyword);
            }
            
            // 2. 更新热词计数
            updateHotWord(keyword);
            
        } catch (Exception e) {
            log.error("记录搜索失败: userId={}, keyword={}", userId, keyword, e);
        }
    }

    /**
     * 保存用户搜索历史
     */
    private void saveHistory(Long userId, String keyword) {
        String key = HISTORY_KEY_PREFIX + userId;
        
        // 先移除已存在的相同关键词（去重）
        redisTemplate.opsForList().remove(key, 0, keyword);
        
        // 添加到列表头部
        redisTemplate.opsForList().leftPush(key, keyword);
        
        // 保留最近N条
        redisTemplate.opsForList().trim(key, 0, MAX_HISTORY_SIZE - 1);
        
        // 设置过期时间
        redisTemplate.expire(key, HISTORY_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /**
     * 更新热词计数
     */
    private void updateHotWord(String keyword) {
        // 总热词计数
        redisTemplate.opsForZSet().incrementScore(HOT_KEY, keyword, 1);
        
        // 每日热词计数
        redisTemplate.opsForZSet().incrementScore(HOT_DAILY_KEY, keyword, 1);
        redisTemplate.expire(HOT_DAILY_KEY, DAILY_HOT_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /**
     * 获取用户搜索历史
     * 
     * @param userId 用户ID
     * @return 搜索历史列表（最新在前）
     */
    public List<String> getHistory(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        
        try {
            String key = HISTORY_KEY_PREFIX + userId;
            List<String> history = redisTemplate.opsForList().range(key, 0, MAX_HISTORY_SIZE - 1);
            return history != null ? history : Collections.emptyList();
        } catch (Exception e) {
            log.error("获取搜索历史失败: userId={}", userId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 删除单条搜索历史
     * 
     * @param userId 用户ID
     * @param keyword 要删除的关键词
     */
    public void deleteHistory(Long userId, String keyword) {
        if (userId == null || keyword == null) {
            return;
        }
        
        try {
            String key = HISTORY_KEY_PREFIX + userId;
            redisTemplate.opsForList().remove(key, 0, keyword);
        } catch (Exception e) {
            log.error("删除搜索历史失败: userId={}, keyword={}", userId, keyword, e);
        }
    }

    /**
     * 清空用户搜索历史
     * 
     * @param userId 用户ID
     */
    public void clearHistory(Long userId) {
        if (userId == null) {
            return;
        }
        
        try {
            String key = HISTORY_KEY_PREFIX + userId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("清空搜索历史失败: userId={}", userId, e);
        }
    }

    /**
     * 获取热门搜索词
     * 
     * @return 热门搜索词列表（按热度降序）
     */
    public List<String> getHotWords() {
        try {
            // 优先使用每日热词，没有则使用总热词
            Set<String> dailyHot = redisTemplate.opsForZSet()
                    .reverseRange(HOT_DAILY_KEY, 0, HOT_WORD_LIMIT - 1);
            
            if (dailyHot != null && !dailyHot.isEmpty()) {
                return new ArrayList<>(dailyHot);
            }
            
            Set<String> hot = redisTemplate.opsForZSet()
                    .reverseRange(HOT_KEY, 0, HOT_WORD_LIMIT - 1);
            
            return hot != null ? new ArrayList<>(hot) : Collections.emptyList();
        } catch (Exception e) {
            log.error("获取热门搜索词失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取搜索建议（历史 + 热词匹配）
     * 
     * @param userId 用户ID（可为null）
     * @param prefix 输入前缀
     * @return 搜索建议列表
     */
    public List<String> getSuggestions(Long userId, String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        prefix = prefix.trim().toLowerCase();
        Set<String> suggestions = new LinkedHashSet<>();
        
        try {
            // 1. 从用户历史中匹配
            if (userId != null) {
                List<String> history = getHistory(userId);
                String finalPrefix = prefix;
                history.stream()
                        .filter(h -> h.toLowerCase().contains(finalPrefix))
                        .limit(5)
                        .forEach(suggestions::add);
            }
            
            // 2. 从热词中匹配
            List<String> hotWords = getHotWords();
            String finalPrefix2 = prefix;
            hotWords.stream()
                    .filter(h -> h.toLowerCase().contains(finalPrefix2))
                    .limit(5)
                    .forEach(suggestions::add);
            
            return new ArrayList<>(suggestions).subList(0, Math.min(suggestions.size(), 10));
        } catch (Exception e) {
            log.error("获取搜索建议失败: prefix={}", prefix, e);
            return Collections.emptyList();
        }
    }
}
