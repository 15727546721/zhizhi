package cn.xu.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 包含用户信息的随笔持久化对象
 * 用于MyBatis联表查询结果映射
 * 
 * @author xu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EssayWithUserPO {
    /**
     * 随笔ID
     */
    private Long id;

    /**
     * 发布随笔的用户ID
     */
    private Long userId;

    /**
     * 话题内容
     */
    private String content;

    /**
     * 图片URL数组，使用符号','分隔
     */
    private String images;

    /**
     * 绑定的话题，使用符号','分隔
     */
    private String topics;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 评论数
     */
    private Long commentCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 关联的用户信息
     */
    private User user;
}