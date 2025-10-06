package cn.xu.api.web.model.dto.post;

import cn.xu.common.request.PageRequest;
import lombok.Data;

@Data
public class FindPostPageByCategoryRequest extends PageRequest {
    private Long categoryId;
    private String type;
}