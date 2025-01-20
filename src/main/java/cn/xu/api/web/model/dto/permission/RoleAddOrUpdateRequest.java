package cn.xu.api.web.model.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "角色添加或更新请求")
public class RoleAddOrUpdateRequest {

    @Schema(description = "角色ID")
    private Long id;

    @Schema(description = "角色编码")
    private String code;

    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "备注")
    private String remark;
}
