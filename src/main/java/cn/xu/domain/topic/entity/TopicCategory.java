package cn.xu.domain.topic.entity;

import cn.xu.domain.topic.constant.TopicErrorCode;
import cn.xu.exception.AppException;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TopicCategory {
    private Long id;
    private String name;
    private String description;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new AppException(TopicErrorCode.CATEGORY_NAME_EMPTY.getCode(), TopicErrorCode.CATEGORY_NAME_EMPTY.getMessage());
        }
    }
} 