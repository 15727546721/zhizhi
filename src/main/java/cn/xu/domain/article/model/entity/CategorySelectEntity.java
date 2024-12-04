package cn.xu.domain.article.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下拉分类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategorySelectEntity {
    private Long id;
    private String name;
}
