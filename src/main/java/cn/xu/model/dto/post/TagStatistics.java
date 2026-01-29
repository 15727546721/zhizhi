package cn.xu.model.dto.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签统计信息
 * 用于表示某个标签的统计数据，包括标签ID、标签名称以及使用次数。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagStatistics {

    /**
     * 标签ID
     */
    private Long tagId;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 使用次数（关联的帖子数量）
     */
    private Integer usageCount;
}
