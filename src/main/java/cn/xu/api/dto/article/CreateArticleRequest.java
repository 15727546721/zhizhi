package cn.xu.api.dto.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
