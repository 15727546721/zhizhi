package cn.xu.controller.admin;

import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.dto.user.UserRoleRequest;
import cn.xu.service.user.UserRoleService;
import cn.xu.support.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 用户角色管理控制器
 * 
 * <p>提供用户角色分配和查询功能
 * <p>用于管理用户与角色的关联关系
 */
@Tag(name = "用户角色管理", description = "用户角色管理相关接口")
@RestController
@RequestMapping("/api/system/user-role")
public class SysUserRoleController {

    @Resource
    private UserRoleService userRoleService;

    /**
     * 为用户分配角色
     * 
     * <p>先删除用户原角色，再分配新角色
     * 
     * @param userRoleRequest 分配请求，包含用户ID和角色ID列表
     * @return 分配结果
     * @throws BusinessException 当用户ID为空时抛出
     */
    @PostMapping("/assign")
    @Operation(summary = "为用户分配角色")
    @ApiOperationLog(description = "为用户分配角色")
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

    /**
     * 获取用户的角色ID列表
     * 
     * <p>查询用户已分配的所有角色ID
     * 
     * @param userId 用户ID
     * @return 角色ID列表
     * @throws BusinessException 当用户ID为空时抛出
     */
    @GetMapping("/user-roles")
    @Operation(summary = "获取用户的角色ID列表")
    @ApiOperationLog(description = "获取用户的角色ID列表")
    public ResponseEntity<List<Long>> getUserRoleIds(@Parameter(description = "用户ID") @RequestParam("userId") Long userId) {
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