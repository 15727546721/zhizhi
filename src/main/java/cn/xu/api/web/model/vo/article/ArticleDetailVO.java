package cn.xu.api.web.model.vo.article;

import cn.xu.domain.article.model.aggregate.ArticleAndAuthorAggregate;
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
    ArticleAndAuthorAggregate articleAndAuthorAggregate;
    private String categoryName;
    private List<String> tags;
    private boolean isLiked;
}
