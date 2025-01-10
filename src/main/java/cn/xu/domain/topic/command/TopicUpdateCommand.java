package cn.xu.domain.topic.command;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TopicUpdateCommand {
    private Long id;
    private String content;
    private List<String> images;
    private Long categoryId;

    public void validate() {
        if (id == null) {
            throw new IllegalArgumentException("话题ID不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("话题内容不能为空");
        }
    }
} 