package cn.xu.domain.search.model.valobj;

import cn.xu.domain.post.model.entity.PostEntity;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 搜索结果值对象
 * 封装搜索结果数据
 */
@Data
@Builder
public class SearchResult {
    
    /**
     * 搜索结果列表
     */
    private List<PostEntity> posts;
    
    /**
     * 总记录数
     */
    private long total;
    
    /**
     * 当前页码
     */
    private int page;
    
    /**
     * 每页大小
     */
    private int size;
    
    /**
     * 是否有结果
     */
    public boolean hasResults() {
        return posts != null && !posts.isEmpty();
    }
}

