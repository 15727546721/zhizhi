package cn.xu.api.dto.permission;

import cn.xu.api.dto.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色查询请求参数")
public class RoleRequest extends PageRequest {

    @Schema(description = "角色名称")
    private String name;
}
