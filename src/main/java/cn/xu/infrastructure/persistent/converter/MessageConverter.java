package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.message.model.entity.MessageEntity;
import cn.xu.domain.message.model.entity.MessageType;
import cn.xu.infrastructure.persistent.po.Message;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息转换器
 * 负责消息领域实体与持久化对象之间的转换，遵循DDD防腐层模式
 * 
 * @author xu
 */
@Component
public class MessageConverter {

    /**
     * 领域实体转换为持久化对象
     */
    public Message toDataObject(MessageEntity entity) {
        if (entity == null) {
            return null;
        }
        return Message.builder()
                .id(entity.getId())
                .type(entity.getType() != null ? entity.getType().getCode() : null)
                .senderId(entity.getSenderId())
                .receiverId(entity.getReceiverId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .targetId(entity.getTargetId())
                .isRead(entity.getIsRead() != null && entity.getIsRead() ? 1 : 0)
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    /**
     * 持久化对象转换为领域实体
     */
    public MessageEntity toDomainEntity(Message po) {
        if (po == null) {
            return null;
        }
        return MessageEntity.builder()
                .id(po.getId())
                .type(MessageType.fromCode(po.getType()))
                .senderId(po.getSenderId())
                .receiverId(po.getReceiverId())
                .title(po.getTitle())
                .content(po.getContent())
                .targetId(po.getTargetId())
                .isRead(po.getIsRead() != null && po.getIsRead() == 1)
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    /**
     * 批量转换持久化对象列表为领域实体列表
     */
    public List<MessageEntity> toDomainEntities(List<Message> poList) {
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyList();
        }
        return poList.stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    /**
     * 批量转换领域实体列表为持久化对象列表
     */
    public List<Message> toDataObjects(List<MessageEntity> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            return Collections.emptyList();
        }
        return entityList.stream()
                .map(this::toDataObject)
                .collect(Collectors.toList());
    }
}