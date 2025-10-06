package cn.xu.api.web.model.vo.post;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import lombok.Data;

@Data
public class PostListPageResponse {
    private PostEntity post;
    private UserEntity user;
}