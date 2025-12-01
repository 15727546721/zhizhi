package cn.xu.model.vo.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 帖子列表VO
 * 包含帖子完整信息
 * 
 * @author zhizhi
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "帖子列表VO")
public class PostListVO {
    
    @Schema(description = "帖子信息")
    private PostItemVO postItem;
    
    public Long getId() {
        return postItem != null ? postItem.getId() : null;
    }
    
    public String getTitle() {
        return postItem != null ? postItem.getTitle() : null;
    }
}