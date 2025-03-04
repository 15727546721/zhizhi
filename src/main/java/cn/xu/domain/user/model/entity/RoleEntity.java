package cn.xu.domain.user.model.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleEntity {
    private Long id;
    private String name;
    private String code;
    private String desc;
} 