package cn.xu.domain.article.model.aggregate;

import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.entity.TagEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleAndAuthorAggregate {
    private ArticleEntity article;
    private UserEntity author;
    private List<TagEntity> tags;
    private boolean isLiked;
    private boolean isCollected;
    private boolean isAuthor;
}
