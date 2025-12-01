package cn.xu.model.dto.post;

import lombok.Data;

import java.util.List;

/**
 * 帖子搜索请求DTO
 * 包含搜索关键词和筛选条件
 */
@Data
public class PostSearchRequest {
    
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 帖子类型筛选（多个类型，如：ARTICLE, POST, DISCUSSION, QUESTION）
     */
    private List<String> types;
    
    /**
     * 发布时间范围筛选
     * all: 全部时间
     * day: 24小时内
     * week: 一周内
     * month: 一个月内
     * year: 一年内
     */
    private String timeRange;
    
    /**
     * 排序方式
     * time: 最新发布（按创建时间倒序）
     * hot: 最热（按热度分数倒序）
     * comment: 评论最多（按评论数倒序）
     * like: 点赞最多（按点赞数倒序）
     */
    private String sortOption;
    
    /**
     * 页码，默认为1
     */
    private Integer pageNo = 1;
    
    /**
     * 页面大小，默认为10
     */
    private Integer pageSize = 10;
}

