package cn.xu.model.entity;

import cn.xu.support.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户屏蔽表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBlock implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
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
    
    // ==================== 业务方法 ====================
    
    /**
     * 验证屏蔽关系
     */
    public static void validateBlockRelation(Long userId, Long blockedUserId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID不能为空或零");
        }
        if (blockedUserId == null || blockedUserId <= 0) {
            throw new BusinessException("被屏蔽用户ID不能为空或零");
        }
        if (userId.equals(blockedUserId)) {
            throw new BusinessException("用户不能屏蔽自己");
        }
    }
    
    /**
     * 创建屏蔽关系
     */
    public static UserBlock create(Long userId, Long blockedUserId) {
        validateBlockRelation(userId, blockedUserId);
        return UserBlock.builder()
                .userId(userId)
                .blockedUserId(blockedUserId)
                .createTime(LocalDateTime.now())
                .build();
    }
}