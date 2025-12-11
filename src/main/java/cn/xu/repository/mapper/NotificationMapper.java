package cn.xu.repository.mapper;

import cn.xu.model.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知Mapper接口
 * <p>处理通知相关的数据库操作</p>

 */
@Mapper
public interface NotificationMapper {
    
    /**
     * 保存通知
     */
    void insert(Notification notification);
    
    /**
     * 更新通知
     */
    void update(Notification notification);
    
    /**
     * 根据ID查询通知
     */
    Notification selectById(@Param("id") Long id);
    
    /**
     * 根据接收者ID和类型分页查询通知
     */
    List<Notification> findByReceiverIdAndType(
            @Param("receiverId") Long receiverId,
            @Param("type") Integer type,
            @Param("pageable") org.springframework.data.domain.Pageable pageable);
    
    /**
     * 根据接收者ID和类型分页查询通知（使用offset和limit）
     */
    List<Notification> selectByReceiverIdAndType(
            @Param("receiverId") Long receiverId,
            @Param("type") Integer type,
            @Param("offset") int offset,
            @Param("limit") int limit);
    
    /**
     * 统计用户未读通知数量
     */
    long countUnreadByReceiverId(@Param("receiverId") Long receiverId);
    
    /**
     * 将通知标记为已读
     */
    void markAsRead(@Param("id") Long id);
    
    /**
     * 将用户的所有通知标记为已读
     */
    void markAllAsRead(@Param("receiverId") Long receiverId);
    
    /**
     * 删除通知
     */
    void deleteById(@Param("id") Long id);
    
    /**
     * 检查通知是否存在
     */
    boolean existsById(@Param("id") Long id);
    
    /**
     * 统计用户各类型未读通知数量
     */
    List<java.util.Map<String, Object>> countUnreadByReceiverIdGroupByType(@Param("receiverId") Long receiverId);
    
    /**
     * 批量删除通知
     */
    void batchDeleteByIds(@Param("ids") List<Long> ids);

    /**
     * 将用户的通知标记为已读（可按类型）
     */
    void markAllAsReadByType(@Param("receiverId") Long receiverId, @Param("type") Integer type);

    /**
     * 统计用户通知数量（按类型）
     */
    long countByReceiverIdAndType(@Param("receiverId") Long receiverId, @Param("type") Integer type);

    /**
     * 根据用户ID和多类型分页查询通知
     */
    List<Notification> findByReceiverIdAndTypes(
            @Param("receiverId") Long receiverId,
            @Param("types") List<Integer> types,
            @Param("offset") int offset,
            @Param("limit") int limit);

    /**
     * 统计用户通知数量（按多类型）
     */
    long countByReceiverIdAndTypes(@Param("receiverId") Long receiverId, @Param("types") List<Integer> types);

    /**
     * 批量删除通知（带用户ID校验）
     */
    void batchDeleteByReceiverIdAndIds(@Param("receiverId") Long receiverId, @Param("ids") List<Long> ids);

    // ==================== 管理后台方法 ====================

    /**
     * 分页查询所有通知（管理后台用）
     */
    List<Notification> selectAllNotifications(
            @Param("type") Integer type,
            @Param("isRead") Integer isRead,
            @Param("offset") int offset,
            @Param("limit") int limit);

    /**
     * 统计所有通知数量（管理后台用）
     */
    long countAllNotifications(@Param("type") Integer type, @Param("isRead") Integer isRead);
}
