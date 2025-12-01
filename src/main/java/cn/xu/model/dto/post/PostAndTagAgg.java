package cn.xu.model.dto.post;

import lombok.Data;

/**
 * 帖子与标签关联聚合
 * 用于批量查询帖子及其标签信息
 */
@Data
public class PostAndTagAgg {
    private Long postId;
    private String tags;
}