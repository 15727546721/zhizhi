package cn.xu.domain.like.event;

import cn.xu.domain.like.model.LikeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 点赞事件对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeEvent {
    private Long userId;
    private Long targetId;
    private LikeType type;
    private Boolean status;
    private LocalDateTime createTime;
    private boolean processingFlag = true; // 默认需要处理
    private Throwable errorCause;          // 异常信息

    // 标记处理失败
    public void markAsFailed(Throwable cause) {
        this.processingFlag = false;
        this.errorCause = cause;
    }

    public boolean shouldProcess() {
        return processingFlag;
    }
}
