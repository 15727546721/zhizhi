package cn.xu.api.controller.system.role;

import cn.xu.api.dto.common.PageResponse;
import cn.xu.api.dto.common.ResponseEntity;
import cn.xu.api.dto.permission.RoleAddOrUpdateRequest;
import cn.xu.api.dto.permission.RoleMenuRequest;
import cn.xu.api.dto.permission.RoleRequest;
import cn.xu.domain.permission.model.entity.RoleEntity;
import cn.xu.domain.permission.service.IPermissionService;
import cn.xu.exception.BusinessException;
import cn.xu.infrastructure.common.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "角色管理", description = "角色管理相关接口")
@RestController
@RequestMapping("/system/role")
public class SysRoleController {

    @Resource
    private IPermissionService permissionService;

    @GetMapping("/list")
    @Operation(summary = "角色列表")
    public ResponseEntity<PageResponse<List<RoleEntity>>> selectRolePage(RoleRequest roleRequest) {

        // 参数校验和默认值设置
        if (roleRequest.getPageNo() == null || roleRequest.getPageNo() < 1) {
            roleRequest.setPageNo(1);
        }
        if (roleRequest.getPageSize() == null || roleRequest.getPageSize() < 1) {
            roleRequest.setPageSize(10);
        }

        // 查询角色列表
        PageResponse<List<RoleEntity>> pageResponse = permissionService.selectRolePage(
                roleRequest.getName(),
                roleRequest.getPageNo(),
                roleRequest.getPageSize()
        );

        return ResponseEntity.<PageResponse<List<RoleEntity>>>builder()
                .data(pageResponse)
                .info("获取角色列表成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @GetMapping("queryUserRole")
    @Operation(summary = "获取当前登录用户所拥有的权限")
    public ResponseEntity<List<Long>> getCurrentUserRole() {
        List<Long> currentUserRole = permissionService.getCurrentUserRole();
        return ResponseEntity.<List<Long>>builder()
                .data(currentUserRole)
                .info("获取当前登录用户所拥有的权限成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @GetMapping("getRoleMenuIds")
    @Operation(summary = "获取角色的菜单权限")
    public ResponseEntity<List<Long>> selectRoleMenuById(@RequestParam("roleId") Long roleId) {
        if (roleId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "角色ID不能为空");
        }
        List<Long> roleMenuIds = permissionService.selectRoleMenuById(roleId);
        return ResponseEntity.<List<Long>>builder()
                .data(roleMenuIds)
                .info("获取角色菜单权限成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @PostMapping("updateRoleMenus")
    @Operation(summary = "分配角色权限")
    public ResponseEntity assignRoleMenus(@RequestBody RoleMenuRequest roleMenuRequest) {
        permissionService.assignRoleMenus(roleMenuRequest);
        return ResponseEntity.builder()
                .info("分配角色权限成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @PostMapping(value = "add")
    @Operation(summary = "添加角色")
    public ResponseEntity addRole(@RequestBody RoleAddOrUpdateRequest role) {
        permissionService.addRole(role);
        return ResponseEntity.builder()
                .info("添加角色成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @PostMapping("/update")
    @Operation(summary = "修改角色")
    public ResponseEntity updateRole(@RequestBody RoleAddOrUpdateRequest role) {
        permissionService.updateRole(role);
        return ResponseEntity.builder()
                .info("修改角色成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @PostMapping("/delete")
    @Operation(summary = "删除角色")
    public ResponseEntity deleteRole(@RequestBody List<Long> ids) {
        permissionService.deleteRoleByIds(ids);
        return ResponseEntity.builder()
                .info("删除角色成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

}
