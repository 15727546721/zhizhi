package cn.xu.event.like;

import cn.xu.model.entity.Like.LikeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 点赞事件对象（简化架构版本）
 * 直接使用PO内的LikeType枚举，移除domain层的冗余类型
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
}
