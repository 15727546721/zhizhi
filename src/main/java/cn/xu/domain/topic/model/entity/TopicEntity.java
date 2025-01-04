package cn.xu.domain.topic.model.entity;

import cn.xu.exception.AppException;
import cn.xu.infrastructure.common.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicEntity {
    
    /**
     * 话题ID
     */
    private Long id;
    
    /**
     * 发布话题的用户ID
     */
    private Long userId;
    
    /**
     * 话题内容
     */
    private String content;
    
    /**
     * 话题图片URL列表
     */
    private List<String> images;
    
    /**
     * 话题分类ID（可选）
     */
    private Long categoryId;
    
    /**
     * 话题标签列表（#标签#）
     */
    private List<String> tags;
    
    /**
     * 浏览数
     */
    private Integer viewCount;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 评论数
     */
    private Integer commentCount;
    
    /**
     * 收藏数
     */
    private Integer collectCount;
    
    /**
     * 状态（0：草稿，1：已发布，2：已下架）
     */
    private Integer status;
    
    /**
     * 是否置顶
     */
    private Boolean isTop;
    
    /**
     * 是否精华
     */
    private Boolean isEssence;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 验证话题数据
     */
    public void validate() {
        if (!StringUtils.hasText(content)) {
            throw new AppException(ResponseCode.NULL_PARAMETER.getCode(), "话题内容不能为空");
        }
        
        if (content.length() > 2000) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "话题内容不能超过2000个字符");
        }
        
        if (userId == null) {
            throw new AppException(ResponseCode.NULL_PARAMETER.getCode(), "用户ID不能为空");
        }
        
        // 验证图片
        if (!CollectionUtils.isEmpty(images)) {
            if (images.size() > 9) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "图片数量不能超过9张");
            }
            for (String image : images) {
                if (!StringUtils.hasText(image)) {
                    throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "图片URL不能为空");
                }
                if (image.length() > 1000) {
                    throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "图片URL长度不能超过1000个字符");
                }
            }
        }
        
        // 验证标签
        if (!CollectionUtils.isEmpty(tags)) {
            if (tags.size() > 5) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "标签数量不能超过5个");
            }
            for (String tag : tags) {
                if (!StringUtils.hasText(tag)) {
                    throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "标签不能为空");
                }
                if (tag.length() > 20) {
                    throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "标签长度不能超过20个字符");
                }
            }
        }
        
        // 初始化状态
        if (status == null) {
            status = 0; // 默认为草稿状态
        }
        
        if (isTop == null) {
            isTop = false;
        }
        
        if (isEssence == null) {
            isEssence = false;
        }
        
        // 初始化计数器
        if (viewCount == null) viewCount = 0;
        if (likeCount == null) likeCount = 0;
        if (commentCount == null) commentCount = 0;
        if (collectCount == null) collectCount = 0;
    }
    
    /**
     * 更新话题内容
     */
    public void update(TopicEntity newTopic) {
        if (StringUtils.hasText(newTopic.getContent())) {
            this.content = newTopic.getContent();
        }
        if (!CollectionUtils.isEmpty(newTopic.getImages())) {
            this.images = newTopic.getImages();
        }
        if (newTopic.getCategoryId() != null) {
            this.categoryId = newTopic.getCategoryId();
        }
        if (!CollectionUtils.isEmpty(newTopic.getTags())) {
            this.tags = newTopic.getTags();
        }
        if (newTopic.getStatus() != null) {
            this.status = newTopic.getStatus();
        }
        if (newTopic.getIsTop() != null) {
            this.isTop = newTopic.getIsTop();
        }
        if (newTopic.getIsEssence() != null) {
            this.isEssence = newTopic.getIsEssence();
        }
    }
    
    /**
     * 从内容中提取标签
     * 格式：#标签内容#
     */
    public void extractTagsFromContent() {
        if (!StringUtils.hasText(content)) {
            return;
        }
        
        // 使用正则表达式提取#标签#
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("#([^#]+)#");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        
        List<String> extractedTags = new java.util.ArrayList<>();
        while (matcher.find()) {
            String tag = matcher.group(1).trim();
            if (StringUtils.hasText(tag)) {
                extractedTags.add(tag);
            }
        }
        
        if (!extractedTags.isEmpty()) {
            this.tags = extractedTags;
        }
    }
    
    /**
     * 增加浏览数
     */
    public void incrementViewCount() {
        this.viewCount = this.viewCount == null ? 1 : this.viewCount + 1;
    }
    
    /**
     * 增加点赞数
     */
    public void incrementLikeCount() {
        this.likeCount = this.likeCount == null ? 1 : this.likeCount + 1;
    }
    
    /**
     * 减少点赞数
     */
    public void decrementLikeCount() {
        if (this.likeCount != null && this.likeCount > 0) {
            this.likeCount--;
        }
    }
    
    /**
     * 增加评论数
     */
    public void incrementCommentCount() {
        this.commentCount = this.commentCount == null ? 1 : this.commentCount + 1;
    }
    
    /**
     * 减少评论数
     */
    public void decrementCommentCount() {
        if (this.commentCount != null && this.commentCount > 0) {
            this.commentCount--;
        }
    }
    
    /**
     * 增加收藏数
     */
    public void incrementCollectCount() {
        this.collectCount = this.collectCount == null ? 1 : this.collectCount + 1;
    }
    
    /**
     * 减少收藏数
     */
    public void decrementCollectCount() {
        if (this.collectCount != null && this.collectCount > 0) {
            this.collectCount--;
        }
    }
} 