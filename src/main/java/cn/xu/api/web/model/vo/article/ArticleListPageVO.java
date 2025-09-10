package cn.xu.api.web.model.vo.article;

import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import lombok.Data;

@Data
public class ArticleListPageVO {
    private ArticleEntity article;
    private UserEntity user;
}
