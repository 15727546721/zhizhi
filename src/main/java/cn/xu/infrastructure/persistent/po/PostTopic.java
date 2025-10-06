package cn.xu.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子话题关联表
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PostTopic implements Serializable {
    /**
     * ID
     */
    private Long id;

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 话题ID
     */
    private Long topicId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    private static final long serialVersionUID = 1L;
}