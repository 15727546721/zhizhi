package cn.xu.api.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.permission.model.entity.MenuEntity;
import cn.xu.domain.permission.model.entity.MenuOptionsEntity;
import cn.xu.domain.permission.model.entity.RouterEntity;
import cn.xu.domain.permission.service.IPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Tag(name = "菜单管理", description = "菜单管理相关接口")
@RequestMapping("/system/menu")
@RestController
public class SysMenuController {

    @Resource
    private IPermissionService permissionService;

    @GetMapping(value = "/getMenuTree")
    @Operation(summary = "获取菜单树")
    @ApiOperationLog(description = "获取菜单树")
    public ResponseEntity<List<MenuEntity>> selectMenuTreeList() {
        List<MenuEntity> menuEntities = permissionService.getMenuTreeList();
        return ResponseEntity.<List<MenuEntity>>builder()
                .data(menuEntities)
                .info("获取菜单树成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @GetMapping(value = "/getMenuOptionsTree")
    @Operation(summary = "获取下拉菜单树")
    @ApiOperationLog(description = "获取下拉菜单树")
    public ResponseEntity<List<MenuOptionsEntity>> getMenuOptionsTree() {
        List<MenuOptionsEntity> menuOptionsTree = permissionService.getMenuOptionsTree();
        return ResponseEntity.<List<MenuOptionsEntity>>builder()
                .data(menuOptionsTree)
                .info("获取下拉菜单树成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @GetMapping(value = "/getUserMenu")
    @Operation(summary = "获取用户菜单")
    @ApiOperationLog(description = "获取用户菜单")
    public ResponseEntity getCurrentUserMenu() {
        List<RouterEntity> routerList = permissionService.getCurrentUserMenu();
        return ResponseEntity.<List<RouterEntity>>builder()
                .data(routerList)
                .info("获取用户菜单成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @SaCheckLogin
    @GetMapping(value = "/info/{id}")
    @Operation(summary = "菜单详情")
    @ApiOperationLog(description = "菜单详情")
    public ResponseEntity<MenuEntity> selectMenuById(@Parameter(description = "菜单ID") @PathVariable Long id) {
        Optional<MenuEntity> menuEntityOptional = permissionService.findMenuById(id);
        return ResponseEntity.<MenuEntity>builder()
                .data(menuEntityOptional.orElse(null))
                .info("获取菜单详情成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @PostMapping(value = "/add")
    @Operation(summary = "添加菜单")
    @ApiOperationLog(description = "添加菜单")
    public ResponseEntity addMenu(@RequestBody MenuEntity menu) {
        permissionService.addMenu(menu);
        return ResponseEntity.builder()
                .info("添加菜单成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @PostMapping(value = "/update")
    @Operation(summary = "修改菜单")
    @ApiOperationLog(description = "修改菜单")
    public ResponseEntity updateMenu(@RequestBody MenuEntity menu) {
        permissionService.updateMenu(menu);
        return ResponseEntity.builder()
                .info("修改菜单成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @GetMapping(value = "/delete/{id}")
    @Operation(summary = "删除菜单")
    @ApiOperationLog(description = "删除菜单")
    public ResponseEntity deleteMenu(@Parameter(description = "菜单ID") @PathVariable Long id) {
        permissionService.deleteMenu(id);
        return ResponseEntity.builder()
                .info("删除菜单成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }
}