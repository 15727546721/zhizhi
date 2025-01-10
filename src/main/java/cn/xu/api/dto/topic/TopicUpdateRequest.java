package cn.xu.api.dto.topic;

import lombok.Data;

import java.util.List;

@Data
public class TopicUpdateRequest {
    private String content;
    private List<String> images;
    private Long categoryId;
} 