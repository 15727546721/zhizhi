package cn.xu.model.entity;

import cn.xu.common.ResponseCode;
import cn.xu.support.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 评论实体
 * <p>帖子评论和回复</p>
 
 */
@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment implements Serializable {
    
    // ========== 数据库字段 ==========
    
    /** 评论ID */
    private Long id;
    
    /** 目标类型 */
    private Integer targetType;
    
    /** 目标ID */
    private Long targetId;
    
    /** 父评论ID */
    private Long parentId;
    
    /** 用户ID */
    private Long userId;
    
    /** 被回复用户ID */
    private Long replyUserId;
    
    /** 评论内容 */
    private String content;
    
    /** 图片URL（数据库存储，逗号分隔） */
    private String imageUrl;
    
    /** 点赞数 */
    private Long likeCount;
    
    /** 回复数 */
    private Long replyCount;
    
    /** 热度分数 */
    private BigDecimal hotScore;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    // ========== 运行时字段（不存数据库） ==========
    
    /** 评论用户信息 */
    private transient User user;
    
    /** 被回复用户信息 */
    private transient User replyUser;
    
    /** 子评论列表 */
    @Builder.Default
    private transient List<Comment> children = new ArrayList<>();
    
    /** 是否热门评论 */
    private transient boolean isHot;
    
    // ========== 业务方法 ==========
    
    /**
     * 获取图片URL列表
     */
    public List<String> getImageUrls() {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(imageUrl.split(","));
    }
    
    /**
     * 设置图片URL列表
     */
    public void setImageUrls(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            this.imageUrl = null;
        } else {
            this.imageUrl = String.join(",", urls);
        }
    }
    
    /**
     * 获取评论内容预览（50字以内）
     */
    public String getPreviewContent() {
        if (content == null) return "";
        return content.length() <= 50 ? content : content.substring(0, 50) + "...";
    }
    
    /**
     * 是否包含图片
     */
    public boolean hasImages() {
        return imageUrl != null && !imageUrl.isEmpty();
    }
    
    /**
     * 获取图片数量
     */
    public int getImageCount() {
        return getImageUrls().size();
    }
    
    /**
     * 是否顶级评论
     */
    public boolean isTopLevel() {
        return parentId == null || parentId == 0;
    }
    
    // ========== 验证方法 ==========
    
    /**
     * 验证评论参数
     */
    public static void validateParams(Integer targetType, Long targetId, Long userId, String content) {
        if (targetType == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "评论类型不能为空");
        }
        if (targetId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "目标ID不能为空");
        }
        if (userId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "用户ID不能为空");
        }
        validateContent(content);
    }
    
    /**
     * 验证评论内容
     */
    public static void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "评论内容不能为空");
        }
        if (content.length() > 2000) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "评论内容长度不能超过2000字");
        }
    }
}