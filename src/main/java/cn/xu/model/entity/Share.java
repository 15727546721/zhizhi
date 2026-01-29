package cn.xu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分享记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Share implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========== 分享平台常量 ==========
    public static final String PLATFORM_COPY = "copy";      // 复制链接
    public static final String PLATFORM_WEIBO = "weibo";    // 微博
    public static final String PLATFORM_QQ = "qq";          // QQ
    public static final String PLATFORM_WECHAT = "wechat";  // 微信
    public static final String PLATFORM_OTHER = "other";    // 其他

    // ========== 字段 ==========

    /**
     * 分享ID
     */
    private Long id;

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 分享用户ID（可为空，未登录用户）
     */
    private Long userId;

    /**
     * 分享平台
     */
    private String platform;

    /**
     * 分享者IP
     */
    private String ip;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    // ========== 工厂方法 ==========

    /**
     * 创建分享记录
     */
    public static Share create(Long postId, Long userId, String platform, String ip, String userAgent) {
        return Share.builder()
                .postId(postId)
                .userId(userId)
                .platform(platform != null ? platform : PLATFORM_OTHER)
                .ip(ip)
                .userAgent(userAgent)
                .createTime(LocalDateTime.now())
                .build();
    }

    // ========== 业务方法 ==========

    /**
     * 验证平台是否有效
     */
    public static boolean isValidPlatform(String platform) {
        return PLATFORM_COPY.equals(platform)
                || PLATFORM_WEIBO.equals(platform)
                || PLATFORM_QQ.equals(platform)
                || PLATFORM_WECHAT.equals(platform)
                || PLATFORM_OTHER.equals(platform);
    }

    /**
     * 获取平台显示名称
     */
    public String getPlatformDisplayName() {
        return switch (platform) {
            case PLATFORM_COPY -> "复制链接";
            case PLATFORM_WEIBO -> "微博";
            case PLATFORM_QQ -> "QQ";
            case PLATFORM_WECHAT -> "微信";
            default -> "其他";
        };
    }
}
