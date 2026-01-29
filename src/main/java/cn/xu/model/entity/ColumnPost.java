package cn.xu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 专栏文章关联实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnPost implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 主键ID */
    private Long id;
    
    /** 专栏ID */
    private Long columnId;
    
    /** 帖子ID */
    private Long postId;
    
    /** 排序值(越小越靠前) */
    private Integer sort;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    // ==================== 工厂方法 ====================
    
    /**
     * 创建专栏文章关联
     */
    public static ColumnPost create(Long columnId, Long postId, Integer sort) {
        return ColumnPost.builder()
                .columnId(columnId)
                .postId(postId)
                .sort(sort != null ? sort : 0)
                .createTime(LocalDateTime.now())
                .build();
    }
}
