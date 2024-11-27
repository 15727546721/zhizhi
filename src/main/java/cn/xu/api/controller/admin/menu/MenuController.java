package cn.xu.api.controller.admin.menu;

import cn.xu.common.Constants;
import cn.xu.common.ResponseEntity;
import cn.xu.domain.permission.model.entity.MenuEntity;
import cn.xu.domain.permission.service.IPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "菜单管理", description = "菜单管理相关接口")
@RequestMapping("/system/menu")
@RestController
public class MenuController {

    @Resource
    private IPermissionService permissionService;

    @GetMapping(value = "/getMenuTree")
    @Operation(summary = "获取菜单树")
    public ResponseEntity<List<MenuEntity>> selectMenuTreeList() {
        List<MenuEntity> menuEntities = permissionService.selectMenuTreeList();
        return ResponseEntity.<List<MenuEntity>>builder()
                .data(menuEntities)
                .info("获取菜单树成功")
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .build();
    }

}
