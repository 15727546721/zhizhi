package cn.xu.api.dto.permission;

import cn.xu.api.dto.common.PageRequest;
import lombok.Data;

@Data
public class RoleRequest extends PageRequest {
    private String name;
}
