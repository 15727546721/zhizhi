package cn.xu.domain.comment.model.entity;

import cn.xu.application.common.ResponseCode;
import cn.xu.domain.comment.model.valueobject.CommentContent;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.infrastructure.common.exception.BusinessException;
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
    private UserEntity user; // 评论用户信息
    private UserEntity replyUser; // 被回复用户信息
    private List<CommentEntity> children = new ArrayList<>(); // 子评论

    private double hotScore; // 热度分数
    private boolean isHot; // 是否热门评论

    /**
     * 评论类型枚举
     */
    public enum CommentType {
        ARTICLE(1, "文章评论"),
        ESSAY(2, "话题评论");

        private final int code;
        private final String desc;

        CommentType(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static CommentType fromCode(int code) {
            for (CommentType type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
            throw new IllegalArgumentException("不支持的评论类型: " + code);
        }
    }

    // ==================== 业务方法 ====================

    /**
     * 创建根评论
     */
    public static CommentEntity createRootComment(Integer targetType, Long targetId, Long userId, String content, List<String> imageUrls) {
        return CommentEntity.builder()
                .targetType(targetType)
                .targetId(targetId)
                .parentId(null)
                .userId(userId)
                .replyUserId(null)
                .content(new CommentContent(content))
                .imageUrls(imageUrls != null ? imageUrls : new ArrayList<>())
                .likeCount(0L)
                .replyCount(0L)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .hotScore(0.0)
                .isHot(false)
                .build();
    }

    /**
     * 创建回复评论
     */
    public static CommentEntity createReplyComment(Integer targetType, Long targetId, Long parentId, Long userId, Long replyUserId, String content, List<String> imageUrls) {
        return CommentEntity.builder()
                .targetType(targetType)
                .targetId(targetId)
                .parentId(parentId)
                .userId(userId)
                .replyUserId(replyUserId)
                .content(new CommentContent(content))
                .imageUrls(imageUrls != null ? imageUrls : new ArrayList<>())
                .likeCount(0L)
                .replyCount(0L)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .hotScore(0.0)
                .isHot(false)
                .build();
    }

    /**
     * 增加点赞数
     */
    public void increaseLikeCount() {
        this.likeCount = (this.likeCount == null ? 0 : this.likeCount) + 1;
        updateHotScore();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 减少点赞数
     */
    public void decreaseLikeCount() {
        this.likeCount = Math.max(0, (this.likeCount == null ? 0 : this.likeCount) - 1);
        updateHotScore();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 增加回复数
     */
    public void increaseReplyCount() {
        this.replyCount = (this.replyCount == null ? 0 : this.replyCount) + 1;
        updateHotScore();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 减少回复数
     */
    public void decreaseReplyCount() {
        this.replyCount = Math.max(0, (this.replyCount == null ? 0 : this.replyCount) - 1);
        updateHotScore();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 更新评论内容
     */
    public void updateContent(String newContent) {
        this.content = new CommentContent(newContent);
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 验证用户是否有权限操作该评论
     */
    public void validateOwnership(Long currentUserId) {
        if (currentUserId == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户未登录");
        }
        
        if (!this.userId.equals(currentUserId)) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "无权操作此评论");
        }
    }

    /**
     * 判断是否为根评论
     */
    public boolean isRootComment() {
        return this.parentId == null;
    }

    /**
     * 判断是否为回复评论
     */
    public boolean isReplyComment() {
        return this.parentId != null;
    }

    /**
     * 判断是否为热门评论
     */
    public boolean isPopular() {
        // 简单的热门评论判断逻辑：点赞数超过10或回复数超过5
        long likes = this.likeCount != null ? this.likeCount : 0;
        long replies = this.replyCount != null ? this.replyCount : 0;
        return likes >= 10 || replies >= 5;
    }

    /**
     * 计算热度分数
     */
    public void updateHotScore() {
        long likes = this.likeCount != null ? this.likeCount : 0;
        long replies = this.replyCount != null ? this.replyCount : 0;
        
        // 时间衰减因子
        double timeDecay = calculateTimeDecay();
        
        // 热度计算公式：点赞数*2 + 回复数*3，乘以时间衰减因子
        this.hotScore = (likes * 2 + replies * 3) * timeDecay;
        this.isHot = this.hotScore >= 10; // 热度分数超过10认为是热门评论
    }

    /**
     * 计算时间衰减因子
     */
    private double calculateTimeDecay() {
        if (createTime == null) return 1.0;
        
        long hoursSinceCreation = java.time.Duration.between(createTime, LocalDateTime.now()).toHours();
        // 每12小时热度衰减20%
        return Math.pow(0.8, hoursSinceCreation / 12.0);
    }

    /**
     * 添加子评论
     */
    public void addChild(CommentEntity childComment) {
        if (childComment == null) {
            return;
        }
        
        if (!this.id.equals(childComment.getParentId())) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "子评论的父ID与当前评论不匹配");
        }
        
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        
        this.children.add(childComment);
    }

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
        CommentType.fromCode(targetType);
    }

    // ==================== 兼容性方法 ====================
    
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