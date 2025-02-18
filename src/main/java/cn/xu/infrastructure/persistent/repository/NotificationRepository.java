package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.notification.model.aggregate.NotificationAggregate;
import cn.xu.domain.notification.model.entity.NotificationEntity;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import cn.xu.domain.notification.repository.INotificationRepository;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.persistent.dao.INotificationDao;
import cn.xu.infrastructure.persistent.po.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知仓储实现类
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class NotificationRepository implements INotificationRepository {

    private final INotificationDao notificationDao;

    /**
     * 保存通知
     *
     * @param entity 通知实体
     * @return 保存后的通知聚合根
     * @throws BusinessException 保存失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationAggregate save(NotificationEntity entity) {
        try {
            // 验证实体有效性
            entity.validate();

            if (entity.getId() == null) {
                log.info("[通知服务] 开始新增通知, receiverId={}, type={}", entity.getReceiverId(), entity.getType());
                entity.setStatus(true); // 设置为有效状态
                notificationDao.insert(
                        Notification.builder()
                                .receiverId(entity.getReceiverId())
                                .senderId(entity.getSenderId())
                                .type(entity.getType().getValue())
                                .title(entity.getTitle())
                                .content(entity.getContent())
                                .businessType(entity.getBusinessType().getValue())
                                .businessId(entity.getBusinessId())
                                .isRead(0)
                                .status(0)
                                .createTime(entity.getCreateTime())
                                .build()
                );
                log.info("[通知服务] 新增通知成功, id={}", entity.getId());
            } else {
                log.info("[通知服务] 开始更新通知, id={}", entity.getId());
                notificationDao.update(entity);
                log.info("[通知服务] 更新通知成功");
            }
            return NotificationAggregate.from(entity);
        } catch (Exception e) {
            log.error("[通知服务] 保存通知失败", e);
            throw new BusinessException("保存通知失败");
        }
    }

    /**
     * 根据ID查询通知
     *
     * @param id 通知ID
     * @return 通知聚合根
     * @throws BusinessException 查询失败时抛出
     */
    @Override
    public NotificationEntity findById(Long id) {
        try {
            log.info("[通知服务] 开始查询通知, id={}", id);
            NotificationEntity entity = notificationDao.selectById(id);
            if (entity == null) {
                log.info("[通知服务] 通知不存在, id={}", id);
                throw new BusinessException("通知不存在");
            }
            log.info("[通知服务] 查询通知成功");
            return entity;
        } catch (Exception e) {
            log.error("[通知服务] 查询通知失败, id={}", id, e);
            throw new BusinessException("查询通知失败");
        }
    }

    /**
     * 根据用户ID和类型分页查询通知
     *
     * @param userId   用户ID
     * @param type     通知类型
     * @param pageable 分页信息
     * @return 通知聚合根列表
     * @throws BusinessException 查询失败时抛出
     */
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
            throw new BusinessException("查询用户通知失败");
        }
    }

    /**
     * 统计用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     * @throws BusinessException 统计失败时抛出
     */
    @Override
    public long countByUserIdAndIsReadFalse(Long userId) {
        try {
            log.info("[通知服务] 开始统计用户未读通知数量, userId={}", userId);
            long count = notificationDao.countUnreadByReceiverId(userId);
            log.info("[通知服务] 用户未读通知数量为: {}", count);
            return count;
        } catch (Exception e) {
            log.error("[通知服务] 统计用户未读通知数量失败, userId={}", userId, e);
            throw new BusinessException("统计用户未读通知数量失败");
        }
    }

    /**
     * 将指定通知标记为已读
     *
     * @param notificationId 通知ID
     * @throws BusinessException 标记失败时抛出
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
            throw new BusinessException("标记通知为已读失败");
        }
    }

    /**
     * 根据用户ID和通知类型分页查询通知
     *
     * @param userId 用户ID
     * @param type   通知类型
     * @param offset 偏移量
     * @param limit  限制数量
     * @return 通知聚合根列表
     * @throws BusinessException 查询失败时抛出
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
            throw new BusinessException("查询用户通知失败");
        }
    }

    /**
     * 统计用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     * @throws BusinessException 统计失败时抛出
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
            throw new BusinessException("统计用户未读通知数量失败");
        }
    }

    /**
     *
     * 将用户所有未读通知标记为已读
     * @param userId 用户ID
     * @throws BusinessException 标记失败时抛出
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
            throw new BusinessException("标记用户通知为已读失败");
        }
    }

    /**
     * 删除通知
     *
     * @param notificationId 通知ID
     * @throws BusinessException 删除失败时抛出
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
            throw new BusinessException("删除通知失败");
        }
    }

    /**
     * 检查通知是否存在
     *
     * @param id 通知ID
     * @return 是否存在
     * @throws BusinessException 检查失败时抛出
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
            throw new BusinessException("检查通知是否存在失败");
        }
    }

    /**
     * 根据接收者ID分页查询通知，按创建时间降序
     *
     * @param receiverId 接收者ID
     * @param offset     偏移量
     * @param limit      限制数量
     * @return 通知聚合根列表
     */
    @Override
    public List<NotificationAggregate> findByReceiverIdOrderByCreatedTimeDesc(Long receiverId, int offset, int limit) {
        return null;
    }

    /**
     * 统计接收者未读通知数量
     *
     * @param receiverId 接收者ID
     * @return 未读通知数量
     */
    @Override
    public long countByReceiverIdAndReadFalse(Long receiverId) {
        return 0;
    }
} 