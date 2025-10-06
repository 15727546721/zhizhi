package cn.xu.api.web.model.vo.post;

import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.entity.TagEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "帖子详情响应数据")
public class PostDetailResponse {
    @Schema(description = "帖子信息")
    private PostEntity post;
    
    @Schema(description = "用户信息")
    private UserEntity user;
    
    @Schema(description = "分类名称")
    private String categoryName;
    
    @Schema(description = "标签列表")
    private List<TagEntity> tags;
    
    @Schema(description = "话题ID列表")
    private List<Long> topicIds;
    
    @Schema(description = "评论列表")
    private List<CommentEntity> comments;
    
    @JsonProperty("isLiked")
    @Schema(description = "是否已点赞")
    private boolean isLiked;
    
    @JsonProperty("isCollected")
    @Schema(description = "是否已收藏")
    private boolean isCollected;
    
    @JsonProperty("isAuthor")
    @Schema(description = "是否为作者")
    private boolean isAuthor;
    
    @JsonProperty("isFollowed")
    @Schema(description = "是否已关注")
    private boolean isFollowed;
}