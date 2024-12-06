package cn.xu.api.dto.article;

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
    private String status;
    private Boolean commentEnabled;
    private Long categoryId;
    private List<Long> tagIds;
}
