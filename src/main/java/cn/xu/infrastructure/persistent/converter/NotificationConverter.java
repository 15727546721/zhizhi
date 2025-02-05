package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.notification.model.aggregate.NotificationAggregate;
import cn.xu.domain.notification.model.entity.NotificationEntity;
import cn.xu.infrastructure.persistent.po.NotificationPO;
import org.springframework.stereotype.Component;

@Component
public class NotificationConverter {

    public NotificationPO toNotificationPO(NotificationAggregate aggregate) {
        NotificationEntity entity = aggregate.getNotification();
        NotificationPO po = new NotificationPO();
        po.setId(entity.getId());
        po.setType(entity.getType());
        po.setSenderId(entity.getSenderId());
        po.setSenderType(entity.getSenderType());
        po.setReceiverId(entity.getReceiverId());
        po.setTitle(entity.getTitle());
        po.setContent(entity.getContent());
        po.setBusinessType(entity.getBusinessType());
        po.setBusinessId(entity.getBusinessId());
        po.setExtraInfo(entity.getExtraInfo());
        po.setRead(entity.isRead());
        po.setStatus(entity.getStatus());
        po.setCreatedTime(entity.getCreatedTime());
        po.setUpdatedTime(entity.getUpdatedTime());
        return po;
    }

    public NotificationAggregate toNotificationAggregate(NotificationPO po) {
        NotificationEntity entity = new NotificationEntity();
        entity.setId(po.getId());
        entity.setType(po.getType());
        entity.setSenderId(po.getSenderId());
        entity.setSenderType(po.getSenderType());
        entity.setReceiverId(po.getReceiverId());
        entity.setTitle(po.getTitle());
        entity.setContent(po.getContent());
        entity.setBusinessType(po.getBusinessType());
        entity.setBusinessId(po.getBusinessId());
        entity.setExtraInfo(po.getExtraInfo());
        entity.setRead(po.getRead());
        entity.setStatus(po.getStatus());
        entity.setCreatedTime(po.getCreatedTime());
        entity.setUpdatedTime(po.getUpdatedTime());
        return NotificationAggregate.from(entity);
    }
} 