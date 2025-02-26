package cn.xu.api.web.model.dto.article;

import lombok.Data;

@Data
public class DraftRequest {
    private Long id;
    private String title;
    private String content;
}
