package cn.xu.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @TableName tag
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Tag implements Serializable {
    /**
     * 主键id
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
     * 标签的创建时间
     */
    private LocalDateTime createTime;

    /**
     * 标签的最后更新时间
     */
    private LocalDateTime updateTime;
}