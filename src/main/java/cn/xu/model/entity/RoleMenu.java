package cn.xu.model.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoleMenu implements Serializable {
    private Long id;                // 关系唯一标识符
    private Long roleId;            // 角色 ID
    private Long menuId;            // 菜单 ID
}
