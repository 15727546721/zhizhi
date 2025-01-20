package cn.xu.api.web.model.dto.article;

import lombok.Data;

@Data
public class CategoryRequest {
    private Long id;
    private String name;
    private String description;
}
