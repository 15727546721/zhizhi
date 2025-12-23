package cn.xu.repository;

/**
 * 打招呼记录仓储接口
 */
public interface IGreetingRecordRepository {
    
    /**
     * 记录打招呼（发送敲门消息）
     *
     * @param userId 发送者ID
     * @param targetId 接收者ID
     */
    void markGreetingSent(Long userId, Long targetId);
    
    /**
     * 检查是否已发送过打招呼消息
     *
     * @param userId 发送者ID
     * @param targetId 接收者ID
     * @return 是否已发送
     */
    boolean hasSentGreeting(Long userId, Long targetId);
    
    /**
     * 删除打招呼记录（用于清理）
     *
     * @param userId 发送者ID
     * @param targetId 接收者ID
     */
    void deleteGreeting(Long userId, Long targetId);
    
    /**
     * 双向删除打招呼记录（互关后清理）
     *
     * @param userId1 用户1
     * @param userId2 用户2
     */
    void deleteBidirectionalGreeting(Long userId1, Long userId2);
}
