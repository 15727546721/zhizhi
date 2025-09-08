package cn.xu.api.system.controller;

import cn.xu.api.web.model.dto.user.UserRoleRequest;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.user.service.IUserRoleService;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.common.response.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户角色管理控制器
 * 
 * @author Lily
 */
@Tag(name = "用户角色管理", description = "用户角色管理相关接口")
@RestController
@RequestMapping("/system/user-role")
public class SysUserRoleController {

    @Resource
    private IUserRoleService userRoleService;

    @PostMapping("/assign")
    @Operation(summary = "为用户分配角色")
    public ResponseEntity assignRolesToUser(@RequestBody UserRoleRequest userRoleRequest) {
        // 参数校验
        if (userRoleRequest.getUserId() == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "用户ID不能为空");
        }
        
        userRoleService.assignRolesToUser(userRoleRequest.getUserId(), userRoleRequest.getRoleIds());
        return ResponseEntity.builder()
                .info("分配用户角色成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @GetMapping("/user-roles")
    @Operation(summary = "获取用户的角色ID列表")
    public ResponseEntity<List<Long>> getUserRoleIds(@RequestParam("userId") Long userId) {
        if (userId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "用户ID不能为空");
        }
        
        List<Long> roleIds = userRoleService.getUserRoleIds(userId);
        return ResponseEntity.<List<Long>>builder()
                .data(roleIds)
                .info("获取用户角色ID列表成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }
}