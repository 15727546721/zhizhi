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
 * <p>提供角色管理功能，包括增、删、改、查操作</p>
 * <p>需要有相应的权限才能访问</p>
 
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/system/role")
@Tag(name = "角色管理", description = "角色管理相关接口")
public class SysRoleController {

    private PermissionService permissionService;

    /**
     * 查询角色列表
     *
     * <p>实现角色的分页查询功能
     * <p>需要system:role:list权限
     *
     * @param roleRequest 查询参数，包括角色名称和分页信息
     * @return 角色的分页数据
     */
    @GetMapping("/list")
    @Operation(summary = "查询角色列表")
    @SaCheckLogin
    @SaCheckPermission("system:role:list")
    @ApiOperationLog(description = "获取角色列表")
    public ResponseEntity<PageResponse<List<Role>>> selectRolePage(RoleRequest roleRequest) {

        // 参数验证和默认值设置
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
                .info("查询角色列表成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    /**
     * 查询当前登录用户的角色IDs
     *
     * <p>根据当前用户获取其角色ID列表
     * <p>需要登录权限
     *
     * @return 当前用户的角色ID列表
     */
    @GetMapping("queryUserRole")
    @Operation(summary = "查询当前登录用户的角色ID")
    @SaCheckLogin
    @ApiOperationLog(description = "获取当前登录用户的角色ID")
    public ResponseEntity<List<Long>> getCurrentUserRole() {
        List<Long> currentUserRole = permissionService.getCurrentUserRoleMenuIds();
        return ResponseEntity.<List<Long>>builder()
                .data(currentUserRole)
                .info("查询当前登录用户的角色ID成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    /**
     * 查询角色的菜单ID列表
     *
     * <p>根据角色ID查询其关联的菜单ID列表
     * <p>需要system:role:list权限
     *
     * @param roleId 角色ID
     * @return 角色的菜单ID列表
     * @throws BusinessException 角色ID不能为空时抛出异常
     */
    @GetMapping("getRoleMenuIds")
    @Operation(summary = "查询角色的菜单ID")
    @SaCheckLogin
    @SaCheckPermission("system:role:list")
    @ApiOperationLog(description = "获取角色的菜单ID")
    public ResponseEntity<List<Long>> selectRoleMenuById(@Parameter(description = "角色ID") @RequestParam("roleId") Long roleId) {
        if (roleId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "角色ID不能为空");
        }
        List<Long> roleMenuIds = permissionService.findRoleMenuIdsById(roleId);
        return ResponseEntity.<List<Long>>builder()
                .data(roleMenuIds)
                .info("查询角色菜单成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    /**
     * 分配角色菜单
     *
     * <p>分配角色所对应的菜单
     * <p>需要system:role:assign权限
     *
     * @param roleMenuRequest 分配请求，包括角色ID和菜单ID列表
     * @return 操作结果
     */
    @PostMapping("updateRoleMenus")
    @Operation(summary = "分配角色菜单")
    @SaCheckLogin
    @SaCheckPermission("system:role:assign")
    @ApiOperationLog(description = "分配角色菜单")
    public ResponseEntity assignRoleMenus(@RequestBody RoleMenuRequest roleMenuRequest) {
        permissionService.assignRoleMenus(roleMenuRequest);
        return ResponseEntity.builder()
                .info("分配角色菜单成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    /**
     * 添加角色
     *
     * <p>创建一个新的角色
     * <p>需要system:role:add权限
     *
     * @param role 角色信息，包括角色名称、描述等
     * @return 添加结果
     */
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

    /**
     * 更新角色
     *
     * <p>更新已有角色的信息
     * <p>需要system:role:update权限
     *
     * @param role 角色信息，包括角色ID和新更新的角色数据
     * @return 更新结果
     */
    @PostMapping("/update")
    @Operation(summary = "更新角色")
    @SaCheckLogin
    @SaCheckPermission("system:role:update")
    @ApiOperationLog(description = "更新角色")
    public ResponseEntity updateRole(@RequestBody RoleAddOrUpdateRequest role) {
        permissionService.updateRole(role);
        return ResponseEntity.builder()
                .info("更新角色成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    /**
     * 删除角色
     *
     * <p>根据角色ID删除角色
     * <p>需要system:role:delete权限
     *
     * @param ids 角色ID列表
     * @return 删除结果
     */
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
