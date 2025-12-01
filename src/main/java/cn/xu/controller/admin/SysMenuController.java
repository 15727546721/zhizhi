package cn.xu.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.entity.Menu;
import cn.xu.model.vo.permission.MenuOptionsVO;
import cn.xu.model.vo.permission.RouterVO;
import cn.xu.service.permission.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * 菜单管理控制器
 * 
 * @author xu
 * @since 2025-11-30
 */
@RestController
@RequestMapping("/api/system/menu")
@Tag(name = "菜单管理", description = "菜单管理相关接口")
public class SysMenuController {

    @Resource
    private PermissionService permissionService;

    @GetMapping(value = "/getMenuTree")
    @Operation(summary = "获取菜单树")
    @SaCheckLogin
    @SaCheckPermission("system:menu:list")
    @ApiOperationLog(description = "获取菜单树")
    public ResponseEntity<List<Menu>> selectMenuTreeList() {
        List<Menu> menuEntities = permissionService.getMenuTreeList();
        return ResponseEntity.<List<Menu>>builder()
                .data(menuEntities)
                .info("获取菜单树成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @GetMapping(value = "/getMenuOptionsTree")
    @Operation(summary = "获取下拉菜单树")
    @SaCheckLogin
    @ApiOperationLog(description = "获取下拉菜单树")
    public ResponseEntity<List<MenuOptionsVO>> getMenuOptionsTree() {
        List<MenuOptionsVO> menuOptionsTree = permissionService.getMenuOptionsTree();
        return ResponseEntity.<List<MenuOptionsVO>>builder()
                .data(menuOptionsTree)
                .info("获取下拉菜单树成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @GetMapping(value = "/getUserMenu")
    @Operation(summary = "获取用户菜单")
    @SaCheckLogin
    @ApiOperationLog(description = "获取用户菜单")
    public ResponseEntity getCurrentUserMenu() {
        List<RouterVO> routerList = permissionService.getCurrentUserMenu();
        return ResponseEntity.<List<RouterVO>>builder()
                .data(routerList)
                .info("获取用户菜单成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @SaCheckLogin
    @GetMapping(value = "/info/{id}")
    @Operation(summary = "菜单详情")
    @ApiOperationLog(description = "菜单详情")
    public ResponseEntity<Menu> selectMenuById(@Parameter(description = "菜单ID") @PathVariable Long id) {
        Optional<Menu> menuEntityOptional = permissionService.findMenuById(id);
        return ResponseEntity.<Menu>builder()
                .data(menuEntityOptional.orElse(null))
                .info("获取菜单详情成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @PostMapping(value = "/add")
    @Operation(summary = "添加菜单")
    @SaCheckLogin
    @SaCheckPermission("system:menu:add")
    @ApiOperationLog(description = "添加菜单")
    public ResponseEntity addMenu(@RequestBody Menu menu) {
        permissionService.addMenu(menu);
        return ResponseEntity.builder()
                .info("添加菜单成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @PostMapping(value = "/update")
    @Operation(summary = "修改菜单")
    @SaCheckLogin
    @SaCheckPermission("system:menu:update")
    @ApiOperationLog(description = "修改菜单")
    public ResponseEntity updateMenu(@RequestBody Menu menu) {
        permissionService.updateMenu(menu);
        return ResponseEntity.builder()
                .info("修改菜单成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @GetMapping(value = "/delete/{id}")
    @Operation(summary = "删除菜单")
    @SaCheckLogin
    @SaCheckPermission("system:menu:delete")
    @ApiOperationLog(description = "删除菜单")
    public ResponseEntity deleteMenu(@Parameter(description = "菜单ID") @PathVariable Long id) {
        permissionService.deleteMenu(id);
        return ResponseEntity.builder()
                .info("删除菜单成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }
}
