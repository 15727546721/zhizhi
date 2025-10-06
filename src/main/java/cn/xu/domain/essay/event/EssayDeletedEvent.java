package cn.xu.domain.essay.event;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 随笔删除事件
 */
@Data
@Builder
public class EssayDeletedEvent {
    private final Long essayId;        // 被删除随笔ID
    private final Long userId;         // 删除用户ID
    private final LocalDateTime deletedTime; // 删除时间
}