package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.message.model.aggregate.PrivateMessageAggregate;
import cn.xu.domain.message.model.entity.PrivateMessageEntity;
import cn.xu.domain.message.model.valueobject.MessageStatus;
import cn.xu.infrastructure.persistent.po.Message;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 私信转换器
 * 负责私信领域实体与持久化对象之间的转换，遵循DDD防腐层模式
 */
@Component
public class PrivateMessageConverter {
    
    /**
     * 私信聚合根转换为持久化对象
     */
    public Message toDataObject(PrivateMessageAggregate aggregate) {
        if (aggregate == null || aggregate.getPrivateMessage() == null) {
            return null;
        }
        PrivateMessageEntity entity = aggregate.getPrivateMessage();
        return Message.builder()
                .id(entity.getId())
                .type(2) // 私信消息类型
                .senderId(entity.getSenderId())
                .receiverId(entity.getReceiverId())
                .content(entity.getContent())
                .isRead(entity.getIsRead() != null && entity.getIsRead() ? 1 : 0)
                .status(entity.getStatus() != null ? entity.getStatus().getCode() : MessageStatus.DELIVERED.getCode())
                .createTime(entity.getCreateTime() != null ? Date.from(entity.getCreateTime().atZone(ZoneId.systemDefault()).toInstant()) : null)
                .updateTime(entity.getUpdateTime() != null ? Date.from(entity.getUpdateTime().atZone(ZoneId.systemDefault()).toInstant()) : null)
                .build();
    }
    
    /**
     * 持久化对象转换为私信实体
     */
    public PrivateMessageEntity toDomainEntity(Message po) {
        if (po == null) {
            return null;
        }
        return PrivateMessageEntity.builder()
                .id(po.getId())
                .senderId(po.getSenderId())
                .receiverId(po.getReceiverId())
                .content(po.getContent())
                .isRead(po.getIsRead() != null && po.getIsRead() == 1)
                .status(po.getStatus() != null ? MessageStatus.fromCode(po.getStatus()) : MessageStatus.DELIVERED)
                .createTime(po.getCreateTime() != null ? LocalDateTime.ofInstant(po.getCreateTime().toInstant(), ZoneId.systemDefault()) : null)
                .updateTime(po.getUpdateTime() != null ? LocalDateTime.ofInstant(po.getUpdateTime().toInstant(), ZoneId.systemDefault()) : null)
                .build();
    }
    
    /**
     * 持久化对象转换为私信聚合根
     */
    public PrivateMessageAggregate toDomainAggregate(Message po) {
        if (po == null) {
            return null;
        }
        PrivateMessageEntity entity = toDomainEntity(po);
        return PrivateMessageAggregate.restore(entity);
    }
    
    /**
     * 批量转换持久化对象列表为私信聚合根列表
     */
    public List<PrivateMessageAggregate> toDomainAggregates(List<Message> poList) {
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyList();
        }
        return poList.stream()
                .map(this::toDomainAggregate)
                .collect(Collectors.toList());
    }
    
    /**
     * 批量转换私信聚合根列表为持久化对象列表
     */
    public List<Message> toDataObjects(List<PrivateMessageAggregate> aggregateList) {
        if (aggregateList == null || aggregateList.isEmpty()) {
            return Collections.emptyList();
        }
        return aggregateList.stream()
                .map(this::toDataObject)
                .collect(Collectors.toList());
    }
}

