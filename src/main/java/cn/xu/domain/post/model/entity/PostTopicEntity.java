package cn.xu.domain.post.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 帖子话题关联实体
 * 封装帖子与话题关联的业务逻辑和规则
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostTopicEntity {
    private Long id;
    private Long postId;
    private Long topicId;
    private LocalDateTime createTime;
}