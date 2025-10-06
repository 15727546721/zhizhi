package cn.xu.domain.essay.model.entity;

import cn.xu.common.exception.BusinessException;
import cn.xu.domain.essay.model.valobj.EssayContent;
import cn.xu.domain.essay.model.valobj.EssayImages;
import cn.xu.domain.essay.model.valobj.EssayTopics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 随笔领域实体
 * 负责封装随笔的核心业务逻辑和业务规则
 */
@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EssayEntity {
    
    /**
     * 随笔ID
     */
    private Long id;
    
    /**
     * 发布随笔的用户ID
     */
    private Long userId;
    
    /**
     * 随笔内容
     */
    private EssayContent content;
    
    /**
     * 图片列表
     */
    private EssayImages images;
    
    /**
     * 话题列表
     */
    private EssayTopics topics;
    
    /**
     * 点赞数
     */
    private Long likeCount;
    
    /**
     * 评论数
     */
    private Long commentCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 创建新的随笔实体
     * 
     * @param userId 用户ID
     * @param contentStr 内容
     * @param imageList 图片列表
     * @param topicList 话题列表
     * @return 随笔实体
     */
    public static EssayEntity create(Long userId, String contentStr, List<String> imageList, List<String> topicList) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID不能为空");
        }
        
        EssayContent content = EssayContent.of(contentStr);
        EssayImages images = EssayImages.of(imageList);
        EssayTopics topics = EssayTopics.of(topicList);
        
        EssayEntity essay = EssayEntity.builder()
                .userId(userId)
                .content(content)
                .images(images)
                .topics(topics)
                .likeCount(0L)
                .commentCount(0L)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        essay.validate();
        return essay;
    }
    
    /**
     * 从持久化数据恢复实体
     * 
     * @param id 随笔ID
     * @param userId 用户ID
     * @param contentStr 内容
     * @param imagesStr 图片字符串
     * @param topicsStr 话题字符串
     * @param likeCount 点赞数
     * @param commentCount 评论数
     * @param createTime 创建时间
     * @param updateTime 更新时间
     * @return 随笔实体
     */
    public static EssayEntity restore(Long id, Long userId, String contentStr, String imagesStr, 
                                     String topicsStr, Long likeCount, Long commentCount,
                                     LocalDateTime createTime, LocalDateTime updateTime) {
        return EssayEntity.builder()
                .id(id)
                .userId(userId)
                .content(EssayContent.of(contentStr))
                .images(EssayImages.fromString(imagesStr))
                .topics(EssayTopics.fromString(topicsStr))
                .likeCount(likeCount != null ? likeCount : 0L)
                .commentCount(commentCount != null ? commentCount : 0L)
                .createTime(createTime)
                .updateTime(updateTime)
                .build();
    }
    
    /**
     * 验证实体完整性
     * 
     * @throws BusinessException 当验证失败时
     */
    public void validate() {
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID不能为空");
        }
        
        if (content == null) {
            throw new BusinessException("随笔内容不能为空");
        }
        content.validate();
        
        if (images != null) {
            images.validate();
        }
        
        if (topics != null) {
            topics.validate();
        }
    }
    
    /**
     * 更新随笔内容
     * 
     * @param newContentStr 新内容
     * @param newImageList 新图片列表
     * @param newTopicList 新话题列表
     */
    public void update(String newContentStr, List<String> newImageList, List<String> newTopicList) {
        if (newContentStr != null) {
            this.content = EssayContent.of(newContentStr);
        }
        
        if (newImageList != null) {
            this.images = EssayImages.of(newImageList);
        }
        
        if (newTopicList != null) {
            this.topics = EssayTopics.of(newTopicList);
        }
        
        this.updateTime = LocalDateTime.now();
        this.validate();
    }
    
    /**
     * 增加点赞数
     * 
     * @param count 增加的数量
     */
    public void increaseLikeCount(int count) {
        if (count < 0) {
            throw new BusinessException("点赞增加数量不能为负数");
        }
        this.likeCount = (this.likeCount != null ? this.likeCount : 0L) + count;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 减少点赞数
     * 
     * @param count 减少的数量
     */
    public void decreaseLikeCount(int count) {
        if (count < 0) {
            throw new BusinessException("点赞减少数量不能为负数");
        }
        long currentCount = this.likeCount != null ? this.likeCount : 0L;
        this.likeCount = Math.max(0L, currentCount - count);
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 增加评论数
     * 
     * @param count 增加的数量
     */
    public void increaseCommentCount(int count) {
        if (count < 0) {
            throw new BusinessException("评论增加数量不能为负数");
        }
        this.commentCount = (this.commentCount != null ? this.commentCount : 0L) + count;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 减少评论数
     * 
     * @param count 减少的数量
     */
    public void decreaseCommentCount(int count) {
        if (count < 0) {
            throw new BusinessException("评论减少数量不能为负数");
        }
        long currentCount = this.commentCount != null ? this.commentCount : 0L;
        this.commentCount = Math.max(0L, currentCount - count);
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 判断是否是指定用户的随笔
     * 
     * @param userId 用户ID
     * @return 如果是该用户的随笔返回true
     */
    public boolean belongsToUser(Long userId) {
        return Objects.equals(this.userId, userId);
    }
    
    /**
     * 验证用户是否有权限操作该随笔
     * 
     * @param currentUserId 当前用户ID
     * @param isAdmin 是否为管理员
     * @throws BusinessException 当用户无权操作时
     */
    public void validateOwnership(Long currentUserId, boolean isAdmin) {
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 如果是管理员，直接允许操作
        if (isAdmin) {
            return;
        }
        
        // 非管理员只能操作自己的随笔
        if (!belongsToUser(currentUserId)) {
            throw new BusinessException("无权操作此随笔");
        }
    }
    
    /**
     * 验证用户是否有权限操作该随笔（兼容旧方法）
     * 
     * @param currentUserId 当前用户ID
     * @throws BusinessException 当用户无权操作时
     */
    public void validateOwnership(Long currentUserId) {
        validateOwnership(currentUserId, false);
    }
    
    /**
     * 获取内容字符串（用于持久化）
     * 
     * @return 内容字符串
     */
    public String getContentString() {
        return content != null ? content.toString() : "";
    }
    
    /**
     * 获取图片字符串（用于持久化）
     * 
     * @return 图片字符串
     */
    public String getImagesString() {
        return images != null ? images.toString() : "";
    }
    
    /**
     * 获取话题字符串（用于持久化）
     * 
     * @return 话题字符串
     */
    public String getTopicsString() {
        return topics != null ? topics.toString() : "";
    }
    
    /**
     * 判断随笔是否包含图片
     * 
     * @return 如果包含图片返回true
     */
    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }
    
    /**
     * 判断随笔是否包含话题
     * 
     * @return 如果包含话题返回true
     */
    public boolean hasTopics() {
        return topics != null && !topics.isEmpty();
    }
    
    /**
     * 获取热度分数（简单计算：点赞数 * 2 + 评论数 * 3）
     * 
     * @return 热度分数
     */
    public long getHotScore() {
        long likes = likeCount != null ? likeCount : 0L;
        long comments = commentCount != null ? commentCount : 0L;
        return likes * 2 + comments * 3;
    }
} 