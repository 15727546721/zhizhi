package cn.xu.api.dto.article;

import cn.xu.domain.article.model.valobj.TagVO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CreateArticleRequest {
    private String title;
    private String content;
    private String coverUrl;
    private Long authorId;
    private Long categoryId;
    private List<TagVO> tags; // 直接使用 TagVO
}
