package cn.xu.api.web.model.vo.article;

import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.entity.TagEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleDetailVO {
    private ArticleEntity article;
    private UserEntity user;
    private String categoryName;
    private List<TagEntity> tags;
    @JsonProperty("isLiked")
    private boolean isLiked;
    @JsonProperty("isCollected")
    private boolean isCollected;
    @JsonProperty("isAuthor")
    private boolean isAuthor;
    @JsonProperty("isFollowed")
    private boolean isFollowed;
}
