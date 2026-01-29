package cn.xu.model.dto.comment;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 评论数量统计结果
 * <p>用于批量统计目标的评论数</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentCountResult {
    
    /**
     * 目标ID
     */
    private Long targetId;
    
    /**
     * 评论数量
     */
    private Long count;
}
