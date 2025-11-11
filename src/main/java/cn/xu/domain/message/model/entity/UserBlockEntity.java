package cn.xu.domain.message.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户屏蔽实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBlockEntity {
    /**
     * 屏蔽关系ID
     */
    private Long id;
    
    /**
     * 用户ID（屏蔽发起者）
     */
    private Long userId;
    
    /**
     * 被屏蔽用户ID
     */
    private Long blockedUserId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 验证屏蔽关系
     */
    public static void validateBlockRelation(Long userId, Long blockedUserId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID cannot be null or zero");
        }
        if (blockedUserId == null || blockedUserId <= 0) {
            throw new IllegalArgumentException("Blocked user ID cannot be null or zero");
        }
        if (userId.equals(blockedUserId)) {
            throw new IllegalArgumentException("User cannot block themselves");
        }
    }
    
    /**
     * 创建屏蔽关系
     */
    public static UserBlockEntity create(Long userId, Long blockedUserId) {
        validateBlockRelation(userId, blockedUserId);
        return UserBlockEntity.builder()
                .userId(userId)
                .blockedUserId(blockedUserId)
                .createTime(LocalDateTime.now())
                .build();
    }
}

