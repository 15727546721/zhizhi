package cn.xu.application.service;

import cn.xu.domain.message.model.aggregate.PrivateMessageAggregate;
import cn.xu.domain.message.model.entity.ConversationEntity;
import cn.xu.domain.message.model.entity.SystemConfigEntity;
import cn.xu.domain.message.model.entity.UserBlockEntity;
import cn.xu.domain.message.model.entity.UserMessageSettingsEntity;
import cn.xu.domain.message.model.valueobject.MessageStatus;
import cn.xu.domain.message.service.ConversationDomainService;
import cn.xu.domain.message.service.PrivateMessageDomainService;
import cn.xu.domain.message.service.SystemConfigDomainService;
import cn.xu.domain.message.service.UserBlockDomainService;
import cn.xu.domain.message.service.UserMessageSettingsDomainService;
import cn.xu.domain.message.repository.IPrivateMessageRepository;
import cn.xu.infrastructure.cache.RedisKeyManager;
import cn.xu.infrastructure.cache.RedisService;
import cn.xu.infrastructure.util.RedisLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
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
        
        // 轻量频控：同一会话2秒冷却，防止秒刷
        String cooldownKey = "pm:cooldown:" + senderId + ":" + receiverId;
        boolean acquired = false;
        try {
            acquired = redisLock.tryLock(cooldownKey, 2, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("[私信应用服务] 频控锁异常，忽略降级 - key={}", cooldownKey, e);
            acquired = true; // 降级放行
        }
        if (!acquired) {
            throw new cn.xu.common.exception.BusinessException("您发送太快啦，请稍后再试");
        }
        
        PrivateMessageDomainService.SendMessageResult result = privateMessageDomainService.sendPrivateMessage(senderId, receiverId, content);
        
        // 清除相关缓存
        clearConversationListCache(senderId);
        clearConversationListCache(receiverId);
        clearUnreadCountCache(receiverId, senderId);
        
        return result;
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
            // 清除所有分页的对话列表缓存（使用通配符）
            // 注意：由于RedisService可能不支持通配符删除，这里先清除常见分页的缓存
            // 实际应用中可以考虑使用Redis的SCAN命令或维护一个专门的key来记录所有缓存key
            String baseKey = RedisKeyManager.privateMessageConversationListKey(userId, 0, 0)
                    .replace(":0:0", "");
            // 清除前几页的缓存（通常用户不会翻太多页）
            for (int page = 1; page <= 5; page++) {
                for (int size = 10; size <= 50; size += 10) {
                    String cacheKey = baseKey + ":" + page + ":" + size;
                    redisService.del(cacheKey);
                }
            }
        } catch (Exception e) {
            log.warn("清除对话列表缓存失败，用户: {}", userId, e);
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

