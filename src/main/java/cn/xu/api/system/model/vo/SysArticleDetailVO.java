package cn.xu.api.system.model.vo;

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
public class SysArticleDetailVO {
    ArticleAndAuthorAggregate articleAndAuthorAggregate;
    private String categoryName;
    private List<String> tags;
}
