package cn.xu.domain.message.service;

import cn.xu.common.exception.BusinessException;
import cn.xu.domain.follow.service.FollowQueryDomainService;
import cn.xu.domain.message.model.aggregate.PrivateMessageAggregate;
import cn.xu.domain.message.model.entity.ConversationEntity;
import cn.xu.domain.message.model.entity.FirstMessageEntity;
import cn.xu.domain.message.model.entity.UserMessageSettingsEntity;
import cn.xu.domain.message.model.valueobject.MessageStatus;
import cn.xu.domain.message.model.valueobject.UserRelationship;
import cn.xu.domain.message.repository.IConversationRepository;
import cn.xu.domain.message.repository.IFirstMessageRepository;
import cn.xu.domain.message.repository.IPrivateMessageRepository;
import cn.xu.domain.message.repository.IUserBlockRepository;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 私信领域服务
 * 负责处理私信的核心业务逻辑
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PrivateMessageDomainService {
    
    private final IPrivateMessageRepository privateMessageRepository;
    private final IConversationRepository conversationRepository;
    private final IFirstMessageRepository firstMessageRepository;
    private final IUserBlockRepository userBlockRepository;
    private final FollowQueryDomainService followQueryDomainService;
    private final IUserService userService;
    private final SystemConfigDomainService systemConfigDomainService;
    private final UserMessageSettingsDomainService userMessageSettingsDomainService;
    
    /**
     * 发送私信结果
     */
    public static class SendMessageResult {
        private final MessageStatus status;
        private final String message;
        private final Long messageId;
        
        public SendMessageResult(MessageStatus status, String message, Long messageId) {
            this.status = status;
            this.message = message;
            this.messageId = messageId;
        }
        
        public static SendMessageResult success(MessageStatus status, String message, Long messageId) {
            return new SendMessageResult(status, message, messageId);
        }
        
        public MessageStatus getStatus() { return status; }
        public String getMessage() { return message; }
        public Long getMessageId() { return messageId; }
    }
    
    /**
     * 发送私信
     */
    @Transactional(rollbackFor = Exception.class)
    public SendMessageResult sendPrivateMessage(Long senderId, Long receiverId, String content) {
        log.info("[私信领域服务] 开始发送私信 - 发送者: {}, 接收者: {}", senderId, receiverId);
        
        try {
            // 1. 验证用户存在并检查用户状态
            validateUsers(senderId, receiverId);
            
            // 2. 检查系统私信功能是否开启
            Boolean privateMessageEnabled = systemConfigDomainService.getConfigBooleanValue("private_message.enabled", true);
            if (privateMessageEnabled == null || !privateMessageEnabled) {
                log.warn("[私信领域服务] 私信功能已关闭");
                throw new BusinessException("私信功能暂时关闭");
            }
            
            // 3. 检查消息内容
            if (content == null || content.trim().isEmpty()) {
                throw new BusinessException("消息内容不能为空");
            }
            // 区分文本消息和图片消息
            boolean isImageMessage = isImageMessage(content);
            if (!isImageMessage) {
                // 文本消息：检查长度限制
                Integer maxMessageLength = systemConfigDomainService.getConfigIntValue("private_message.max_message_length", 1000);
                if (maxMessageLength != null && maxMessageLength > 0 && content.length() > maxMessageLength) {
                    throw new BusinessException("消息内容过长，最多" + maxMessageLength + "字");
                }
            } else {
                // 图片消息：验证JSON格式
                if (!validateImageMessageFormat(content)) {
                    throw new BusinessException("图片消息格式错误");
                }
            }
            
            // 4. 检查发送者状态（是否被封禁、待审核）
            UserEntity sender = userService.getUserById(senderId);
            try {
                sender.validateCanPerformAction(); // 检查发送者是否被封禁或待审核
            } catch (BusinessException e) {
                log.warn("[私信领域服务] 发送者状态异常 - 发送者: {}, 错误: {}", senderId, e.getMessage());
                throw new BusinessException("您的账号状态异常，无法发送私信");
            }
            
            // 5. 检查屏蔽关系（优先级最高）
            if (userBlockRepository.existsBlock(receiverId, senderId)) {
                log.warn("[私信领域服务] 用户被屏蔽 - 接收者: {}, 发送者: {}", receiverId, senderId);
                // 被接收者屏蔽：消息入库但不投递，仅发送方可见
                PrivateMessageAggregate blockedAggregate = PrivateMessageAggregate.create(
                        senderId, receiverId, content, MessageStatus.BLOCKED
                );
                Long blockedMessageId = privateMessageRepository.save(blockedAggregate);
                log.info("[私信领域服务] 被屏蔽消息已保存（仅发送方可见） - 消息ID: {}, 发送者: {}, 接收者: {}", blockedMessageId, senderId, receiverId);
                return SendMessageResult.success(
                        MessageStatus.BLOCKED,
                        "对方已屏蔽你，消息未送达，仅自己可见",
                        blockedMessageId
                );
            }
            
            // 6. 检查关注关系
            UserRelationship relationship = determineRelationship(senderId, receiverId);
            boolean isMutualFollow = relationship.isMutualFollow();
            
            // 7. 检查私信权限（仅在非互相关注时检查）
            // 优先级：系统设置 > 用户设置
            if (!isMutualFollow) {
                // 7.1 先检查系统设置（系统级别的全局限制）
                Boolean systemAllowStranger = systemConfigDomainService.getConfigBooleanValue(
                        "private_message.allow_stranger", true);
                if (systemAllowStranger == null || !systemAllowStranger) {
                    log.warn("[私信领域服务] 系统设置不允许陌生人私信");
                    throw new BusinessException("系统暂不允许陌生人私信");
                }
                
                // 7.2 再检查用户设置（用户个人的限制）
                UserMessageSettingsEntity receiverSettings = userMessageSettingsDomainService.getSettings(receiverId);
                
                // 检查用户是否允许陌生人私信
                if (!receiverSettings.isAllowStrangerMessage()) {
                    log.warn("[私信领域服务] 接收者不允许陌生人私信 - 接收者: {}", receiverId);
                    throw new BusinessException("对方设置了不允许陌生人私信");
                }
                
                // 检查用户是否允许非互相关注用户私信
                if (!receiverSettings.isAllowNonMutualFollowMessage()) {
                    log.warn("[私信领域服务] 接收者不允许非互相关注用户私信 - 接收者: {}", receiverId);
                    throw new BusinessException("对方设置了只接收互相关注用户的私信");
                }
            }
            
            // 8. 发送私信
            SendMessageResult result;
            if (isMutualFollow) {
                // 互相关注：直接发送，status=1
                result = sendMutualFollowMessage(senderId, receiverId, content);
            } else {
                // 非互相关注：需要检查
                result = sendNonMutualFollowMessage(senderId, receiverId, content);
            }
            
            // 9. 如果是回复消息，处理对话关系建立
            if (result.getStatus().isDelivered()) {
                handleReplyMessage(senderId, receiverId);
            }
            
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[私信领域服务] 发送私信失败 - 发送者: {}, 接收者: {}", senderId, receiverId, e);
            throw new BusinessException("发送私信失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送互相关注的私信
     */
    private SendMessageResult sendMutualFollowMessage(Long senderId, Long receiverId, String content) {
        // 创建消息，status=1
        PrivateMessageAggregate aggregate = PrivateMessageAggregate.create(
                senderId, receiverId, content, MessageStatus.DELIVERED
        );
        Long messageId = privateMessageRepository.save(aggregate);
        
        // 更新或创建对话关系
        updateConversation(senderId, receiverId);
        
        log.info("[私信领域服务] 互相关注私信发送成功 - 消息ID: {}", messageId);
        return SendMessageResult.success(MessageStatus.DELIVERED, "消息已送达", messageId);
    }
    
    /**
     * 发送非互相关注的私信
     */
    private SendMessageResult sendNonMutualFollowMessage(Long senderId, Long receiverId, String content) {
        // 关键：无论是否已建立对话关系，都要先检查首次消息状态（防骚扰机制）
        Optional<FirstMessageEntity> firstMessageOpt = firstMessageRepository.findBySenderAndReceiver(senderId, receiverId);
        
        log.info("[私信领域服务] 检查首次消息状态 - 发送者: {}, 接收者: {}, 是否存在: {}", 
                senderId, receiverId, firstMessageOpt.isPresent());
        
        if (firstMessageOpt.isPresent()) {
            FirstMessageEntity firstMessage = firstMessageOpt.get();
            log.info("[私信领域服务] 首次消息详情 - 记录ID: {}, 消息ID: {}, 是否已回复: {}", 
                    firstMessage.getId(), firstMessage.getMessageId(), firstMessage.getHasReplied());
            
            if (firstMessage.isNotReplied()) {
                // 对方未回复：保存消息但状态为PENDING（仅发送方可见，防骚扰机制）
                log.warn("[私信领域服务] 对方未回复首次消息，保存消息但对方暂时看不到 - 发送者: {}, 接收者: {}", senderId, receiverId);
                
                PrivateMessageAggregate aggregate = PrivateMessageAggregate.create(
                        senderId, receiverId, content, MessageStatus.PENDING
                );
                Long messageId = privateMessageRepository.save(aggregate);
                
                log.info("[私信领域服务] 防骚扰消息已保存（仅发送方可见） - 消息ID: {}, 发送者: {}, 接收者: {}", 
                        messageId, senderId, receiverId);
                
                return SendMessageResult.success(MessageStatus.PENDING, 
                        "对方尚未回复您的消息，消息已保存但对方暂时看不到", messageId);
            }
            // 对方已回复：应该已建立对话关系，继续正常流程
            log.info("[私信领域服务] 首次消息已回复，允许继续发送 - 发送者: {}, 接收者: {}", senderId, receiverId);
            
            // 检查是否已建立对话关系
            boolean hasConversation = conversationRepository.existsConversation(senderId, receiverId);
            if (hasConversation) {
                // 已建立对话：直接发送，status=1
                PrivateMessageAggregate aggregate = PrivateMessageAggregate.create(
                        senderId, receiverId, content, MessageStatus.DELIVERED
                );
                Long messageId = privateMessageRepository.save(aggregate);
                
                // 更新对话关系
                updateConversation(senderId, receiverId);
                
                log.info("[私信领域服务] 已建立对话的私信发送成功 - 消息ID: {}", messageId);
                return SendMessageResult.success(MessageStatus.DELIVERED, "消息已送达", messageId);
            } else {
                // 首次消息已回复但对话关系不存在（异常情况，但允许继续）
                log.warn("[私信领域服务] 首次消息已回复但对话关系不存在，创建对话关系 - 发送者: {}, 接收者: {}", senderId, receiverId);
                PrivateMessageAggregate aggregate = PrivateMessageAggregate.create(
                        senderId, receiverId, content, MessageStatus.DELIVERED
                );
                Long messageId = privateMessageRepository.save(aggregate);
                
                // 创建对话关系
                createConversation(senderId, receiverId, senderId);
                
                log.info("[私信领域服务] 私信发送成功并创建对话关系 - 消息ID: {}", messageId);
                return SendMessageResult.success(MessageStatus.DELIVERED, "消息已送达", messageId);
            }
        } else {
            // 首次消息记录不存在：需要检查对话关系
            log.info("[私信领域服务] 未找到首次消息记录 - 发送者: {}, 接收者: {}", senderId, receiverId);
            
            // 检查是否已建立对话关系
            boolean hasConversation = conversationRepository.existsConversation(senderId, receiverId);
            log.info("[私信领域服务] 对话关系检查 - 发送者: {}, 接收者: {}, 是否存在: {}", 
                    senderId, receiverId, hasConversation);
            
            if (hasConversation) {
                // 对话关系已存在：说明双方已经建立过联系（对方已回复过），直接发送消息
                // 不需要再走防骚扰机制，直接投递
                log.info("[私信领域服务] 对话关系已存在，直接发送消息 - 发送者: {}, 接收者: {}", senderId, receiverId);
                
                PrivateMessageAggregate aggregate = PrivateMessageAggregate.create(
                        senderId, receiverId, content, MessageStatus.DELIVERED
                );
                Long messageId = privateMessageRepository.save(aggregate);
                
                // 更新对话关系
                updateConversation(senderId, receiverId);
                
                log.info("[私信领域服务] 已建立对话的私信发送成功 - 消息ID: {}", messageId);
                return SendMessageResult.success(MessageStatus.DELIVERED, "消息已送达", messageId);
            } else {
                // 对话关系不存在：走防骚扰机制，记录首次消息
                log.info("[私信领域服务] 对话关系不存在，记录首次消息 - 发送者: {}, 接收者: {}", senderId, receiverId);
                return handleFirstMessage(senderId, receiverId, content);
            }
        }
    }
    
    /**
     * 处理首次消息
     */
    private SendMessageResult handleFirstMessage(Long senderId, Long receiverId, String content) {
        Optional<FirstMessageEntity> firstMessageOpt = firstMessageRepository.findBySenderAndReceiver(senderId, receiverId);
        
        if (!firstMessageOpt.isPresent()) {
            // 未发送过首次消息：允许发送，status=1（直接投递，接收方可见）
            // 防骚扰机制：允许发第一条消息，但后续消息需要等待回复
            PrivateMessageAggregate aggregate = PrivateMessageAggregate.create(
                    senderId, receiverId, content, MessageStatus.DELIVERED
            );
            Long messageId = privateMessageRepository.save(aggregate);
            
            // 记录首次消息
            FirstMessageEntity firstMessage = FirstMessageEntity.create(senderId, receiverId, messageId);
            Long firstMessageId = firstMessageRepository.save(firstMessage);
            if (firstMessageId == null || firstMessageId <= 0) {
                log.error("[私信领域服务] 首次消息记录保存失败 - 消息ID: {}, 发送者: {}, 接收者: {}", messageId, senderId, receiverId);
                throw new BusinessException("保存首次消息记录失败");
            }
            log.info("[私信领域服务] 首次消息记录保存成功 - 记录ID: {}, 消息ID: {}, 发送者: {}, 接收者: {}", 
                    firstMessageId, messageId, senderId, receiverId);
            
            // 创建对话记录，让接收方能在对话列表中看到
            // 防骚扰机制通过first_message表的has_replied字段控制后续消息
            updateConversation(senderId, receiverId);
            
            log.info("[私信领域服务] 首次消息发送成功（直接投递，已创建对话记录） - 消息ID: {}", messageId);
            return SendMessageResult.success(MessageStatus.DELIVERED, "消息已送达", messageId);
        } else {
            // 已发送过首次消息
            FirstMessageEntity firstMessage = firstMessageOpt.get();
            
            if (firstMessage.isNotReplied()) {
                // 对方未回复：保存消息但状态为PENDING（仅发送方可见，防骚扰机制）
                log.warn("[私信领域服务] 对方未回复首次消息，保存消息但对方暂时看不到 - 发送者: {}, 接收者: {}", senderId, receiverId);
                
                PrivateMessageAggregate aggregate = PrivateMessageAggregate.create(
                        senderId, receiverId, content, MessageStatus.PENDING
                );
                Long messageId = privateMessageRepository.save(aggregate);
                
                log.info("[私信领域服务] 防骚扰消息已保存（仅发送方可见） - 消息ID: {}, 发送者: {}, 接收者: {}", 
                        messageId, senderId, receiverId);
                
                return SendMessageResult.success(MessageStatus.PENDING, 
                        "对方尚未回复您的消息，消息已保存但对方暂时看不到", messageId);
            } else {
                // 对方已回复：应该已建立对话关系（逻辑保护，理论上不会走到这里）
                PrivateMessageAggregate aggregate = PrivateMessageAggregate.create(
                        senderId, receiverId, content, MessageStatus.DELIVERED
                );
                Long messageId = privateMessageRepository.save(aggregate);
                
                // 更新对话关系
                updateConversation(senderId, receiverId);
                
                log.warn("[私信领域服务] 对方已回复但未建立对话关系，已建立 - 消息ID: {}", messageId);
                return SendMessageResult.success(MessageStatus.DELIVERED, "消息已送达", messageId);
            }
        }
    }
    
    /**
     * 处理回复消息，建立对话关系
     * 
     * 逻辑说明（符合主流业务逻辑）：
     * 场景：A发送首次消息给B（非互相关注）
     * 1. A发送给B：status=1（DELIVERED），B可以收到，记录首次消息（A -> B，has_replied=0）
     * 2. 如果B没有回复，A再次发送：status=2（PENDING），仅A可见，B看不到（防骚扰机制）
     * 3. 如果B回复A：B发送消息给A（status=1）
     *    - 检查：A是否发送过首次消息给B -> 找到（has_replied=0）
     *    - 建立对话关系
     *    - 将A之前发送给B的所有status=2消息标记为status=1（已送达）
     *    - 这样B回复后，可以看到A之前发送的所有消息，对话更完整
     * 
     * 业务逻辑合理性：
     * - 防骚扰机制：对方未回复时，后续消息对方看不到，避免骚扰
     * - 对方回复后：之前被隐藏的消息应该显示，因为对方已经表示愿意建立对话
     * - 这符合主流系统（微信、QQ等）的设计逻辑
     * 
     * 参数说明：
     * - senderId: 当前发送者（B）
     * - receiverId: 当前接收者（A）
     * 
     * 检查逻辑：
     * - 检查receiverId（A）是否发送过首次消息给senderId（B）
     * - 如果找到且has_replied=0，说明B回复了A的首次消息
     */
    private void handleReplyMessage(Long senderId, Long receiverId) {
        // 检查receiverId（当前接收者）之前是否发送过首次消息给senderId（当前发送者）
        // 例如：A发送首次消息给B，B回复A时，检查A是否发送过首次消息给B
        Optional<FirstMessageEntity> firstMessageOpt = firstMessageRepository.findBySenderAndReceiver(receiverId, senderId);
        
        if (firstMessageOpt.isPresent() && firstMessageOpt.get().isNotReplied()) {
            // 这是回复：建立对话关系
            createConversation(senderId, receiverId, senderId);
            firstMessageRepository.updateHasReplied(receiverId, senderId, true);
            
            // 注意：不要更新之前被拦截的消息状态（status=2）
            // 正确的防骚扰机制：接收方回复后，只是解除了防骚扰限制
            // 后续的新消息可以正常投递（status=1），但之前被拦截的骚扰消息永远不会显示
            // 这样才能保护用户免受骚扰内容的困扰
            
            log.info("[私信领域服务] 回复消息，已建立对话关系，解除防骚扰限制 - 发送者: {}, 接收者: {}", 
                    senderId, receiverId);
        }
    }
    
    /**
     * 判断用户关系
     */
    private UserRelationship determineRelationship(Long userId1, Long userId2) {
        boolean user1FollowsUser2 = followQueryDomainService.isFollowing(userId1, userId2);
        boolean user2FollowsUser1 = followQueryDomainService.isFollowing(userId2, userId1);
        
        if (user1FollowsUser2 && user2FollowsUser1) {
            return UserRelationship.MUTUAL_FOLLOW;
        } else {
            return UserRelationship.NON_MUTUAL_FOLLOW;
        }
    }
    
    /**
     * 获取对话键（确保唯一性）
     */
    private List<Long> getConversationKey(Long userId1, Long userId2) {
        List<Long> key = new ArrayList<>();
        if (userId1 < userId2) {
            key.add(userId1);
            key.add(userId2);
        } else {
            key.add(userId2);
            key.add(userId1);
        }
        return key;
    }
    
    /**
     * 创建对话关系（如果已存在则更新，不存在则创建）
     */
    private void createConversation(Long userId1, Long userId2, Long createdBy) {
        List<Long> key = getConversationKey(userId1, userId2);
        
        // 先检查对话是否已存在（防止并发重复插入）
        Optional<ConversationEntity> existingOpt = conversationRepository.findByUserPair(key.get(0), key.get(1));
        
        if (existingOpt.isPresent()) {
            // 对话已存在，更新最后消息时间
            ConversationEntity conversation = existingOpt.get();
            conversation.updateLastMessageTime();
            conversationRepository.update(conversation);
            log.info("[私信领域服务] 对话关系已存在，更新最后消息时间 - 用户1: {}, 用户2: {}", key.get(0), key.get(1));
        } else {
            // 对话不存在，创建新对话
            ConversationEntity conversation = ConversationEntity.builder()
                    .userId1(key.get(0))
                    .userId2(key.get(1))
                    .createdBy(createdBy)
                    .lastMessageTime(LocalDateTime.now())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            conversationRepository.save(conversation);
            log.info("[私信领域服务] 创建新对话关系 - 用户1: {}, 用户2: {}, 创建者: {}", key.get(0), key.get(1), createdBy);
        }
    }
    
    /**
     * 更新对话关系
     */
    private void updateConversation(Long userId1, Long userId2) {
        List<Long> key = getConversationKey(userId1, userId2);
        Optional<ConversationEntity> conversationOpt = conversationRepository.findByUserPair(key.get(0), key.get(1));
        
        if (!conversationOpt.isPresent()) {
            // 创建对话关系
            createConversation(userId1, userId2, userId1);
        } else {
            // 更新最后消息时间
            ConversationEntity conversation = conversationOpt.get();
            conversation.updateLastMessageTime();
            conversationRepository.update(conversation);
        }
    }
    
    /**
     * 验证用户
     */
    private void validateUsers(Long senderId, Long receiverId) {
        if (senderId == null || senderId <= 0) {
            throw new BusinessException("发送者ID不能为空");
        }
        if (receiverId == null || receiverId <= 0) {
            throw new BusinessException("接收者ID不能为空");
        }
        if (senderId.equals(receiverId)) {
            throw new BusinessException("不能向自己发送私信");
        }
        
        // 检查接收者状态
        UserEntity receiver = userService.getUserById(receiverId);
        if (receiver == null) {
            throw new BusinessException("接收者不存在");
        }
        try {
            receiver.validateCanPerformAction(); // 检查接收者是否被封禁或注销
        } catch (BusinessException e) {
            log.warn("[私信领域服务] 接收者状态异常 - 接收者: {}, 错误: {}", receiverId, e.getMessage());
            throw new BusinessException("对方账号状态异常，无法发送私信");
        }
    }
    
    /**
     * 判断是否是图片消息
     */
    private boolean isImageMessage(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        // 尝试解析JSON，判断是否是图片消息格式
        try {
            String trimmed = content.trim();
            if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
                return false;
            }
            // 简单判断：包含 "type":"image" 和 "url"
            return trimmed.contains("\"type\"") && trimmed.contains("\"image\"") && trimmed.contains("\"url\"");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 验证图片消息格式
     */
    private boolean validateImageMessageFormat(String content) {
        try {
            // 使用简单的JSON验证（实际项目中应该使用JSON库如Jackson或Gson）
            String trimmed = content.trim();
            if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
                return false;
            }
            // 基本格式验证：必须包含 type 和 url 字段
            boolean hasType = trimmed.contains("\"type\"") && trimmed.contains("\"image\"");
            boolean hasUrl = trimmed.contains("\"url\"") && trimmed.contains("http");
            
            // URL长度验证：避免过长的URL
            if (content.length() > 2000) {
                log.warn("[私信领域服务] 图片消息URL过长 - 长度: {}", content.length());
                return false;
            }
            
            return hasType && hasUrl;
        } catch (Exception e) {
            log.error("[私信领域服务] 验证图片消息格式失败", e);
            return false;
        }
    }
}

