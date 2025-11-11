package cn.xu.domain.message.model.aggregate;

import cn.xu.domain.message.model.entity.PrivateMessageEntity;
import cn.xu.domain.message.model.valueobject.MessageStatus;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 私信聚合根
 * 管理私信的完整生命周期和业务一致性
 */
@Data
@Slf4j
public class PrivateMessageAggregate {
    
    /**
     * 私信实体
     */
    private PrivateMessageEntity privateMessage;
    
    /**
     * 领域事件列表
     */
    private List<Object> domainEvents = new ArrayList<>();
    
    /**
     * 私有构造函数
     */
    private PrivateMessageAggregate() {
        this.domainEvents = new ArrayList<>();
    }
    
    /**
     * 创建新的私信聚合根
     */
    public static PrivateMessageAggregate create(Long senderId, Long receiverId, String content, MessageStatus status) {
        PrivateMessageEntity message = PrivateMessageEntity.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .isRead(false)
                .status(status)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        PrivateMessageAggregate aggregate = new PrivateMessageAggregate();
        aggregate.privateMessage = message;
        
        // 添加私信创建事件
        aggregate.addDomainEvent(new PrivateMessageCreatedEvent(
                message.getSenderId(),
                message.getReceiverId(),
                message.getStatus(),
                LocalDateTime.now()
        ));
        
        return aggregate;
    }
    
    /**
     * 从持久化数据恢复聚合根
     */
    public static PrivateMessageAggregate restore(PrivateMessageEntity message) {
        PrivateMessageAggregate aggregate = new PrivateMessageAggregate();
        aggregate.privateMessage = message;
        return aggregate;
    }
    
    /**
     * 标记为已读
     */
    public void markAsRead() {
        if (privateMessage == null) {
            throw new IllegalStateException("Private message cannot be null");
        }
        
        if (!privateMessage.getIsRead()) {
            privateMessage.markAsRead();
            addDomainEvent(new PrivateMessageReadEvent(
                    privateMessage.getId(),
                    privateMessage.getReceiverId(),
                    LocalDateTime.now()
            ));
        }
    }
    
    /**
     * 更新消息状态
     */
    public void updateStatus(MessageStatus status) {
        if (privateMessage == null) {
            throw new IllegalStateException("Private message cannot be null");
        }
        
        MessageStatus oldStatus = privateMessage.getStatus();
        privateMessage.setStatus(status);
        privateMessage.setUpdateTime(LocalDateTime.now());
        
        addDomainEvent(new PrivateMessageStatusUpdatedEvent(
                privateMessage.getId(),
                oldStatus,
                status,
                LocalDateTime.now()
        ));
    }
    
    /**
     * 获取聚合根ID
     */
    public Long getId() {
        return privateMessage != null ? privateMessage.getId() : null;
    }
    
    /**
     * 添加领域事件
     */
    private void addDomainEvent(Object event) {
        if (this.domainEvents == null) {
            this.domainEvents = new ArrayList<>();
        }
        this.domainEvents.add(event);
        log.debug("添加领域事件: {}", event.getClass().getSimpleName());
    }
    
    /**
     * 获取并清空领域事件
     */
    public List<Object> pullDomainEvents() {
        List<Object> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return events;
    }
    
    // ==================== 内部事件类定义 ====================
    
    /**
     * 私信创建事件
     */
    public static class PrivateMessageCreatedEvent {
        private final Long senderId;
        private final Long receiverId;
        private final MessageStatus status;
        private final LocalDateTime createTime;
        
        public PrivateMessageCreatedEvent(Long senderId, Long receiverId, MessageStatus status, LocalDateTime createTime) {
            this.senderId = senderId;
            this.receiverId = receiverId;
            this.status = status;
            this.createTime = createTime;
        }
        
        public Long getSenderId() { return senderId; }
        public Long getReceiverId() { return receiverId; }
        public MessageStatus getStatus() { return status; }
        public LocalDateTime getCreateTime() { return createTime; }
    }
    
    /**
     * 私信已读事件
     */
    public static class PrivateMessageReadEvent {
        private final Long messageId;
        private final Long receiverId;
        private final LocalDateTime readTime;
        
        public PrivateMessageReadEvent(Long messageId, Long receiverId, LocalDateTime readTime) {
            this.messageId = messageId;
            this.receiverId = receiverId;
            this.readTime = readTime;
        }
        
        public Long getMessageId() { return messageId; }
        public Long getReceiverId() { return receiverId; }
        public LocalDateTime getReadTime() { return readTime; }
    }
    
    /**
     * 私信状态更新事件
     */
    public static class PrivateMessageStatusUpdatedEvent {
        private final Long messageId;
        private final MessageStatus oldStatus;
        private final MessageStatus newStatus;
        private final LocalDateTime updateTime;
        
        public PrivateMessageStatusUpdatedEvent(Long messageId, MessageStatus oldStatus, MessageStatus newStatus, LocalDateTime updateTime) {
            this.messageId = messageId;
            this.oldStatus = oldStatus;
            this.newStatus = newStatus;
            this.updateTime = updateTime;
        }
        
        public Long getMessageId() { return messageId; }
        public MessageStatus getOldStatus() { return oldStatus; }
        public MessageStatus getNewStatus() { return newStatus; }
        public LocalDateTime getUpdateTime() { return updateTime; }
    }
}

