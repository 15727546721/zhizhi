package cn.xu.infrastructure.search.model;

import cn.xu.domain.post.model.entity.PostEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 缓存的搜索结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CachedSearchResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private List<PostEntity> content;
    private long totalElements;
    private int pageNumber;
    private int pageSize;
    private int totalPages;
}

