package cn.xu.api.system.controller;

import cn.xu.api.web.model.dto.user.UserPermissionRequest;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.exception.BusinessException;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.user.service.IUserPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户权限管理控制器
 * 
 * @author Lily
 */
@Tag(name = "用户权限管理", description = "用户权限管理相关接口")
@RestController
@RequestMapping("/system/user-permission")
public class SysUserPermissionController {

    @Resource
    private IUserPermissionService userPermissionService;

    @PostMapping("/assign")
    @Operation(summary = "为用户直接分配权限")
    @ApiOperationLog(description = "为用户直接分配权限")
    public ResponseEntity assignPermissionsToUser(@RequestBody UserPermissionRequest userPermissionRequest) {
        // 参数校验
        if (userPermissionRequest.getUserId() == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "用户ID不能为空");
        }
        
        userPermissionService.assignPermissionsToUser(userPermissionRequest.getUserId(), userPermissionRequest.getPermissionIds());
        return ResponseEntity.builder()
                .info("分配用户权限成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @GetMapping("/user-permissions")
    @Operation(summary = "获取用户的权限ID列表")
    @ApiOperationLog(description = "获取用户的权限ID列表")
    public ResponseEntity<List<Long>> getUserPermissionIds(@Parameter(description = "用户ID") @RequestParam("userId") Long userId) {
        if (userId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "用户ID不能为空");
        }
        
        List<Long> permissionIds = userPermissionService.getUserPermissionIds(userId);
        return ResponseEntity.<List<Long>>builder()
                .data(permissionIds)
                .info("获取用户权限ID列表成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }
    
    @DeleteMapping("/remove")
    @Operation(summary = "移除用户的权限")
    @ApiOperationLog(description = "移除用户的权限")
    public ResponseEntity removeUserPermissions(@RequestBody UserPermissionRequest userPermissionRequest) {
        // 参数校验
        if (userPermissionRequest.getUserId() == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "用户ID不能为空");
        }
        
        userPermissionService.removeUserPermissions(userPermissionRequest.getUserId(), userPermissionRequest.getPermissionIds());
        return ResponseEntity.builder()
                .info("移除用户权限成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }
    
    @DeleteMapping("/clear")
    @Operation(summary = "清除用户的所有权限")
    @ApiOperationLog(description = "清除用户的所有权限")
    public ResponseEntity clearUserPermissions(@Parameter(description = "用户ID") @RequestParam("userId") Long userId) {
        if (userId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "用户ID不能为空");
        }
        
        userPermissionService.clearUserPermissions(userId);
        return ResponseEntity.builder()
                .info("清除用户权限成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }
}