package cn.xu.api.web.model.dto.permission;

import lombok.Data;

import java.util.List;

@Data
public class RoleMenuRequest {
    private Long roleId;

    private List<Long> menuIds;
}
