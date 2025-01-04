package cn.xu.domain.topic.entity;

import cn.xu.domain.topic.constant.TopicErrorCode;
import cn.xu.exception.AppException;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class Topic {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private List<String> images;
    private Long categoryId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public void validate() {
        if (title == null || title.trim().isEmpty()) {
            throw new AppException(TopicErrorCode.TITLE_EMPTY.getCode(), TopicErrorCode.TITLE_EMPTY.getMessage());
        }
        if (content == null || content.trim().isEmpty()) {
            throw new AppException(TopicErrorCode.CONTENT_EMPTY.getCode(), TopicErrorCode.CONTENT_EMPTY.getMessage());
        }
        if (userId == null) {
            throw new AppException(TopicErrorCode.USER_ID_EMPTY.getCode(), TopicErrorCode.USER_ID_EMPTY.getMessage());
        }
    }
} 