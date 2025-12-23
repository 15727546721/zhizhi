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
     * 根据用户ID和多类型分页查询通知
     */
    @Override
    public List<Notification> findByUserIdAndTypes(Long userId, List<Integer> types, Pageable pageable) {
        try {
            log.debug("[通知服务] 分页查询用户通知, userId={}, types={}", userId, types);
            List<Notification> notifications = notificationMapper.findByReceiverIdAndTypes(
                    userId, types, (int) pageable.getOffset(), pageable.getPageSize());
            return notifications != null ? notifications : Collections.emptyList();
        } catch (Exception e) {
            log.error("[通知服务] 查询用户通知失败, userId={}, types={}", userId, types, e);
            throw new BusinessException("查询用户通知失败");
        }
    }

    /**
     * 根据用户ID和类型分页查询通知
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
     * 将用户所有未读通知标记为已读（可按类型）
     *
     * @param userId 用户ID
     * @param type   通知类型（可选，为null则标记全部）
     * @throws BusinessException 标记失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId, Integer type) {
        try {
            notificationMapper.markAllAsReadByType(userId, type);
        } catch (Exception e) {
            log.error("[通知服务] 标记用户通知为已读失败, userId={}, type={}", userId, type, e);
            throw new BusinessException("标记用户通知为已读失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long notificationId) {
        try {
            notificationMapper.deleteById(notificationId);
        } catch (Exception e) {
            log.error("[通知服务] 删除通知失败, notificationId={}", notificationId, e);
            throw new BusinessException("删除通知失败");
        }
    }

    @Override
    public boolean exists(Long id) {
        try {
            return notificationMapper.existsById(id);
        } catch (Exception e) {
            log.error("[通知服务] 检查通知是否存在失败, id={}", id, e);
            throw new BusinessException("检查通知是否存在失败");
        }
    }

    @Override
    public List<Notification> findByReceiverIdOrderByCreatedTimeDesc(Long receiverId, int offset, int limit) {
        try {
            List<Notification> notifications = notificationMapper.selectByReceiverIdAndType(receiverId, null, offset, limit);
            return notifications != null ? notifications : Collections.emptyList();
        } catch (Exception e) {
            log.error("[通知服务] 查询接收者通知失败, receiverId={}", receiverId, e);
            throw new BusinessException("查询接收者通知失败");
        }
    }

    @Override
    public long countByReceiverIdAndReadFalse(Long receiverId) {
        return countByUserIdAndIsReadFalse(receiverId);
    }

    @Override
    public Map<Integer, Long> getUnreadCountByType(Long userId) {
        try {
            List<Map<String, Object>> results = notificationMapper.countUnreadByReceiverIdGroupByType(userId);
            Map<Integer, Long> countMap = new HashMap<>();
            if (results != null) {
                for (Map<String, Object> row : results) {
                    Object typeObj = row.get("type");
                    Object countObj = row.get("count");
                    Integer type = (typeObj instanceof Number) ? ((Number) typeObj).intValue() : null;
                    Long count = (countObj instanceof Number) ? ((Number) countObj).longValue() : 0L;
                    if (type != null) {
                        countMap.put(type, count);
                    }
                }
            }
            log.info("[通知服务] 获取各类型未读通知数量: {}", countMap);
            return countMap;
        } catch (Exception e) {
            log.error("[通知服务] 获取各类型未读通知数量失败, userId={}", userId, e);
            throw new BusinessException("获取各类型未读通知数量失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> notificationIds) {
        try {
            if (notificationIds == null || notificationIds.isEmpty()) {
                return;
            }
            notificationMapper.batchDeleteByIds(notificationIds);
        } catch (Exception e) {
            log.error("[通知服务] 批量删除通知失败, ids={}", notificationIds, e);
            throw new BusinessException("批量删除通知失败");
        }
    }

    @Override
    public long countByUserIdAndType(Long userId, Integer type) {
        try {
            return notificationMapper.countByReceiverIdAndType(userId, type);
        } catch (Exception e) {
            log.error("[通知服务] 统计用户通知数量失败, userId={}, type={}", userId, type, e);
            throw new BusinessException("统计用户通知数量失败");
        }
    }

    @Override
    public long countByUserIdAndTypes(Long userId, List<Integer> types) {
        try {
            return notificationMapper.countByReceiverIdAndTypes(userId, types);
        } catch (Exception e) {
            log.error("[通知服务] 统计用户通知数量失败, userId={}, types={}", userId, types, e);
            throw new BusinessException("统计用户通知数量失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteByUserIdAndIds(Long userId, List<Long> notificationIds) {
        try {
            if (notificationIds == null || notificationIds.isEmpty()) {
                return;
            }
            notificationMapper.batchDeleteByReceiverIdAndIds(userId, notificationIds);
        } catch (Exception e) {
            log.error("[通知服务] 批量删除用户通知失败, userId={}, ids={}", userId, notificationIds, e);
            throw new BusinessException("批量删除用户通知失败");
        }
    }
}