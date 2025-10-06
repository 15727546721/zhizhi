package cn.xu.domain.essay.model.vo;

import cn.xu.domain.user.model.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EssayResponse {
    private Long id;
    private UserEntity user;
    private String content;
    private String[] images;
    private String[] topics;
    private Long likeCount;
    private Long commentCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}