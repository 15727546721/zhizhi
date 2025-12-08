package cn.xu.model.entity;

import cn.xu.support.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户关注标签PO（持久化对象）
 * 简化设计：只包含核心字段，直接承载业务逻辑
 * 
 * 用途：
 * - 用户关注感兴趣的标签/领域
 * - 系统根据关注的标签推荐相关内容
 * - 标签页面显示关注该标签的用户数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInterestedTag implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 标签ID
     */
    private Long tagId;
    
    /**
     * 关注时间
     */
    private LocalDateTime createTime;
    
    // ==================== 业务方法 ====================
    
    /**
     * 创建新的关注关系
     * 
     * @param userId 用户ID
     * @param tagId 标签ID
     * @return 用户关注标签对象
     */
    public static UserInterestedTag create(Long userId, Long tagId) {
        validateParams(userId, tagId);
        
        return UserInterestedTag.builder()
                .userId(userId)
                .tagId(tagId)
                .createTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 验证参数
     * 
     * @param userId 用户ID
     * @param tagId 标签ID
     */
    public static void validateParams(Long userId, Long tagId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID不能为空");
        }
        if (tagId == null || tagId <= 0) {
            throw new BusinessException("标签ID不能为空");
        }
    }
    
    /**
     * 判断关注关系是否有效
     * 
     * @return 是否有效
     */
    public boolean isValid() {
        return userId != null && userId > 0 
            && tagId != null && tagId > 0;
    }
}
