package cn.xu.domain.post.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 标签领域实体
 * 封装标签相关的业务逻辑和规则
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagEntity {
    private Long id;
    private String name;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}