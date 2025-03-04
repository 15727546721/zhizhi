package cn.xu.infrastructure.persistent.po;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Builder
@Data
public class UserRole implements Serializable {
    private Long id;                // 关系唯一标识符
    private Long userId;            // 用户 ID
    private Long roleId;            // 角色 ID
}
