package cn.xu.domain.comment.model.entity;

import cn.xu.common.ResponseCode;
import cn.xu.common.exception.BusinessException;
import cn.xu.domain.comment.model.valueobject.CommentContent;
import cn.xu.domain.comment.model.valueobject.CommentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 评论领域实体
 * 封装评论相关的业务逻辑和规则
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentEntity {
    /**
     * 评论ID
     */
    private Long id;

    /**
     * 评论类型，如1-文章；2-话题
     */
    private Integer targetType;

    /**
     * 评论来源的标识符
     */
    private Long targetId;

    /**
     * 父评论的唯一标识符，顶级评论为NULL
     */
    private Long parentId;

    /**
     * 发表评论的用户ID
     */
    private Long userId;

    /**
     * 回复的用户ID，若为回复评论则存在
     */
    private Long replyUserId;

    /**
     * 评论的具体内容
     */
    private CommentContent content;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 子评论数
     */
    private Long replyCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    private List<String> imageUrls = new ArrayList<>(); // 图片URL列表
    private List<CommentEntity> children = new ArrayList<>(); // 子评论

    private double hotScore; // 热度分数
    private boolean isHot; // 是否热门评论

    // 添加用户信息字段
    private cn.xu.domain.user.model.entity.UserEntity user; // 评论用户信息
    private cn.xu.domain.user.model.entity.UserEntity replyUser; // 被回复用户信息

    /**
     * 验证评论参数
     */
    public static void validateCommentParams(Integer targetType, Long targetId, Long userId, String content) {
        if (targetType == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "评论类型不能为空");
        }
        
        if (targetId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "目标ID不能为空");
        }
        
        if (userId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "用户ID不能为空");
        }
        
        // CommentContent的构造函数会验证content参数
        new CommentContent(content);
        
        // 验证目标类型是否支持
        CommentType.valueOf(targetType);
    }

    // ==================== 业务方法 ====================
    
    /**
     * 获取评论预览内容（截取前50个字符）
     * 
     * @return 预览内容
     */
    public String getPreviewContent() {
        if (content == null) {
            return "";
        }
        String contentValue = content.getValue();
        if (contentValue.length() <= 50) {
            return contentValue;
        }
        return contentValue.substring(0, 50) + "...";
    }
    
    /**
     * 获取格式化的内容（处理换行符等）
     * 
     * @return 格式化后的内容
     */
    public String getFormattedContent() {
        if (content == null) {
            return "";
        }
        // 将换行符替换为HTML的<br>标签
        return content.getValue().replace("\n", "<br>");
    }
    
    /**
     * 判断评论是否包含图片
     * 
     * @return true表示包含图片，false表示不包含
     */
    public boolean hasImages() {
        return imageUrls != null && !imageUrls.isEmpty();
    }
    
    /**
     * 获取图片数量
     * 
     * @return 图片数量
     */
    public int getImageCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }

    // ==================== 兼容性方法 ====================
    
    /**
     * 获取被回复用户信息
     * 
     * @return 被回复用户信息
     */
    public cn.xu.domain.user.model.entity.UserEntity getReplyUser() {
        return replyUser;
    }
    
    /**
     * 设置被回复用户信息
     * 
     * @param replyUser 被回复用户信息
     */
    public void setReplyUser(cn.xu.domain.user.model.entity.UserEntity replyUser) {
        this.replyUser = replyUser;
    }
    
    /**
     * 获取评论用户信息
     * 
     * @return 评论用户信息
     */
    public cn.xu.domain.user.model.entity.UserEntity getUser() {
        return user;
    }
    
    /**
     * 设置评论用户信息
     * 
     * @param user 评论用户信息
     */
    public void setUser(cn.xu.domain.user.model.entity.UserEntity user) {
        this.user = user;
    }
    
    /**
     * 获取内容字符串（兼容现有代码）
     */
    public String getContentValue() {
        return content != null ? content.getValue() : null;
    }
    
    /**
     * 设置内容（兼容现有代码）
     */
    public void setContentValue(String contentValue) {
        this.content = contentValue != null ? new CommentContent(contentValue) : null;
        this.updateTime = LocalDateTime.now();
    }
}