package cn.xu.api.web.model.vo.post;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.entity.TopicEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "帖子分页列表响应数据")
public class PostPageListResponse {
    @Schema(description = "帖子信息")
    private PostEntity post;
    
    @Schema(description = "用户信息")
    private UserEntity user;
    
    @Schema(description = "标签列表")
    private String[] tags;
    
    @Schema(description = "话题列表")
    private List<TopicEntity> topics;
}