package cn.xu.api.web.model.vo.post;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 帖子列表响应VO
 * 包含帖子信息和用户信息
 * 注意：为了向后兼容，保留post和user字段
 * 但建议使用扁平化的PostListItemVO
 * 
 * @author zhizhi
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "帖子列表响应VO")
public class PostListResponse {
    
    @Schema(description = "帖子信息（扁平化）")
    private PostListItemVO postItem;
    
    @Schema(description = "帖子实体（向后兼容，已废弃）")
    @Deprecated
    private PostEntity post;
    
    @Schema(description = "用户实体（向后兼容，已废弃）")
    @Deprecated
    private UserEntity user;
    
    /**
     * 向后兼容：获取帖子ID
     */
    public Long getId() {
        if (postItem != null) {
            return postItem.getId();
        }
        return post != null ? post.getId() : null;
    }
    
    /**
     * 向后兼容：获取帖子标题
     */
    public String getTitle() {
        if (postItem != null) {
            return postItem.getTitle();
        }
        return post != null && post.getTitle() != null ? post.getTitle().getValue() : null;
    }
}