package cn.xu.infrastructure.persistent.po;

import lombok.Data;

@Data
public class Menu {
    private Long id;                // 菜单唯一标识符
    private Long parentId;          // 父级菜单的 ID, 根菜单为 NULL
    private String path;            // 菜单路径
    private String name;            // 菜单名称
    private String component;       // 组件名称或路径
    private String icon;            // 菜单图标
    private String title;           // 菜单标题
    private Long order;             // 菜单排序
}
