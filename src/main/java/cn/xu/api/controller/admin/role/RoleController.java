package cn.xu.api.controller.admin.role;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.xu.api.dto.common.PageResponse;
import cn.xu.api.dto.permission.RoleMenuRequest;
import cn.xu.api.dto.permission.RoleRequest;
import cn.xu.common.Constants;
import cn.xu.common.ResponseEntity;
import cn.xu.domain.permission.model.entity.RoleEntity;
import cn.xu.domain.permission.service.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "角色管理", description = "角色管理相关接口")
@RestController
@RequestMapping("/system/role")
public class RoleController {

    @Resource
    private PermissionService permissionService;

    @GetMapping("/list/{page}/{size}")
    @Operation(summary = "角色列表")
    public ResponseEntity selectRolePage(String name, @PathVariable("page") int page, @PathVariable("size") int size) {
        PageResponse<List<RoleEntity>> pageResponse = permissionService.selectRolePage(name, page, size);
        return ResponseEntity.builder()
                .data(pageResponse)
                .info("获取角色列表成功")
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .build();
    }

    @GetMapping("queryUserRole")
    @Operation(summary = "获取当前登录用户所拥有的权限")
    public ResponseEntity<List<Long>> getCurrentUserRole() {
        List<Long> currentUserRole = permissionService.getCurrentUserRole();
        return ResponseEntity.<List<Long>>builder()
                .data(currentUserRole)
                .info("获取当前登录用户所拥有的权限成功")
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .build();
    }

    @GetMapping("getRoleMenuIds")
    @Operation(summary = "获取当前登录用户所拥有的权限" )
    public ResponseEntity<List<Long>> selectRoleMenuById(Long roleId) {
        List<Long> roleMenuIds = permissionService.selectRoleMenuById(roleId);
        return ResponseEntity.<List<Long>>builder()
                .data(roleMenuIds)
                .info("获取当前登录用户所拥有的权限成功")
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .build();
    }

    @SaCheckPermission("system:role:assign")
    @PostMapping("updateRoleMenus")
    @Operation(summary = "分配角色权限")
    public ResponseEntity assignRoleMenus(@RequestBody RoleMenuRequest roleMenuRequest) {
        permissionService.assignRoleMenus(roleMenuRequest);
        return ResponseEntity.builder()
                .info("分配角色权限成功")
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .build();
    }

    @PostMapping(value = "add")
    @SaCheckPermission("system:role:add")
    @Operation(summary = "添加角色")
    public ResponseEntity addRole(@RequestBody RoleRequest role) {
        permissionService.addRole(role);
        return ResponseEntity.builder()
                .info("添加角色成功")
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .build();
    }

    @PostMapping("/update")
    @SaCheckPermission("system:role:update")
    @Operation(summary = "修改角色")
    public ResponseEntity updateRole(@RequestBody RoleRequest role) {
        permissionService.updateRole(role);
        return ResponseEntity.builder()
                .info("修改角色成功")
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .build();
    }

    @PostMapping("/delete")
    @SaCheckPermission("system:role:delete")
    @Operation(summary = "删除角色")
    public ResponseEntity deleteRole(@RequestBody List<Long> ids) {
        permissionService.deleteRoleByIds(ids);
        return ResponseEntity.builder()
                .info("删除角色成功")
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .build();
    }

}
