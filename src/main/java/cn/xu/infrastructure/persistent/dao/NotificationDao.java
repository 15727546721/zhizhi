package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationDao {

    /**
     * 插入一条新的通知到数据库。
     *
     * @param notification 要插入的通知
     */
    void insert(Notification notification);

    /**
     * 更新数据库中的一条通知。
     *
     * @param notification 要更新的通知
     */
    void update(Notification notification);

    /**
     * 根据ID查找通知。
     *
     * @param id 通知的ID
     * @return 指定ID的通知
     */
    Notification findById(@Param("id") Long id);

    /**
     * 根据接收者ID查找通知，按创建时间降序排列。
     *
     * @param receiverId 接收者的ID
     * @param offset 分页偏移量
     * @param limit 返回的最大结果数
     * @return 指定接收者的通知列表
     */
    List<Notification> findByReceiverIdOrderByCreatedTimeDesc(
            @Param("receiverId") Long receiverId,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    /**
     * 统计接收者未读通知的数量。
     *
     * @param receiverId 接收者的ID
     * @return 未读通知的数量
     */
    Long countByReceiverIdAndReadFalse(@Param("receiverId") Long receiverId);

    /**
     * 将用户的所有通知标记为已读。
     *
     * @param userId 用户的ID
     */
    void markAllAsRead(@Param("userId") Long userId);
} 