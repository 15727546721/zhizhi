package cn.xu.domain.permission.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 菜单领域实体
 * 用于表示系统中的菜单资源，包含菜单的基本信息和层级关系
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MenuEntity {
    /**
     * 主键
     */
    private Long id;

    /**
     * 上级资源ID
     */
    private Long parentId;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 菜单名称
     */
    private String title;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 资源图标
     */
    private String icon;

    /**
     * 类型 menu、button
     */
    private String type;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 重定向地址
     */
    private String redirect;

    /**
     * 跳转地址
     */
    private String name;

    /**
     * 是否隐藏
     */
    private Integer hidden;

    /**
     * 权限标识
     */
    private String perm;

    /**
     * 子菜单列表
     */
    private List<MenuEntity> children;
}