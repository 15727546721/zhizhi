package cn.xu.domain.essay.model.vo;

import cn.xu.domain.user.model.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EssayVO {
    private Long id;
    private UserEntity user;
    private String content;
    private String[] images;
    private String[] topics;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
