package cn.xu.api.web.model.dto.topic;

import lombok.Data;

import java.util.List;

@Data
public class TopicUpdateRequest {
    private String content;
    private List<String> images;
    private Long categoryId;
} 