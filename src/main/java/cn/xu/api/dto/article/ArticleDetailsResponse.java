package cn.xu.api.dto.article;

import cn.xu.domain.article.model.entity.CategoryEntity;
import cn.xu.domain.article.model.entity.TagEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ArticleDetailsResponse {
    private Long id;
    private String title;
    private String coverUrl;
    private String description;
    private String content;
    private String isTop;
    private String status;
    private String commentEnabled;
    private CategoryEntity category;
    private List<TagEntity> tags;
}
