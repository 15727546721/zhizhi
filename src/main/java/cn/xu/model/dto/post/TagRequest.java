package cn.xu.model.dto.post;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagRequest {
    private Long id;
    private String name;
    private String description;
    private Integer sort;
}
