package cn.xu.api.controller.web.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublishArticleRequest {
    private Long id;
    private String title;
    private String description;
    private String content;
    private String coverUrl;
    private Long categoryId;
    private List<Long> tagIds; // 标签ID列表
}
