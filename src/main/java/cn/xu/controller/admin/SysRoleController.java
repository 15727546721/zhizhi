package cn.xu.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.dto.permission.RoleAddOrUpdateRequest;
import cn.xu.model.dto.permission.RoleMenuRequest;
import cn.xu.model.dto.permission.RoleRequest;
import cn.xu.model.entity.Role;
import cn.xu.service.permission.PermissionService;
import cn.xu.support.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 * 
 * @author xu
 * @since 2025-11-30
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/system/role")
@Tag(name = "角色管理", description = "角色管理相关接口")
public class SysRoleController {

    private PermissionService permissionService;

    @GetMapping("/list")
    @Operation(summary = "角色列表")
    @SaCheckLogin
    @SaCheckPermission("system:role:list")
    @ApiOperationLog(description = "获取角色列表")
    public ResponseEntity<PageResponse<List<Role>>> selectRolePage(RoleRequest roleRequest) {

        // 参数校验和默认值设置
        if (roleRequest.getPageNo() == null || roleRequest.getPageNo() < 1) {
            roleRequest.setPageNo(1);
        }
        if (roleRequest.getPageSize() == null || roleRequest.getPageSize() < 1) {
            roleRequest.setPageSize(10);
        }

        // 查询角色列表
        PageResponse<List<Role>> pageResponse = permissionService.findRolePage(
                roleRequest.getName(),
                roleRequest.getPageNo(),
                roleRequest.getPageSize()
        );

        return ResponseEntity.<PageResponse<List<Role>>>builder()
                .data(pageResponse)
                .info("获取角色列表成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @GetMapping("queryUserRole")
    @Operation(summary = "获取当前登录用户所拥有的权限")
    @SaCheckLogin
    @ApiOperationLog(description = "获取当前登录用户所拥有的权限")
    public ResponseEntity<List<Long>> getCurrentUserRole() {
        List<Long> currentUserRole = permissionService.getCurrentUserRoleMenuIds();
        return ResponseEntity.<List<Long>>builder()
                .data(currentUserRole)
                .info("获取当前登录用户所拥有的权限成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @GetMapping("getRoleMenuIds")
    @Operation(summary = "获取角色的菜单权限")
    @SaCheckLogin
    @SaCheckPermission("system:role:list")
    @ApiOperationLog(description = "获取角色的菜单权限")
    public ResponseEntity<List<Long>> selectRoleMenuById(@Parameter(description = "角色ID") @RequestParam("roleId") Long roleId) {
        if (roleId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "角色ID不能为空");
        }
        List<Long> roleMenuIds = permissionService.findRoleMenuIdsById(roleId);
        return ResponseEntity.<List<Long>>builder()
                .data(roleMenuIds)
                .info("获取角色菜单权限成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @PostMapping("updateRoleMenus")
    @Operation(summary = "分配角色权限")
    @SaCheckLogin
    @SaCheckPermission("system:role:assign")
    @ApiOperationLog(description = "分配角色权限")
    public ResponseEntity assignRoleMenus(@RequestBody RoleMenuRequest roleMenuRequest) {
        permissionService.assignRoleMenus(roleMenuRequest);
        return ResponseEntity.builder()
                .info("分配角色权限成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @PostMapping(value = "add")
    @Operation(summary = "添加角色")
    @SaCheckLogin
    @SaCheckPermission("system:role:add")
    @ApiOperationLog(description = "添加角色")
    public ResponseEntity addRole(@RequestBody RoleAddOrUpdateRequest role) {
        permissionService.addRole(role);
        return ResponseEntity.builder()
                .info("添加角色成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @PostMapping("/update")
    @Operation(summary = "修改角色")
    @SaCheckLogin
    @SaCheckPermission("system:role:update")
    @ApiOperationLog(description = "修改角色")
    public ResponseEntity updateRole(@RequestBody RoleAddOrUpdateRequest role) {
        permissionService.updateRole(role);
        return ResponseEntity.builder()
                .info("修改角色成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @PostMapping("/delete")
    @Operation(summary = "删除角色")
    @SaCheckLogin
    @SaCheckPermission("system:role:delete")
    @ApiOperationLog(description = "删除角色")
    public ResponseEntity deleteRole(@Parameter(description = "角色ID列表") @RequestBody List<Long> ids) {
        permissionService.deleteRoleByIds(ids);
        return ResponseEntity.builder()
                .info("删除角色成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

}
