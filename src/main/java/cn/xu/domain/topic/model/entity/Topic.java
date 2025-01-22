package cn.xu.domain.topic.model.entity;

import cn.xu.domain.topic.constant.TopicErrorCode;
import cn.xu.infrastructure.common.exception.BusinessException;
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
            throw new BusinessException(TopicErrorCode.TITLE_EMPTY.getCode(), TopicErrorCode.TITLE_EMPTY.getMessage());
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(TopicErrorCode.CONTENT_EMPTY.getCode(), TopicErrorCode.CONTENT_EMPTY.getMessage());
        }
        if (userId == null) {
            throw new BusinessException(TopicErrorCode.USER_ID_EMPTY.getCode(), TopicErrorCode.USER_ID_EMPTY.getMessage());
        }
    }
} 