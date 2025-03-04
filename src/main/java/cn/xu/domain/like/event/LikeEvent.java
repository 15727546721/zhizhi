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
    private boolean liked;
    private LocalDateTime occurredTime;
}
