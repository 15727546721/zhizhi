package cn.xu.domain.like.model;

import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.domain.like.model.LikeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import java.time.LocalDateTime;

/**
 * 点赞实体
 * 封装点赞的业务逻辑和状态管理
 * 
 * @author xu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeEntity {
    /**
     * 点赞ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 点赞类型：1-文章，2-话题，3-评论等
     */
    private LikeType type;

    /**
     * 是否点赞，1-点赞，0-取消点赞
     */
    private LikeStatus status;

    /**
     * 点赞时间
     */
    private LocalDateTime createTime;
    
    // ==================== 业务方法 ====================
    
    /**
     * 创建新的点赞记录
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 点赞类型
     * @return 点赞实体
     */
    public static LikeEntity createLike(Long userId, Long targetId, LikeType type) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID不能为空");
        }
        if (targetId == null || targetId <= 0) {
            throw new BusinessException("目标ID不能为空");
        }
        if (type == null) {
            throw new BusinessException("点赞类型不能为空");
        }
        
        return LikeEntity.builder()
                .userId(userId)
                .targetId(targetId)
                .type(type)
                .status(LikeStatus.LIKED) // 表示点赞
                .createTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 取消点赞
     */
    public void cancelLike() {
        this.status = LikeStatus.UNLIKED; // 表示取消点赞
    }
    
    /**
     * 重新点赞
     */
    public void doLike() {
        this.status = LikeStatus.LIKED; // 表示点赞
        this.createTime = LocalDateTime.now();
    }
    
    /**
     * 判断是否已点赞
     * 
     * @return true表示已点赞，false表示未点赞
     */
    public boolean isLiked() {
        return this.status != null && this.status == LikeStatus.LIKED;
    }
    
    /**
     * 验证点赞的有效性
     */
    public void validate() {
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID不能为空");
        }
        if (targetId == null || targetId <= 0) {
            throw new BusinessException("目标ID不能为空");
        }
        if (type == null) {
            throw new BusinessException("点赞类型不能为空");
        }
    }
    
    /**
     * 获取点赞类型的代码值
     * 
     * @return 类型代码
     */
    public Integer getTypeCode() {
        return type != null ? type.getCode() : null;
    }
    
    /**
     * 设置点赞类型
     * 
     * @param typeCode 类型代码
     */
    public void setTypeFromCode(Integer typeCode) {
        this.type = LikeType.valueOf(typeCode);
    }
}