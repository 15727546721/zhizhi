package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.message.model.entity.ConversationEntity;
import cn.xu.infrastructure.persistent.po.Conversation;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 对话关系转换器
 * 负责对话关系领域实体与持久化对象之间的转换，遵循DDD防腐层模式
 */
@Component
public class ConversationConverter {
    
    /**
     * 领域实体转换为持久化对象
     */
    public Conversation toDataObject(ConversationEntity entity) {
        if (entity == null) {
            return null;
        }
        return Conversation.builder()
                .id(entity.getId())
                .userId1(entity.getUserId1())
                .userId2(entity.getUserId2())
                .createdBy(entity.getCreatedBy())
                .lastMessageTime(entity.getLastMessageTime())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
    
    /**
     * 持久化对象转换为领域实体
     */
    public ConversationEntity toDomainEntity(Conversation po) {
        if (po == null) {
            return null;
        }
        return ConversationEntity.builder()
                .id(po.getId())
                .userId1(po.getUserId1())
                .userId2(po.getUserId2())
                .createdBy(po.getCreatedBy())
                .lastMessageTime(po.getLastMessageTime())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
    
    /**
     * 批量转换持久化对象列表为领域实体列表
     */
    public List<ConversationEntity> toDomainEntities(List<Conversation> poList) {
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
    public List<Conversation> toDataObjects(List<ConversationEntity> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            return Collections.emptyList();
        }
        return entityList.stream()
                .map(this::toDataObject)
                .collect(Collectors.toList());
    }
}

