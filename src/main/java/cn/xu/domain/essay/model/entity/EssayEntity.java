package cn.xu.domain.essay.model.entity;

import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EssayEntity {
    private Long id;
    private Long userId;
    private String content;
    private String images;
    private String topics;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public void validate() {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("发布内容不能为空！");
        }
    }
} 