package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.message.model.entity.FirstMessageEntity;
import cn.xu.infrastructure.persistent.po.FirstMessage;
import org.springframework.stereotype.Component;

/**
 * 首次消息记录转换器
 * 负责首次消息记录领域实体与持久化对象之间的转换，遵循DDD防腐层模式
 */
@Component
public class FirstMessageConverter {
    
    /**
     * 领域实体转换为持久化对象
     */
    public FirstMessage toDataObject(FirstMessageEntity entity) {
        if (entity == null) {
            return null;
        }
        return FirstMessage.builder()
                .id(entity.getId())
                .senderId(entity.getSenderId())
                .receiverId(entity.getReceiverId())
                .messageId(entity.getMessageId())
                .hasReplied(entity.getHasReplied() != null && entity.getHasReplied() ? 1 : 0)
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
    
    /**
     * 持久化对象转换为领域实体
     */
    public FirstMessageEntity toDomainEntity(FirstMessage po) {
        if (po == null) {
            return null;
        }
        return FirstMessageEntity.builder()
                .id(po.getId())
                .senderId(po.getSenderId())
                .receiverId(po.getReceiverId())
                .messageId(po.getMessageId())
                .hasReplied(po.getHasReplied() != null && po.getHasReplied() == 1)
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
}

