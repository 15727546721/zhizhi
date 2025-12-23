package cn.xu.task;

import cn.xu.repository.mapper.PrivateMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 消息清理定时任务
 * 
 * <p>功能：
 * <ul>
 *   <li>清理双方均已删除的消息（物理删除）</li>
 * </ul>
 *
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageCleanupTask {
    
    private final PrivateMessageMapper messageMapper;
    
    /**
     * 每天凌晨3点执行清理任务
     * 删除双方均已删除的消息，节省存储空间
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanDeletedMessages() {
        log.info("[消息清理] 开始执行定时清理任务...");
        
        try {
            // 物理删除双方均已删除的消息
            int deletedCount = messageMapper.deleteBothDeletedMessages();
            log.info("[消息清理] 完成，删除了 {} 条双方都已删除的消息", deletedCount);
        } catch (Exception e) {
            log.error("[消息清理] 执行失败", e);
        }
    }
    
    /**
     * 手动触发清理（供管理后台调用）
     * @return 删除的消息数量
     */
    public int manualCleanup() {
        log.info("[消息清理] 手动触发清理...");
        int deletedCount = messageMapper.deleteBothDeletedMessages();
        log.info("[消息清理] 手动清理完成，删除了 {} 条消息", deletedCount);
        return deletedCount;
    }
}