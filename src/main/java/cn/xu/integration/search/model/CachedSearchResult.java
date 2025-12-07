package cn.xu.integration.search.model;

import cn.xu.model.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 缓存的搜索结果
 * <p>用于Redis缓存搜索结果</p>
 *
 * @author xu
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CachedSearchResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private List<Post> content;
    private long totalElements;
    private int pageNumber;
    private int pageSize;
    private int totalPages;
}

