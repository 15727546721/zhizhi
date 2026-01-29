package cn.xu.model.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 专栏统计实体
 */
@Data
public class ColumnStatistics {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 专栏ID
     */
    private Long columnId;
    
    /**
     * 统计日期
     */
    private LocalDate statDate;
    
    /**
     * 阅读量
     */
    private Integer viewCount;
    
    /**
     * 订阅数
     */
    private Integer subscribeCount;
    
    /**
     * 文章数
     */
    private Integer postCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
