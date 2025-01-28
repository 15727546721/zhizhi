package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.notification.model.aggregate.NotificationAggregate;
import cn.xu.domain.notification.model.entity.NotificationEntity;
import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import cn.xu.domain.notification.model.valueobject.SenderType;
import cn.xu.domain.notification.repository.INotificationRepository;
import cn.xu.infrastructure.persistent.dao.INotificationDao;
import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 通知仓储实现类
 * 
 * @author xuhh
 * @date 2024/03/20
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class NotificationRepository implements INotificationRepository {

    private final INotificationDao notificationDao;

    /**
     * 保存通知
     *
     * @param aggregate 通知聚合根
     * @return 保存后的通知聚合根
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationAggregate save(NotificationAggregate aggregate) {
        try {
            NotificationEntity entity = aggregate.getNotification();
            // 验证实体有效性
            entity.validate();
            
            LocalDateTime now = LocalDateTime.now();
            if (entity.getId() == null) {
                log.info("[通知服务] 开始新增通知, receiverId={}, type={}", entity.getReceiverId(), entity.getType());
                entity.setCreatedTime(now);
                entity.setUpdatedTime(now);
                entity.setStatus(true); // 设置为有效状态
                notificationDao.insert(entity);
                log.info("[通知服务] 新增通知成功, id={}", entity.getId());
            } else {
                log.info("[通知服务] 开始更新通知, id={}", entity.getId());
                entity.setUpdatedTime(now);
                notificationDao.update(entity);
                log.info("[通知服务] 更新通知成功");
            }
            return NotificationAggregate.from(entity);
        } catch (Exception e) {
            log.error("[通知服务] 保存通知失败", e);
            throw new RuntimeException("保存通知失败", e);
        }
    }

    /**
     * 根据ID查询通知
     *
     * @param id 通知ID
     * @return 通知聚合根
     */
    @Override
    public Optional<NotificationAggregate> findById(Long id) {
        try {
            log.info("[通知服务] 开始查询通知, id={}", id);
            NotificationEntity entity = notificationDao.selectById(id);
            if (entity == null) {
                log.info("[通知服务] 通知不存在, id={}", id);
                return Optional.empty();
            }
            log.info("[通知服务] 查询通知成功");
            return Optional.of(NotificationAggregate.from(entity));
        } catch (Exception e) {
            log.error("[通知服务] 查询通知失败, id={}", id, e);
            throw new RuntimeException("查询通知失败", e);
        }
    }

    @Override
    public List<NotificationAggregate> findByUserIdAndType(Long userId, NotificationType type, Pageable pageable) {
        try {
            log.info("[通知服务] 开始分页查询用户通知, userId={}, type={}, pageable={}", userId, type, pageable);
            List<NotificationEntity> entities = notificationDao.findByReceiverIdAndType(userId, type.getValue(), pageable);
            if (entities.isEmpty()) {
                log.info("[通知服务] 未查询到通知记录");
                return Collections.emptyList();
            }
            log.info("[通知服务] 查询到{}条通知记录", entities.size());
            return entities.stream()
                    .map(NotificationAggregate::from)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("[通知服务] 查询用户通知失败, userId={}, type={}", userId, type, e);
            throw new RuntimeException("查询用户通知失败", e);
        }
    }

    @Override
    public long countByUserIdAndIsReadFalse(Long userId) {
        try {
            log.info("[通知服务] 开始统计用户未读通知数量, userId={}", userId);
            long count = notificationDao.countUnreadByReceiverId(userId);
            log.info("[通知服务] 用户未读通知数量为: {}", count);
            return count;
        } catch (Exception e) {
            log.error("[通知服务] 统计用户未读通知数量失败, userId={}", userId, e);
            throw new RuntimeException("统计用户未读通知数量失败", e);
        }
    }

    /**
     * 将指定通知标记为已读
     *
     * @param notificationId 通知ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long notificationId) {
        try {
            log.info("[通知服务] 开始将通知标记为已读, notificationId={}", notificationId);
            notificationDao.markAsRead(notificationId);
            log.info("[通知服务] 标记已读成功");
        } catch (Exception e) {
            log.error("[通知服务] 标记通知为已读失败, notificationId={}", notificationId, e);
            throw new RuntimeException("标记通知为已读失败", e);
        }
    }

    /**
     * 根据用户ID和通知类型分页查询通知
     *
     * @param userId 用户ID
     * @param type 通知类型
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 通知聚合根列表
     */
    @Override
    public List<NotificationAggregate> findByUserIdAndType(Long userId, NotificationType type, int offset, int limit) {
        try {
            log.info("[通知服务] 开始分页查询用户通知, userId={}, type={}, offset={}, limit={}", userId, type, offset, limit);
            List<NotificationEntity> entities = notificationDao.selectByReceiverIdAndType(userId, type.getValue(), offset, limit);
            if (entities.isEmpty()) {
                log.info("[通知服务] 未查询到通知记录");
                return Collections.emptyList();
            }
            log.info("[通知服务] 查询到{}条通知记录", entities.size());
            return entities.stream()
                    .map(NotificationAggregate::from)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("[通知服务] 查询用户通知失败, userId={}, type={}", userId, type, e);
            throw new RuntimeException("查询用户通知失败", e);
        }
    }

    /**
     * 统计用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    @Override
    public long countUnreadByUserId(Long userId) {
        try {
            log.info("[通知服务] 开始统计用户未读通知数量, userId={}", userId);
            long count = notificationDao.countUnreadByReceiverId(userId);
            log.info("[通知服务] 用户未读通知数量为: {}", count);
            return count;
        } catch (Exception e) {
            log.error("[通知服务] 统计用户未读通知数量失败, userId={}", userId, e);
            throw new RuntimeException("统计用户未读通知数量失败", e);
        }
    }

    /**
     * 将用户所有未读通知标记为已读
     *
     * @param userId 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        try {
            log.info("[通知服务] 开始将用户所有未读通知标记为已读, userId={}", userId);
            notificationDao.markAllAsRead(userId);
            log.info("[通知服务] 标记全部已读成功");
        } catch (Exception e) {
            log.error("[通知服务] 标记用户通知为已读失败, userId={}", userId, e);
            throw new RuntimeException("标记用户通知为已读失败", e);
        }
    }

    /**
     * 删除通知
     *
     * @param notificationId 通知ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long notificationId) {
        try {
            log.info("[通知服务] 开始删除通知, notificationId={}", notificationId);
            notificationDao.deleteById(notificationId);
            log.info("[通知服务] 删除通知成功");
        } catch (Exception e) {
            log.error("[通知服务] 删除通知失败, notificationId={}", notificationId, e);
            throw new RuntimeException("删除通知失败", e);
        }
    }

    /**
     * 批量保存通知
     *
     * @param notifications 通知聚合根列表
     * @return 保存后的通知聚合根列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<NotificationAggregate> saveAll(List<NotificationAggregate> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            log.warn("[通知服务] 批量保存通知列表为空");
            return Collections.emptyList();
        }
        
        try {
            log.info("[通知服务] 开始批量保存{}条通知", notifications.size());
            List<NotificationEntity> entities = notifications.stream()
                    .map(NotificationAggregate::getNotification)
                    .peek(entity -> {
                        entity.validate();
                        LocalDateTime now = LocalDateTime.now();
                        entity.setCreatedTime(now);
                        entity.setUpdatedTime(now);
                        entity.setStatus(true); // 设置为有效状态
                    })
                    .collect(Collectors.toList());
            
            notificationDao.batchInsert(entities);
            log.info("[通知服务] 批量保存通知成功");
            return notifications;
        } catch (Exception e) {
            log.error("[通知服务] 批量保存通知失败", e);
            throw new RuntimeException("批量保存通知失败", e);
        }
    }

    /**
     * 检查通知是否存在
     *
     * @param id 通知ID
     * @return 是否存在
     */
    @Override
    public boolean exists(Long id) {
        try {
            log.info("[通知服务] 检查通知是否存在, id={}", id);
            boolean exists = notificationDao.existsById(id);
            log.info("[通知服务] 通知{}存在", exists ? "" : "不");
            return exists;
        } catch (Exception e) {
            log.error("[通知服务] 检查通知是否存在失败, id={}", id, e);
            throw new RuntimeException("检查通知是否存在失败", e);
        }
    }
} 