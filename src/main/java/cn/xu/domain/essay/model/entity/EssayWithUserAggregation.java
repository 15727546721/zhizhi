package cn.xu.domain.essay.model.entity;

import cn.xu.domain.user.model.entity.UserEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EssayWithUserAggregation {
    private Long id;
    private UserEntity user;
    private String content;
    private String images;
    private String topics;
    private Long likeCount;
    private Long commentCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
