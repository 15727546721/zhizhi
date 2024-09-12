package cn.xu.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Category {
    /**
     * 分类的唯一标识符
     */
    private Long id;

    /**
     * 分类的名称
     */
    private String name;

    /**
     * 分类的描述
     */
    private String description;

    /**
     * 分类的创建时间
     */
    private LocalDateTime createTime;

    /**
     * 分类的最后更新时间
     */
    private LocalDateTime updateTime;
}
