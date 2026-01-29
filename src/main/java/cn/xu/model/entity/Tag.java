package cn.xu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 标签持久化对象
 * tableName: tag
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Tag implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 标签ID
     */
    private Long id;
    
    /**
     * 标签名称
     */
    private String name;
    
    /**
     * 标签描述
     */
    private String description;
    
    /**
     * 是否为推荐标签
     */
    private Integer isRecommended;
    
    /**
     * 使用次数
     */
    private Integer usageCount;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}