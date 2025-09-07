package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.notification.model.entity.NotificationEntity;
import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import cn.xu.infrastructure.persistent.po.Notification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知领域实体与持久化对象转换器
 * 符合DDD架构的防腐层模式
 */
@Component
public class NotificationConverter {

    /**
     * 将领域实体转换为持久化对象
     *
     * @param entity 通知领域实体
     * @return 通知持久化对象
     */
    public Notification toDataObject(NotificationEntity entity) {
        if (entity == null) {
            return null;
        }

        return Notification.builder()
                .id(entity.getId())
                .receiverId(entity.getReceiverId())
                .senderId(entity.getSenderId())
                .type(entity.getType() != null ? entity.getType().getValue() : null)
                .title(entity.getTitle())
                .content(entity.getContent())
                .businessType(entity.getBusinessType() != null ? entity.getBusinessType().getValue() : null)
                .businessId(entity.getBusinessId())
                .isRead(entity.isRead() ? 1 : 0)
                .status(entity.isStatus() ? 1 : 0)
                .readTime(entity.getReadTime())
                .createTime(entity.getCreateTime() != null ? entity.getCreateTime() : LocalDateTime.now())
                .updateTime(entity.getUpdateTime() != null ? entity.getUpdateTime() : LocalDateTime.now())
                .build();
    }

    /**
     * 将持久化对象转换为领域实体
     *
     * @param dataObject 通知持久化对象
     * @return 通知领域实体
     */
    public NotificationEntity toDomainEntity(Notification dataObject) {
        if (dataObject == null) {
            return null;
        }

        return NotificationEntity.builder()
                .id(dataObject.getId())
                .receiverId(dataObject.getReceiverId())
                .senderId(dataObject.getSenderId())
                .type(dataObject.getType() != null ? NotificationType.fromValue(dataObject.getType()) : null)
                .title(dataObject.getTitle())
                .content(dataObject.getContent())
                .businessType(dataObject.getBusinessType() != null ? BusinessType.fromValue(dataObject.getBusinessType()) : null)
                .businessId(dataObject.getBusinessId())
                .read(dataObject.getIsRead() != null && dataObject.getIsRead() == 1)
                .status(dataObject.getStatus() != null && dataObject.getStatus() == 1)
                .readTime(dataObject.getReadTime())
                .createTime(dataObject.getCreateTime())
                .updateTime(dataObject.getUpdateTime())
                .build();
    }

    /**
     * 将持久化对象列表转换为领域实体列表
     *
     * @param dataObjects 持久化对象列表
     * @return 领域实体列表
     */
    public List<NotificationEntity> toDomainEntities(List<Notification> dataObjects) {
        if (dataObjects == null || dataObjects.isEmpty()) {
            return new ArrayList<>();
        }

        return dataObjects.stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    /**
     * 将领域实体列表转换为持久化对象列表
     *
     * @param entities 领域实体列表
     * @return 持久化对象列表
     */
    public List<Notification> toDataObjects(List<NotificationEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }

        return entities.stream()
                .map(this::toDataObject)
                .collect(Collectors.toList());
    }

    /**
     * 更新持久化对象的部分字段（来自领域实体）
     *
     * @param target 目标持久化对象
     * @param source 源领域实体
     * @return 更新后的持久化对象
     */
    public Notification updateDataObject(Notification target, NotificationEntity source) {
        if (target == null || source == null) {
            return target;
        }

        if (source.getTitle() != null) {
            target.setTitle(source.getTitle());
        }

        if (source.getContent() != null) {
            target.setContent(source.getContent());
        }

        if (source.getType() != null) {
            target.setType(source.getType().getValue());
        }

        if (source.getBusinessType() != null) {
            target.setBusinessType(source.getBusinessType().getValue());
        }

        if (source.getBusinessId() != null) {
            target.setBusinessId(source.getBusinessId());
        }

        target.setIsRead(source.isRead() ? 1 : 0);
        target.setStatus(source.isStatus() ? 1 : 0);
        target.setReadTime(source.getReadTime());
        target.setUpdateTime(LocalDateTime.now());

        return target;
    }
}