package cn.xu.api.controller.admin.role;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.xu.api.dto.common.PageResponse;
import cn.xu.api.dto.permission.RoleMenuRequest;
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

    @RequestMapping(value = "queryUserRole", method = RequestMethod.GET)
    @Operation(summary = "获取当前登录用户所拥有的权限")
    public ResponseEntity<List<Long>> getCurrentUserRole() {
        List<Long> currentUserRole = permissionService.getCurrentUserRole();
        return ResponseEntity.<List<Long>>builder()
                .data(currentUserRole)
                .info("获取当前登录用户所拥有的权限成功")
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .build();
    }

    @RequestMapping(value = "getRoleMenuIds", method = RequestMethod.GET)
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
    @RequestMapping(value = "updateRoleMenus", method = RequestMethod.PUT)
    @Operation(summary = "分配角色权限")
    public ResponseEntity assignRoleMenus(@RequestBody RoleMenuRequest roleMenuRequest) {
        permissionService.assignRoleMenus(roleMenuRequest);
        return ResponseEntity.builder()
                .info("分配角色权限成功")
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .build();
    }
}
