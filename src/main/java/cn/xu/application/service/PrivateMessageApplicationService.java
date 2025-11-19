package cn.xu.application.service;

import cn.xu.domain.message.model.aggregate.PrivateMessageAggregate;
import cn.xu.domain.message.model.entity.ConversationEntity;
import cn.xu.domain.message.model.entity.SystemConfigEntity;
import cn.xu.domain.message.model.entity.UserBlockEntity;
import cn.xu.domain.message.model.entity.UserMessageSettingsEntity;
import cn.xu.domain.message.repository.IPrivateMessageRepository;
import cn.xu.domain.message.service.*;
import cn.xu.infrastructure.cache.RedisKeyManager;
import cn.xu.infrastructure.cache.RedisService;
import cn.xu.infrastructure.util.RedisLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 私信应用服务
 * 协调领域服务，处理私信相关的应用层逻辑
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PrivateMessageApplicationService {
    
    private final PrivateMessageDomainService privateMessageDomainService;
    private final UserBlockDomainService userBlockDomainService;
    private final ConversationDomainService conversationDomainService;
    private final IPrivateMessageRepository privateMessageRepository;
    private final UserMessageSettingsDomainService userMessageSettingsDomainService;
    private final SystemConfigDomainService systemConfigDomainService;
    private final RedisService redisService;
    private final RedisLock redisLock;
    
    /**
     * 发送私信
     */
    public PrivateMessageDomainService.SendMessageResult sendPrivateMessage(Long senderId, Long receiverId, String content) {
        log.info("[私信应用服务] 开始发送私信 - 发送者: {}, 接收者: {}", senderId, receiverId);
        
        // 多层频控防护：锁 + Redis计数器
        String cooldownKey = "pm:cooldown:" + senderId + ":" + receiverId;
        String rateLimitKey = "pm:rate_limit:" + senderId + ":" + receiverId;
        
        // 第一层：Redis计数器频控（1分钟内最多5条消息）
        try {
            // 使用原子操作：先增加计数器，再检查结果
            long newCount = redisService.incr(rateLimitKey, 1L);
            if (newCount == 1) {
                // 第一次设置，同时设置过期时间
                redisService.expire(rateLimitKey, 60);
            }
            
            if (newCount > 5) {
                log.warn("[私信应用服务] 用户发送消息频率过高，触发计数器限制 - 发送者: {}, 接收者: {}, 当前计数: {}", senderId, receiverId, newCount);
                throw new cn.xu.common.exception.BusinessException("您发送得太频繁，请稍后再试");
            }
        } catch (Exception e) {
            log.error("[私信应用服务] 频控计数器异常，阻止发送 - key={}", rateLimitKey, e);
            throw new cn.xu.common.exception.BusinessException("系统繁忙，请稍后再试");
        }
        
        // 第二层：分布式锁频控（同一会话2秒冷却）
        boolean acquired = false;
        try {
            acquired = redisLock.tryLock(cooldownKey, 2, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("[私信应用服务] 用户发送消息过快，触发冷却限制 - 发送者: {}, 接收者: {}", senderId, receiverId);
                throw new cn.xu.common.exception.BusinessException("您发送太快啦，请稍后再试");
            }
        } catch (Exception e) {
            log.error("[私信应用服务] 频控锁异常，阻止发送 - key={}", cooldownKey, e);
            // 不再降级放行，直接阻止发送
            throw new cn.xu.common.exception.BusinessException("系统繁忙，请稍后再试");
        }
        
        try {
            PrivateMessageDomainService.SendMessageResult result = privateMessageDomainService.sendPrivateMessage(senderId, receiverId, content);
            
            // 清除相关缓存
            clearConversationListCache(senderId);
            clearConversationListCache(receiverId);
            clearUnreadCountCache(receiverId, senderId);
            
            return result;
        } finally {
            // 确保锁被释放（锁会自动过期，这里仅作清理）
            if (acquired) {
                try {
                    // 如果RedisLock有unlock方法就调用，否则依赖自动过期
                    // redisLock.unlock(cooldownKey);
                } catch (Exception e) {
                    log.warn("[私信应用服务] 释放频控锁失败 - key={}", cooldownKey, e);
                }
            }
        }
    }
    
    /**
     * 获取两个用户之间的消息列表
     */
    public List<PrivateMessageAggregate> getMessagesBetweenUsers(Long currentUserId, Long otherUserId, Integer pageNo, Integer pageSize) {
        log.info("[私信应用服务] 获取消息列表 - 当前用户: {}, 对方用户: {}", currentUserId, otherUserId);
        int offset = (pageNo - 1) * pageSize;
        return privateMessageRepository.findMessagesBetweenUsers(currentUserId, otherUserId, offset, pageSize);
    }
    
    /**
     * 获取对话列表
     */
    public List<ConversationEntity> getConversationList(Long userId, Integer pageNo, Integer pageSize) {
        log.info("[私信应用服务] 获取对话列表 - 用户: {}", userId);
        return conversationDomainService.getConversationList(userId, pageNo, pageSize);
    }
    
    /**
     * 标记消息为已读
     */
    public void markAsRead(Long receiverId, Long senderId) {
        log.info("[私信应用服务] 标记消息为已读 - 接收者: {}, 发送者: {}", receiverId, senderId);
        privateMessageRepository.markAsReadBySender(receiverId, senderId);
        
        // 清除未读数缓存
        clearUnreadCountCache(receiverId, senderId);
        
        // 清除对话列表缓存
        clearConversationListCache(receiverId);
    }
    
    /**
     * 清除对话列表缓存
     */
    private void clearConversationListCache(Long userId) {
        try {
            // 方法1：使用缓存注册表精确清除（使用实际存在的Redis API）
            clearConversationCacheByRegistry(userId);
            
        } catch (Exception e) {
            log.warn("清除对话列表缓存失败，用户: {}", userId, e);
            // 降级处理：使用扩展的硬编码方式作为最后保障
            clearConversationCacheLegacy(userId);
        }
    }
    
    /**
     * 通过缓存注册表精确清除对话列表缓存
     */
    private void clearConversationCacheByRegistry(Long userId) {
        try {
            // 获取该用户的所有对话列表缓存key
            String registryKey = "pm:conv_cache_registry:" + userId;
            Set<Object> cacheKeys = redisService.sGet(registryKey);
            
            if (cacheKeys != null && !cacheKeys.isEmpty()) {
                String[] keysToDelete = cacheKeys.toArray(new String[0]);
                redisService.del(keysToDelete);
                log.info("[私信应用服务] 通过注册表清除对话列表缓存，用户: {}, 清除数量: {}", userId, keysToDelete.length);
                
                // 清空注册表
                redisService.del(registryKey);
            }
        } catch (Exception e) {
            log.warn("[私信应用服务] 通过注册表清除缓存失败，用户: {}", userId, e);
            throw e;
        }
    }
    
    /**
     * 降级处理：使用扩展的硬编码方式清除缓存
     */
    private void clearConversationCacheLegacy(Long userId) {
        try {
            String baseKey = RedisKeyManager.privateMessageConversationListKey(userId, 0, 0)
                    .replace(":0:0", "");
            
            // 扩大清除范围，清除前10页的缓存，页大小范围也扩大
            for (int page = 1; page <= 10; page++) {
                for (int size = 10; size <= 100; size += 10) {
                    String cacheKey = baseKey + ":" + page + ":" + size;
                    redisService.del(cacheKey);
                }
            }
            log.warn("[私信应用服务] 使用降级方式清除对话列表缓存，用户: {}", userId);
        } catch (Exception e) {
            log.warn("[私信应用服务] 降级清除缓存也失败，用户: {}", userId, e);
        }
    }
    
    /**
     * 注册对话列表缓存key（在获取对话列表时调用）
     */
    private void registerConversationCacheKey(Long userId, String cacheKey) {
        try {
            String registryKey = "pm:conv_cache_registry:" + userId;
            redisService.sSet(registryKey, cacheKey);
            // 设置注册表过期时间，防止内存泄漏
            redisService.expire(registryKey, 3600); // 1小时
        } catch (Exception e) {
            log.warn("[私信应用服务] 注册缓存key失败，用户: {}, key: {}", userId, cacheKey, e);
        }
    }
    
    /**
     * 获取未读消息数（带缓存）
     */
    public long getUnreadCount(Long receiverId, Long senderId) {
        String cacheKey = RedisKeyManager.privateMessageUnreadCountKey(receiverId, senderId);
        
        // 尝试从缓存获取
        try {
            Object cached = redisService.get(cacheKey);
            if (cached != null) {
                if (cached instanceof Number) {
                    return ((Number) cached).longValue();
                } else if (cached instanceof String) {
                    return Long.parseLong((String) cached);
                }
            }
        } catch (Exception e) {
            log.warn("从Redis获取未读数失败，接收者: {}, 发送者: {}", receiverId, senderId, e);
        }
        
        // 缓存未命中，从数据库查询
        long count = privateMessageRepository.countUnreadMessages(receiverId, senderId);
        
        // 写入缓存
        try {
            redisService.set(cacheKey, count, RedisKeyManager.UNREAD_COUNT_CACHE_TTL);
        } catch (Exception e) {
            log.warn("写入Redis未读数失败，接收者: {}, 发送者: {}", receiverId, senderId, e);
        }
        
        return count;
    }
    
    /**
     * 清除未读数缓存
     */
    public void clearUnreadCountCache(Long receiverId, Long senderId) {
        try {
            String cacheKey = RedisKeyManager.privateMessageUnreadCountKey(receiverId, senderId);
            redisService.del(cacheKey);
            
            // 同时清除用户的未读数Map缓存
            String mapKey = RedisKeyManager.privateMessageUnreadCountMapKey(receiverId);
            redisService.del(mapKey);
        } catch (Exception e) {
            log.warn("清除未读数缓存失败，接收者: {}, 发送者: {}", receiverId, senderId, e);
        }
    }
    
    /**
     * 屏蔽用户
     */
    public void blockUser(Long userId, Long blockedUserId) {
        log.info("[私信应用服务] 屏蔽用户 - 用户: {}, 被屏蔽用户: {}", userId, blockedUserId);
        userBlockDomainService.blockUser(userId, blockedUserId);
    }
    
    /**
     * 取消屏蔽用户
     */
    public void unblockUser(Long userId, Long blockedUserId) {
        log.info("[私信应用服务] 取消屏蔽用户 - 用户: {}, 被屏蔽用户: {}", userId, blockedUserId);
        userBlockDomainService.unblockUser(userId, blockedUserId);
    }
    
    /**
     * 获取屏蔽列表
     */
    public List<UserBlockEntity> getBlockList(Long userId, Integer pageNo, Integer pageSize) {
        log.info("[私信应用服务] 获取屏蔽列表 - 用户: {}", userId);
        return userBlockDomainService.getBlockList(userId, pageNo, pageSize);
    }
    
    /**
     * 获取用户私信设置
     */
    public UserMessageSettingsEntity getUserMessageSettings(Long userId) {
        log.info("[私信应用服务] 获取用户私信设置 - 用户: {}", userId);
        return userMessageSettingsDomainService.getSettings(userId);
    }
    
    /**
     * 更新用户私信设置
     */
    public void updateUserMessageSettings(Long userId, Boolean allowStrangerMessage, Boolean allowNonMutualFollowMessage, Boolean messageNotificationEnabled) {
        log.info("[私信应用服务] 更新用户私信设置 - 用户: {}", userId);
        userMessageSettingsDomainService.updateSettings(userId, allowStrangerMessage, allowNonMutualFollowMessage, messageNotificationEnabled);
    }
    
    /**
     * 获取系统配置
     */
    public SystemConfigEntity getSystemConfig(String configKey) {
        log.info("[私信应用服务] 获取系统配置 - 配置键: {}", configKey);
        return systemConfigDomainService.getConfigsByPrefix("private_message").stream()
                .filter(config -> config.getConfigKey().equals(configKey))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 获取所有私信相关系统配置
     */
    public List<SystemConfigEntity> getPrivateMessageSystemConfigs() {
        log.info("[私信应用服务] 获取所有私信相关系统配置");
        return systemConfigDomainService.getConfigsByPrefix("private_message");
    }
    
    /**
     * 更新系统配置
     */
    public void updateSystemConfig(String configKey, String configValue) {
        log.info("[私信应用服务] 更新系统配置 - 配置键: {}, 配置值: {}", configKey, configValue);
        systemConfigDomainService.updateConfig(configKey, configValue);
    }

    /**
     * 查询屏蔽关系（blocker 是否屏蔽了 blockedUser）
     */
    public boolean existsBlock(Long blockerUserId, Long blockedUserId) {
        try {
            return userBlockDomainService.isBlocked(blockerUserId, blockedUserId);
        } catch (Exception e) {
            log.warn("[私信应用服务] 查询屏蔽关系失败 - blocker: {}, blocked: {}", blockerUserId, blockedUserId, e);
            return false;
        }
    }
}

