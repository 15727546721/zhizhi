package cn.xu.domain.post.model.aggregate;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.valobj.PostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 帖子聚合根
 * DDD设计中的帖子聚合根，包含帖子实体和相关的标签关联
 * 负责维护帖子与标签之间的一致性边界
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostAggregate {

    /**
     * 聚合根ID（与帖子ID相同）
     */
    private Long id;
    
    /**
     * 帖子实体
     */
    private PostEntity postEntity;
    
    /**
     * 帖子关联的标签ID列表
     */
    private List<Long> tagIds;
    
    /**
     * 帖子关联的话题ID列表
     */
    private List<Long> topicIds;
    
    /**
     * 被采纳的回答ID（仅用于问答帖）
     */
    private Long acceptedAnswerId;
    
    /**
     * 发布帖子聚合根
     */
    public void publish() {
        if (postEntity != null) {
            postEntity.publish();
        }
    }
    
    /**
     * 撤回帖子聚合根
     */
    public void withdraw() {
        if (postEntity != null) {
            postEntity.withdraw();
        }
    }
    
    /**
     * 删除帖子聚合根
     */
    public void delete() {
        if (postEntity != null) {
            postEntity.delete();
        }
    }
    
    /**
     * 更新帖子内容
     */
    public void updateContent(String title, String content, String description) {
        if (postEntity != null) {
            postEntity.updateContent(title, content, description);
        }
    }
    
    /**
     * 设置标签
     */
    public void setTags(List<Long> tagIds) {
        // 验证标签数量
        if (tagIds != null && tagIds.size() > 10) {
            throw new IllegalArgumentException("帖子标签数量不能超过10个");
        }
        this.tagIds = tagIds;
    }
    
    /**
     * 设置话题
     */
    public void setTopics(List<Long> topicIds) {
        // 验证话题数量
        if (topicIds != null && topicIds.size() > 10) {
            throw new IllegalArgumentException("帖子话题数量不能超过10个");
        }
        this.topicIds = topicIds;
    }
    
    /**
     * 获取聚合根的用户ID
     */
    public Long getUserId() {
        return postEntity != null ? postEntity.getUserId() : null;
    }
    
    /**
     * 验证用户权限
     */
    public void validateOwnership(Long currentUserId) {
        if (postEntity != null) {
            postEntity.validateOwnership(currentUserId);
        }
    }
    
    /**
     * 判断是否已发布
     */
    public boolean isPublished() {
        return postEntity != null && postEntity.isPublished();
    }
    
    /**
     * 判断是否为问答帖
     */
    public boolean isQuestion() {
        return postEntity != null && postEntity.isQuestion();
    }
    
    /**
     * 判断是否为讨论帖
     */
    public boolean isDiscussion() {
        return postEntity != null && postEntity.isDiscussion();
    }
    
    /**
     * 判断是否为文章
     */
    public boolean isArticle() {
        return postEntity != null && postEntity.isArticle();
    }
    
    /**
     * 判断是否加精
     */
    public boolean isFeatured() {
        return postEntity != null && postEntity.isFeatured();
    }
    
    /**
     * 设置是否加精
     */
    public void setFeatured(boolean isFeatured) {
        if (postEntity != null) {
            postEntity.setFeatured(isFeatured);
        }
    }
    
    /**
     * 验证帖子创建前的完整性
     */
    public void validateForCreation() {
        if (postEntity == null) {
            throw new IllegalStateException("帖子实体不能为空");
        }
        
        // 验证帖子实体的完整性
        postEntity.validateForCreation();
        
        // 可以添加其他聚合级别的验证
        if (tagIds != null && tagIds.size() > 10) {
            throw new IllegalArgumentException("帖子标签数量不能超过10个");
        }
        
        if (topicIds != null && topicIds.size() > 10) {
            throw new IllegalArgumentException("帖子话题数量不能超过10个");
        }
        
        // 对于问答帖，验证类型设置
        if (postEntity.getType() != null && PostType.QUESTION.equals(postEntity.getType()) && acceptedAnswerId != null) {
            throw new IllegalArgumentException("新创建的问答帖不能有已采纳的回答");
        }
        
        // 验证帖子类型与聚合根的一致性
        if (postEntity.getType() == null) {
            throw new IllegalArgumentException("帖子类型不能为空");
        }
    }
    
    /**
     * 增加帖子浏览数
     */
    public void incrementViewCount() {
        if (postEntity != null) {
            Long currentViewCount = postEntity.getViewCount();
            postEntity.setViewCount(currentViewCount != null ? currentViewCount + 1 : 1L);
        }
    }
    
    /**
     * 更新帖子内容
     */
    public void updatePost(PostEntity updatedPost) {
        if (postEntity != null && updatedPost != null) {
            // 更新帖子实体的内容
            postEntity.updateContent(
                updatedPost.getTitleValue(),
                updatedPost.getContentValue(), 
                updatedPost.getDescription()
            );
            // 可以根据需要更新其他字段
            if (updatedPost.getCoverUrl() != null) {
                postEntity.setCoverUrl(updatedPost.getCoverUrl());
            }
            if (updatedPost.getCategoryId() != null) {
                postEntity.setCategoryId(updatedPost.getCategoryId());
            }
            if (updatedPost.getType() != null) {
                postEntity.setType(updatedPost.getType());
            }
            if (updatedPost.isFeatured() != postEntity.isFeatured()) {
                postEntity.setFeatured(updatedPost.isFeatured());
            }
        }
    }
    
    /**
     * 设置已采纳的回答
     */
    public void setAcceptedAnswer(Long answerId) {
        if (!isQuestion()) {
            throw new IllegalStateException("只有问答帖才能设置已采纳的回答");
        }
        // 验证是否可以设置已采纳的回答
        if (!isPublished()) {
            throw new IllegalStateException("只有已发布的问答帖才能采纳回答");
        }
        
        // 验证回答ID是否有效
        if (answerId == null) {
            throw new IllegalArgumentException("回答ID不能为空");
        }
        
        this.acceptedAnswerId = answerId;
    }
    
    /**
     * 判断是否可以设置已采纳的回答
     */
    public boolean canSetAcceptedAnswer() {
        return isQuestion() && isPublished();
    }
    
    /**
     * 验证是否可以采纳指定的回答
     */
    public void validateCanAcceptAnswer(PostAggregate answerAggregate) {
        if (!isQuestion()) {
            throw new IllegalStateException("只有问答帖才能采纳回答");
        }
        
        if (!isPublished()) {
            throw new IllegalStateException("只有已发布的问答帖才能采纳回答");
        }
        
        if (answerAggregate == null) {
            throw new IllegalArgumentException("回答不能为空");
        }
        
        if (!answerAggregate.isPublished()) {
            throw new IllegalArgumentException("只能采纳已发布的回答");
        }
        
        // 验证回答是否属于当前问题
        if (!isAnswerBelongsToQuestion(answerAggregate)) {
            throw new IllegalArgumentException("回答不属于当前问题");
        }
    }
    
    /**
     * 验证回答是否属于当前问题
     * @param answerAggregate 回答聚合根
     * @return 是否属于当前问题
     */
    private boolean isAnswerBelongsToQuestion(PostAggregate answerAggregate) {
        // 检查回答聚合根是否为空
        if (answerAggregate == null) {
            return false;
        }
        
        // 检查回答的帖子实体是否为空
        if (answerAggregate.getPostEntity() == null) {
            return false;
        }
        
        // 对于问答系统，回答应该指向问题的ID
        PostEntity answerPost = answerAggregate.getPostEntity();
        
        // 检查回答的acceptedAnswerId是否等于当前问题的ID
        return answerPost.getAcceptedAnswerId() != null && 
               answerPost.getAcceptedAnswerId().equals(this.getId());
    }
    
    /**
     * 增加点赞数
     */
    public void increaseLikeCount() {
        if (postEntity != null) {
            postEntity.increaseLikeCount();
        }
    }
    
    /**
     * 减少点赞数
     */
    public void decreaseLikeCount() {
        if (postEntity != null) {
            postEntity.decreaseLikeCount();
        }
    }
    
    /**
     * 增加收藏数
     */
    public void increaseFavoriteCount() {
        if (postEntity != null) {
            postEntity.increaseFavoriteCount();
        }
    }
    
    /**
     * 减少收藏数
     */
    public void decreaseFavoriteCount() {
        if (postEntity != null) {
            postEntity.decreaseFavoriteCount();
        }
    }
    
    /**
     * 增加评论数
     */
    public void increaseCommentCount() {
        if (postEntity != null) {
            postEntity.increaseCommentCount();
        }
    }
    
    /**
     * 减少评论数
     */
    public void decreaseCommentCount() {
        if (postEntity != null) {
            postEntity.decreaseCommentCount();
        }
    }
    
    /**
     * 获取帖子ID
     */
    public Long getId() {
        return id;
    }
    
    /**
     * 设置帖子ID
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * 获取帖子实体
     */
    public PostEntity getPostEntity() {
        return postEntity;
    }
    
    /**
     * 设置帖子实体
     */
    public void setPostEntity(PostEntity postEntity) {
        this.postEntity = postEntity;
    }
    
    /**
     * 获取标签ID列表
     */
    public List<Long> getTagIds() {
        return tagIds;
    }
    
    /**
     * 设置标签ID列表
     */
    public void setTagIds(List<Long> tagIds) {
        this.tagIds = tagIds;
    }
    
    /**
     * 获取话题ID列表
     */
    public List<Long> getTopicIds() {
        return topicIds;
    }
    
    /**
     * 设置话题ID列表
     */
    public void setTopicIds(List<Long> topicIds) {
        this.topicIds = topicIds;
    }
    
    /**
     * 获取已采纳的回答ID
     */
    public Long getAcceptedAnswerId() {
        return acceptedAnswerId;
    }
    
    /**
     * 设置已采纳的回答ID
     */
    public void setAcceptedAnswerId(Long acceptedAnswerId) {
        this.acceptedAnswerId = acceptedAnswerId;
    }
}