package cn.xu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户等级持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLevel {
    private Long id;
    private Long userId;
    private Integer level; // 等级
    private String levelName; // 等级名称
    private Long currentExp; // 当前经验值
    private Long nextLevelExp; // 下一级所需经验值
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}