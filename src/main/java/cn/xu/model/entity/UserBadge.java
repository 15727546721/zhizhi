package cn.xu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户勋章持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBadge {
    private Long id;
    private Long userId;
    private Long badgeId; // 勋章ID
    private String badgeName; // 勋章名称
    private String badgeDescription; // 勋章描述
    private String badgeIcon; // 勋章图标
    private Integer status; // 状态：0-未获得，1-已获得，2-已失效
    private LocalDateTime obtainTime; // 获得时间
    private LocalDateTime expireTime; // 过期时间
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}