package cn.xu.api.system.model.dto.article;

import lombok.Data;

@Data
public class TagRequest {
    private Long id;
    private String name;
    private String description;
}
