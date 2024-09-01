package cn.xu.infrastructure.persistent.po;

import lombok.Data;

@Data
public class RoleMenuPO {
    private Long id;                // 关系唯一标识符
    private Long roleId;            // 角色 ID
    private Long menuId;            // 菜单 ID
}
