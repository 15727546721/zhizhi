package cn.xu.domain.like.command;

import cn.xu.domain.like.model.LikeType;
import lombok.Builder;
import lombok.Data;

/**
 * 点赞命令
 */
@Data
@Builder
public class LikeCommand {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 点赞类型
     */
    private LikeType type;

    /**
     * 验证命令是否有效
     */
    public boolean isValid() {
        return userId != null && userId > 0
                && targetId != null && targetId > 0
                && type != null;
    }
} 