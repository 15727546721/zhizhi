package cn.xu.api.dto.request.article;

import lombok.Data;

import java.util.List;


@Data
public class ArticleCreateDTO {
    private String id;
    private String title;
    private String content;
    private Long authorId;
    private Long categoryId;
    private String coverUrl;
    private String status;
    private List<Long> tagIds;
}
