package cn.xu.repository.impl;

import cn.xu.model.entity.Notification;
import cn.xu.repository.INotificationRepository;
import cn.xu.repository.mapper.NotificationMapper;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知仓储实现
 *
 * @author xu
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class NotificationRepository implements INotificationRepository {

    private final NotificationMapper notificationMapper;

    /**
     * 保存通知
     *
     * @param notification 通知PO
     * @return 保存后的通知
     * @throws BusinessException 保存失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Notification save(Notification notification) {
        try {
            // 验证通知有效性
            notification.validate();

            if (notification.getId() == null) {
                log.info("[通知服务] 开始新增通知, receiverId={}, type={}", notification.getReceiverId(), notification.getType());
                if (notification.getStatus() == null) {
                    notification.setStatus(Notification.STATUS_VALID);
                }
                notificationMapper.insert(notification);
                log.info("[通知服务] 新增通知成功, id={}", notification.getId());
            } else {
                log.info("[通知服务] 开始更新通知, id={}", notification.getId());
                notificationMapper.update(notification);
                log.info("[通知服务] 更新通知成功");
            }
            return notification;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[通知服务] 保存通知失败", e);
            throw new BusinessException("保存通知失败");
        }
    }

    /**
     * 根据ID查询通知
     *
     * @param id 通知ID
     * @return 通知PO
     * @throws BusinessException 查询失败时抛出
     */
    @Override
    public Notification findById(Long id) {
        try {
            log.info("[通知服务] 开始查询通知, id={}", id);
            Notification notification = notificationMapper.selectById(id);
            if (notification == null) {
                log.info("[通知服务] 通知不存在, id={}", id);
                return null;
            }
            log.info("[通知服务] 查询通知成功");
            return notification;
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
     * @return 通知PO列表
     * @throws BusinessException 查询失败时抛出
     */
    @Override
    public List<Notification> findByUserIdAndType(Long userId, Integer type, Pageable pageable) {
        try {
            log.info("[通知服务] 开始分页查询用户通知, userId={}, type={}, pageable={}", userId, type, pageable);
            List<Notification> notifications = notificationMapper.findByReceiverIdAndType(userId, type, pageable);
            if (notifications == null || notifications.isEmpty()) {
                log.info("[通知服务] 未查询到通知记录");
                return Collections.emptyList();
            }
            log.info("[通知服务] 查询到{}条通知记录", notifications.size());
            return notifications;
        } catch (Exception e) {
            log.error("[通知服务] 查询用户通知失败, userId={}, type={}", userId, type, e);
            throw new BusinessException("查询用户通知失败");
        }
    }

    /**
     * 根据用户ID和类型分页查询通知
     *
     * @param userId   用户ID
     * @param type     通知类型
     * @param pageable 分页信息
     * @return 通知PO列表
     * @return 未读通知数量
     * @throws BusinessException 统计失败时抛出
     */
    @Override
    public long countByUserIdAndIsReadFalse(Long userId) {
        try {
            log.info("[通知服务] 开始统计用户未读通知数量, userId={}", userId);
            long count = notificationMapper.countUnreadByReceiverId(userId);
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
            notificationMapper.markAsRead(notificationId);
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
     * @return 通知PO列表
     * @throws BusinessException 查询失败时抛出
     */
    @Override
    public List<Notification> findByUserIdAndType(Long userId, Integer type, int offset, int limit) {
        try {
            log.info("[通知服务] 开始分页查询用户通知, userId={}, type={}, offset={}, limit={}", userId, type, offset, limit);
            List<Notification> notifications = notificationMapper.selectByReceiverIdAndType(userId, type, offset, limit);
            if (notifications == null || notifications.isEmpty()) {
                log.info("[通知服务] 未查询到通知记录");
                return Collections.emptyList();
            }
            log.info("[通知服务] 查询到{}条通知记录", notifications.size());
            return notifications;
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
            long count = notificationMapper.countUnreadByReceiverId(userId);
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
            notificationMapper.markAllAsRead(userId);
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
            notificationMapper.deleteById(notificationId);
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
            boolean exists = notificationMapper.existsById(id);
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
     * @return 通知PO列表
     */
    @Override
    public List<Notification> findByReceiverIdOrderByCreatedTimeDesc(Long receiverId, int offset, int limit) {
        try {
            log.info("[通知服务] 开始分页查询接收者通知, receiverId={}, offset={}, limit={}", receiverId, offset, limit);
            List<Notification> notifications = notificationMapper.selectByReceiverIdAndType(receiverId, null, offset, limit);
            if (notifications == null || notifications.isEmpty()) {
                log.info("[通知服务] 未查询到通知记录");
                return Collections.emptyList();
            }
            log.info("[通知服务] 查询到{}条通知记录", notifications.size());
            return notifications;
        } catch (Exception e) {
            log.error("[通知服务] 查询接收者通知失败, receiverId={}", receiverId, e);
            throw new BusinessException("查询接收者通知失败");
        }
    }

    /**
     * 统计接收者未读通知数量
     *
     * @param receiverId 接收者ID
     * @return 未读通知数量
     */
    @Override
    public long countByReceiverIdAndReadFalse(Long receiverId) {
        return countByUserIdAndIsReadFalse(receiverId);
    }

    /**
     * 获取各类型未读通知数量
     *
     * @param userId 用户ID
     * @return 类型到数量的映射
     */
    @Override
    public Map<Integer, Long> getUnreadCountByType(Long userId) {
        try {
            log.info("[通知服务] 开始获取各类型未读通知数量, userId={}", userId);
            List<Map<String, Object>> results = notificationMapper.countUnreadByReceiverIdGroupByType(userId);
            Map<Integer, Long> countMap = new HashMap<>();
            if (results != null) {
                for (Map<String, Object> row : results) {
                    Integer type = ((Number) row.get("type")).intValue();
                    Long count = ((Number) row.get("count")).longValue();
                    countMap.put(type, count);
                }
            }
            log.info("[通知服务] 获取各类型未读通知数量成功: {}", countMap);
            return countMap;
        } catch (Exception e) {
            log.error("[通知服务] 获取各类型未读通知数量失败, userId={}", userId, e);
            throw new BusinessException("获取各类型未读通知数量失败");
        }
    }

    /**
     * 批量删除通知
     *
     * @param notificationIds 通知ID列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> notificationIds) {
        try {
            if (notificationIds == null || notificationIds.isEmpty()) {
                log.info("[通知服务] 批量删除通知，ID列表为空");
                return;
            }
            log.info("[通知服务] 开始批量删除通知, count={}", notificationIds.size());
            notificationMapper.batchDeleteByIds(notificationIds);
            log.info("[通知服务] 批量删除通知成功");
        } catch (Exception e) {
            log.error("[通知服务] 批量删除通知失败, ids={}", notificationIds, e);
            throw new BusinessException("批量删除通知失败");
        }
    }
}