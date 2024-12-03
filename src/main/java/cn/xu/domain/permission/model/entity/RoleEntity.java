package cn.xu.domain.permission.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RoleEntity {
    private Long id;
    private String name;
    private String code;
    private String desc;
}
