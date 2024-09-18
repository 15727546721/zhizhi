package cn.xu.domain.article.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class TagEntity {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
