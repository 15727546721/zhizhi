package cn.xu.api.web.model.vo.post;

import cn.xu.api.web.model.vo.user.UserResponse;
import cn.xu.domain.post.model.entity.TagEntity;
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
    @Schema(description = "帖子ID")
    private Long id;

    @Schema(description = "帖子类型")
    private String type;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "用户信息")
    private UserResponse author;

    @Schema(description = "封面图片")
    private String coverUrl;

    @Schema(description = "分类名称")
    private String categoryName;
    
    @Schema(description = "标签列表")
    private List<TagEntity> tags;
    
    @Schema(description = "话题ID列表")
    private List<Long> topicIds;

    @Schema(description = "被采纳的回答ID（仅用于问答帖）")
    private Long acceptedAnswerId;

    @Schema(description = "浏览量")
    private Long viewCount;

    @Schema(description = "点赞数")
    private Long likeCount;

    @Schema(description = "评论数")
    private Long commentCount;

    @Schema(description = "收藏数")
    private Long collectCount;

    @Schema(description = "分享数")
    private Long shareCount;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "是否加精")
    private Boolean isFeatured;

    @Schema(description = "是否已点赞")
    private boolean isLiked;
    
    @Schema(description = "是否已收藏")
    private boolean isCollected;
    
    @Schema(description = "是否为作者")
    private boolean isAuthor;
    
    @Schema(description = "是否已关注")
    private boolean isFollowed;
}