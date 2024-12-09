package cn.xu.api.dto.article;

import cn.xu.api.dto.common.PageRequest;
import lombok.Data;

@Data
public class ArticleRequest extends PageRequest {
    private String title;
    private Long categoryId;
    private Long tagId;
    private String status;
}
