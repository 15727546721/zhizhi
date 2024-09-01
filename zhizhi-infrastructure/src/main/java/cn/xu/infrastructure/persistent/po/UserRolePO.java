package cn.xu.infrastructure.persistent.po;

import lombok.Data;

@Data
public class UserRolePO {
    private Long id;                // 关系唯一标识符
    private Long userId;            // 用户 ID
    private Long roleId;            // 角色 ID
}
