package cn.xu.domain.post.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 分类领域实体
 * 封装分类相关的业务逻辑和规则
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryEntity {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}