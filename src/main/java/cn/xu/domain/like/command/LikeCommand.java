package cn.xu.domain.like.command;

import cn.xu.domain.like.model.LikeType;
import lombok.Builder;
import lombok.Data;

/**
 * 点赞命令对象
 */
@Data
@Builder
public class LikeCommand {
    private final Long userId;
    private final Long targetId;
    private final LikeType type;
} 