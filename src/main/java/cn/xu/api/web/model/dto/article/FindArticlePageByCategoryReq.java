package cn.xu.api.web.model.dto.article;

import cn.xu.infrastructure.common.request.PageRequest;
import lombok.Data;

@Data
public class FindArticlePageByCategoryReq extends PageRequest {
    private Long categoryId;
    private String type;
}
