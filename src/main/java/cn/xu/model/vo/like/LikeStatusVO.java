package cn.xu.model.vo.like;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 点赞状态VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeStatusVO {

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 点赞类型
     */
    private String type;

    /**
     * 点赞数
     */
    private Long count;

    /**
     * 是否已点赞
     */
    private Boolean liked;
}
