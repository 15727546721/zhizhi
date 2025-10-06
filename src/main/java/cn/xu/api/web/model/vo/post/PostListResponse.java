package cn.xu.api.web.model.vo.post;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostListResponse {
    PostEntity post;
    UserEntity user;
}