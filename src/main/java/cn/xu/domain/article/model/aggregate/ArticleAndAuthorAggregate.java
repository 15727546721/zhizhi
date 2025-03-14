package cn.xu.domain.article.model.aggregate;

import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class ArticleAndAuthorAggregate {
    private ArticleEntity article;
    private UserEntity user;
}
