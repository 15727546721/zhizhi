package cn.xu.api.dto.article;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CreateArticleRequest {
    private Long id;
    private String title;
    private String description;
    private String content;
    private String coverUrl;
    private String status;
    private String commentEnabled;
    private String isTop;
    private Long categoryId;
    private List<Long> tagIds; // 标签ID列表
}
