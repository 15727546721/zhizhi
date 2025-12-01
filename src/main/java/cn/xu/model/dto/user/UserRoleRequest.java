package cn.xu.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户角色请求参数
 * 
 * 
 */
@Data
@Schema(description = "用户角色请求参数")
public class UserRoleRequest {
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "角色ID列表")
    private List<Long> roleIds;
}