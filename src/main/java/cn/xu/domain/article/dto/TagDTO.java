package cn.xu.domain.article.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagDTO {
    /**
     * 标签ID
     */
    private Long id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签颜色
     */
    private String color;
} 