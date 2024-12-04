package cn.xu.domain.article.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下单标签
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagSelectEntity {
    private Long id;
    private String name;
}
