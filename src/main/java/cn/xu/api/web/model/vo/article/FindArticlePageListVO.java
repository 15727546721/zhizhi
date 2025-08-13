package cn.xu.api.web.model.vo.article;

import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindArticlePageListVO {
    private ArticleEntity article;
    private UserEntity user;
    private String[] tags;
}
