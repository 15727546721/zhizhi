package cn.xu.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户屏蔽VO
 */
@Data
public class UserBlockVO {
    
    /**
     * 屏蔽关系ID
     */
    private Long id;
    
    /**
     * 被屏蔽用户ID
     */
    private Long blockedUserId;
    
    /**
     * 被屏蔽用户昵称
     */
    private String blockedUserName;
    
    /**
     * 被屏蔽用户头像
     */
    private String blockedUserAvatar;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

