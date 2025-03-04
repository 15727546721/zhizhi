package cn.xu.api.system.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ArticleDetailsVO {
    private Long id;
    private String title;
    private String coverUrl;
    private String description;
    private String content;
    private Long categoryId;
    private List<Long> tagIds;
}
