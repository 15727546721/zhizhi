package cn.xu.cache.repository;

import cn.xu.cache.core.BaseCacheRepository;
import cn.xu.cache.core.RedisKeyManager;
import cn.xu.model.entity.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * 标签缓存仓储
 * <p>处理标签相关的缓存操作</p>
 * <p>继承BaseCacheRepository复用通用方法，减少重复代码</p>
 
 */
@Slf4j
@Repository
public class TagCacheRepository extends BaseCacheRepository {

    /**
     * 获取热门标签
     * @param timeRange 时间范围，取值：today（今日）、week（本周）、month（本月）、all（全部）
     * @param limit 获取数量
     * @return 热门标签列表
     */
    public List<Tag> getHotTags(String timeRange, int limit) {
        String redisKey = RedisKeyManager.tagHotKey(timeRange, limit);
        
        Object cached = getValue(redisKey);
        if (cached != null) {
            log.debug("从Redis获取热门标签缓存命中: key={}, timeRange={}, limit={}", redisKey, timeRange, limit);
            return parseTagList(cached);
        }
        
        return null; // 返回null表示缓存未命中
    }

    /**
     * 缓存热门标签
     * @param timeRange 时间范围
     * @param limit 返回数量限制
     * @param tags 标签列表
     */
    public void cacheHotTags(String timeRange, int limit, List<Tag> tags) {
        String redisKey = RedisKeyManager.tagHotKey(timeRange, limit);
        
        if (tags == null || tags.isEmpty()) {
            setValue(redisKey, Collections.emptyList(), RedisKeyManager.TAG_EMPTY_RESULT_TTL);
            log.debug("缓存空热门标签结果: key={}, ttl={}s", redisKey, RedisKeyManager.TAG_EMPTY_RESULT_TTL);
        } else {
            setValue(redisKey, tags, RedisKeyManager.DEFAULT_TTL);
            log.debug("缓存热门标签成功: key={}, count={}, ttl={}s", redisKey, tags.size(), RedisKeyManager.DEFAULT_TTL);
        }
    }

    /**
     * 删除热门标签缓存
     * @param timeRange 时间范围，如果为null则删除所有时间范围的缓存
     */
    public void evictHotTagsCache(String timeRange) {
        java.util.List<String> keys = new java.util.ArrayList<>();
        
        if (timeRange == null) {
            // 删除所有时间范围的缓存
            for (String range : new String[]{"today", "week", "month", "all"}) {
                for (int limit = 10; limit <= 50; limit += 10) {
                    keys.add(RedisKeyManager.tagHotKey(range, limit));
                }
            }
            log.debug("删除所有热门标签缓存");
        } else {
            // 删除指定时间范围的缓存
            for (int limit = 10; limit <= 50; limit += 10) {
                keys.add(RedisKeyManager.tagHotKey(timeRange, limit));
            }
            log.debug("删除热门标签缓存: timeRange={}", timeRange);
        }
        
        deleteCacheBatch(keys);
    }

    /**
     * 解析缓存的标签列表
     * <p>Redis使用GenericJackson2JsonRedisSerializer自动序列化，可以直接转换</p>
     */
    @SuppressWarnings("unchecked")
    private List<Tag> parseTagList(Object cached) {
        try {
            if (cached instanceof List) {
                List<Object> list = (List<Object>) cached;
                // 检查列表是否为空
                if (list.isEmpty()) {
                    return Collections.emptyList();
                }
                
                // 检查第一个元素类型
                Object first = list.get(0);
                if (first instanceof Tag) {
                    // 如果已经是Tag类型，直接转换
                    return (List<Tag>) (List<?>) list;
                }
                
                // 否则尝试逐个转换（处理Jackson序列化的Map）
                if (first instanceof java.util.Map) {
                    java.util.Map<String, Object> firstMap = (java.util.Map<String, Object>) first;
                    // 如果是Jackson序列化的对象，会有@class字段
                    if (firstMap.containsKey("@class") || firstMap.containsKey("id")) {
                        // 转换为Tag列表
                        return list.stream()
                                .map(item -> {
                                    if (item instanceof java.util.Map) {
                                        return convertMapToTag((java.util.Map<String, Object>) item);
                                    }
                                    return null;
                                })
                                .filter(java.util.Objects::nonNull)
                                .collect(java.util.stream.Collectors.toList());
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析缓存的标签列表失败", e);
        }
        return Collections.emptyList();
    }
    
    /**
     * 将Map转换为Tag
     */
    private Tag convertMapToTag(java.util.Map<String, Object> map) {
        try {
            Tag.TagBuilder builder = Tag.builder();
            
            // ID
            if (map.get("id") != null) {
                Object idObj = map.get("id");
                if (idObj instanceof Number) {
                    builder.id(((Number) idObj).longValue());
                } else if (idObj instanceof String) {
                    builder.id(Long.parseLong((String) idObj));
                }
            }
            
            // Name
            if (map.get("name") != null) {
                builder.name(map.get("name").toString());
            }
            
            // CreateTime
            if (map.get("createTime") != null) {
                Object createTimeObj = map.get("createTime");
                if (createTimeObj instanceof String) {
                    String timeStr = (String) createTimeObj;
                    try {
                        // 尝试解析ISO格式
                        builder.createTime(java.time.LocalDateTime.parse(timeStr));
                    } catch (Exception e) {
                        try {
                            // 尝试解析自定义格式
                            builder.createTime(java.time.LocalDateTime.parse(timeStr, 
                                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        } catch (Exception e2) {
                            log.warn("无法解析createTime: {}", timeStr);
                        }
                    }
                } else if (createTimeObj instanceof java.time.LocalDateTime) {
                    builder.createTime((java.time.LocalDateTime) createTimeObj);
                }
            }
            
            // UpdateTime
            if (map.get("updateTime") != null) {
                Object updateTimeObj = map.get("updateTime");
                if (updateTimeObj instanceof String) {
                    String timeStr = (String) updateTimeObj;
                    try {
                        builder.updateTime(java.time.LocalDateTime.parse(timeStr));
                    } catch (Exception e) {
                        try {
                            builder.updateTime(java.time.LocalDateTime.parse(timeStr, 
                                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        } catch (Exception e2) {
                            log.warn("无法解析updateTime: {}", timeStr);
                        }
                    }
                } else if (updateTimeObj instanceof java.time.LocalDateTime) {
                    builder.updateTime((java.time.LocalDateTime) updateTimeObj);
                }
            }
            
            return builder.build();
        } catch (Exception e) {
            log.warn("转换Map到Tag失败: {}", map, e);
            return null;
        }
    }
}
