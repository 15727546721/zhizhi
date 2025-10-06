package cn.xu.domain.post.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 话题领域实体
 * 封装话题相关的业务逻辑和规则
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopicEntity {
    private Long id;
    private String name;
    private String description;
    private Integer isRecommended;
    private Integer usageCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}