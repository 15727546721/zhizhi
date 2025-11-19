package cn.xu.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 话题关注
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicFollow {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 话题ID
     */
    private Long topicId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
