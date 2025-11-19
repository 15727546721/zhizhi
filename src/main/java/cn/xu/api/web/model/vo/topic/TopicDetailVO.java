package cn.xu.api.web.model.vo.topic;

import cn.xu.domain.post.model.entity.TopicEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopicDetailVO {
    private Long id;
    private String name;
    private String description;
    private Integer isRecommended;
    private Integer usageCount;
    private Long followerCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static TopicDetailVO from(TopicEntity topic, Long followerCount) {
        return TopicDetailVO.builder()
                .id(topic.getId())
                .name(topic.getName())
                .description(topic.getDescription())
                .isRecommended(topic.getIsRecommended())
                .usageCount(topic.getUsageCount())
                .followerCount(followerCount)
                .createTime(topic.getCreateTime())
                .updateTime(topic.getUpdateTime())
                .build();
    }
}
