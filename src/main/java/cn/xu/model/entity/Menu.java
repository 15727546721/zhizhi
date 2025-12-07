package cn.xu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 系统管理-权限资源表
 * 
 * @TableName menu
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Menu implements Serializable {
    
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
     * 子菜单列表（非数据库字段，用于构建树形结构）
     */
    private transient List<Menu> children;
}