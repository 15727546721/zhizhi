package cn.xu.api.web.model.dto.topic;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TopicResponse {
    private Long id;
    private Long userId;
    private String content;
    private List<String> images;
    private Long categoryId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 