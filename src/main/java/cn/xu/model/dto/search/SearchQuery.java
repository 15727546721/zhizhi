package cn.xu.model.dto.search;

import lombok.Builder;
import lombok.Data;

/**
 * 搜索查询实体
 * 封装一次搜索请求的所有信息
 */
@Data
@Builder
public class SearchQuery {
    
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 筛选条件
     */
    private SearchFilter filter;
    
    /**
     * 页码（从1开始）
     */
    private int page;
    
    /**
     * 每页大小
     */
    private int size;
    
    /**
     * 验证搜索查询的有效性
     */
    public void validate() {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }
        
        if (keyword.trim().length() > 100) {
            throw new IllegalArgumentException("搜索关键词长度不能超过100个字符");
        }
        
        if (page < 1) {
            throw new IllegalArgumentException("页码必须大于0");
        }
        
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("每页大小必须在1-100之间");
        }
    }
    
    /**
     * 获取规范化后的关键词
     */
    public String getNormalizedKeyword() {
        return keyword != null ? keyword.trim() : "";
    }
}

