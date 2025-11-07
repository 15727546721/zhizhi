package cn.xu.infrastructure.cache;

import cn.xu.domain.post.model.entity.TagEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 标签缓存仓储
 * 处理标签相关的缓存操作，遵循DDD原则
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TagCacheRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final int DEFAULT_CACHE_TTL = 3600; // 1小时
    private static final int EMPTY_RESULT_TTL = 300;   // 5分钟

    /**
     * 获取热门标签缓存
     * @param timeRange 时间范围：today(今日)、week(本周)、month(本月)、all(全部)
     * @param limit 返回数量限制
     * @return 标签列表
     */
    public List<TagEntity> getHotTags(String timeRange, int limit) {
        String redisKey = RedisKeyManager.tagHotKey(timeRange, limit);
        
        try {
            Object cached = redisTemplate.opsForValue().get(redisKey);
            if (cached != null) {
                log.debug("从Redis获取热门标签缓存命中: key={}, timeRange={}, limit={}", redisKey, timeRange, limit);
                return parseTagList(cached);
            }
        } catch (Exception e) {
            log.error("从Redis获取热门标签缓存失败: key={}, timeRange={}, limit={}", redisKey, timeRange, limit, e);
        }
        
        return null; // 返回null表示缓存未命中
    }

    /**
     * 缓存热门标签
     * @param timeRange 时间范围
     * @param limit 返回数量限制
     * @param tags 标签列表
     */
    public void cacheHotTags(String timeRange, int limit, List<TagEntity> tags) {
        String redisKey = RedisKeyManager.tagHotKey(timeRange, limit);
        
        try {
            if (tags == null || tags.isEmpty()) {
                // 空结果缓存时间短一些
                redisTemplate.opsForValue().set(redisKey, Collections.emptyList(), EMPTY_RESULT_TTL, TimeUnit.SECONDS);
                log.debug("缓存空热门标签结果: key={}, ttl={}s", redisKey, EMPTY_RESULT_TTL);
            } else {
                // 直接存储TagEntity列表，Redis会自动序列化（使用GenericJackson2JsonRedisSerializer）
                redisTemplate.opsForValue().set(redisKey, tags, DEFAULT_CACHE_TTL, TimeUnit.SECONDS);
                log.debug("缓存热门标签成功: key={}, count={}, ttl={}s", redisKey, tags.size(), DEFAULT_CACHE_TTL);
            }
        } catch (Exception e) {
            log.error("缓存热门标签失败: key={}, timeRange={}, limit={}", redisKey, timeRange, limit, e);
        }
    }

    /**
     * 删除热门标签缓存
     * @param timeRange 时间范围，如果为null则删除所有时间范围的缓存
     */
    public void evictHotTagsCache(String timeRange) {
        try {
            if (timeRange == null) {
                // 删除所有时间范围的缓存（使用通配符）
                String pattern = RedisKeyManager.tagHotKey("*", 0).replace("*", "*");
                // 注意：这里需要实现通配符删除，简化处理，删除常见的时间范围
                for (String range : new String[]{"today", "week", "month", "all"}) {
                    for (int limit = 10; limit <= 50; limit += 10) {
                        String key = RedisKeyManager.tagHotKey(range, limit);
                        redisTemplate.delete(key);
                    }
                }
                log.debug("删除所有热门标签缓存");
            } else {
                // 删除指定时间范围的缓存（所有limit）
                for (int limit = 10; limit <= 50; limit += 10) {
                    String key = RedisKeyManager.tagHotKey(timeRange, limit);
                    redisTemplate.delete(key);
                }
                log.debug("删除热门标签缓存: timeRange={}", timeRange);
            }
        } catch (Exception e) {
            log.error("删除热门标签缓存失败: timeRange={}", timeRange, e);
        }
    }

    /**
     * 解析缓存的标签列表
     * Redis使用GenericJackson2JsonRedisSerializer自动序列化，可以直接转换
     */
    @SuppressWarnings("unchecked")
    private List<TagEntity> parseTagList(Object cached) {
        try {
            if (cached instanceof List) {
                List<Object> tagList = (List<Object>) cached;
                // 如果列表为空，直接返回
                if (tagList.isEmpty()) {
                    return Collections.emptyList();
                }
                
                // 检查第一个元素是否是TagEntity（通过@class字段判断）
                Object firstItem = tagList.get(0);
                if (firstItem instanceof java.util.Map) {
                    java.util.Map<String, Object> firstMap = (java.util.Map<String, Object>) firstItem;
                    // 如果是Jackson序列化的对象，会有@class字段
                    if (firstMap.containsKey("@class") || firstMap.containsKey("id")) {
                        // 转换为TagEntity列表
                        return tagList.stream()
                                .map(item -> {
                                    if (item instanceof java.util.Map) {
                                        return convertMapToTagEntity((java.util.Map<String, Object>) item);
                                    }
                                    return null;
                                })
                                .filter(java.util.Objects::nonNull)
                                .collect(java.util.stream.Collectors.toList());
                    }
                }
                
                // 如果已经是TagEntity类型（理论上不会发生，因为Redis序列化后是Map）
                // 但为了兼容性，保留这个判断
                if (firstItem instanceof TagEntity) {
                    return (List<TagEntity>) (List<?>) tagList;
                }
            }
        } catch (Exception e) {
            log.error("解析缓存的标签列表失败", e);
        }
        return Collections.emptyList();
    }
    
    /**
     * 将Map转换为TagEntity
     */
    private TagEntity convertMapToTagEntity(java.util.Map<String, Object> map) {
        try {
            TagEntity.TagEntityBuilder builder = TagEntity.builder();
            
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
            log.warn("转换Map到TagEntity失败: {}", map, e);
            return null;
        }
    }
}

