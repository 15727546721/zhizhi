package cn.xu.api.web.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户权限请求参数
 * 
 * @author Lily
 */
@Data
@Schema(description = "用户权限请求参数")
public class UserPermissionRequest {
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "权限ID列表")
    private List<Long> permissionIds;
}