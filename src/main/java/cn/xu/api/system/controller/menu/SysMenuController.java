package cn.xu.api.system.controller.menu;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.xu.api.web.model.dto.common.ResponseEntity;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.permission.model.entity.MenuEntity;
import cn.xu.domain.permission.model.entity.MenuOptionsEntity;
import cn.xu.domain.permission.model.entity.RouterEntity;
import cn.xu.domain.permission.service.IPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "菜单管理", description = "菜单管理相关接口")
@RequestMapping("/system/menu")
@RestController
public class SysMenuController {

    @Resource
    private IPermissionService permissionService;

    @GetMapping(value = "/getMenuTree")
    @Operation(summary = "获取菜单树")
    public ResponseEntity<List<MenuEntity>> selectMenuTreeList() {
        List<MenuEntity> menuEntities = permissionService.selectMenuTreeList();
        return ResponseEntity.<List<MenuEntity>>builder()
                .data(menuEntities)
                .info("获取菜单树成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @GetMapping(value = "/getMenuOptionsTree")
    @Operation(summary = "获取下拉菜单树")
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
    public ResponseEntity<MenuEntity> selectMenuById(@PathVariable Long id) {
        return ResponseEntity.<MenuEntity>builder()
                .data(permissionService.selectMenuById(id))
                .info("获取菜单详情成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @PostMapping(value = "/add")
    @Operation(summary = "添加菜单")
    public ResponseEntity addMenu(@RequestBody MenuEntity menu) {
        permissionService.addMenu(menu);
        return ResponseEntity.builder()
                .info("添加菜单成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @PostMapping(value = "/update")
    @Operation(summary = "修改菜单")
    public ResponseEntity updateMenu(@RequestBody MenuEntity menu) {
        permissionService.updateMenu(menu);
        return ResponseEntity.builder()
                .info("修改菜单成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @GetMapping(value = "/delete/{id}")
    @Operation(summary = "删除菜单")
    public ResponseEntity deleteMenu(@PathVariable Long id) {
        permissionService.deleteMenu(id);
        return ResponseEntity.builder()
                .info("删除菜单成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }
}
