package cn.xu.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 关注用户信息VO
 * 用于前端显示关注/粉丝列表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowUserVO {

    /**
     * 关注关系ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 用户描述
     */
    private String description;

    /**
     * 用户名（用于@提及）
     */
    private String username;

    /**
     * 关注时间
     */
    private LocalDateTime followTime;

    /**
     * 是否正在关注（当前用户对此用户的关注状态）
     */
    private Boolean isFollowing;

    /**
     * 粉丝数
     */
    private Long fansCount;

    /**
     * 关注数
     */
    private Long followCount;
}
