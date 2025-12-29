package cn.xu.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户类型枚举
 * 
 * <p>定义系统中不同类型的用户及其权限级别</p>
 * <ul>
 *   <li>NORMAL(1) - 普通用户，基础功能权限</li>
 *   <li>OFFICIAL(2) - 官方账号，内容管理权限</li>
 *   <li>ADMIN(3) - 管理员，系统管理权限</li>
 * </ul>
 */
@Getter
@AllArgsConstructor
public enum UserType {
    
    NORMAL(1, "普通用户"),
    OFFICIAL(2, "官方账号"),
    ADMIN(3, "管理员");

    private final Integer code;
    private final String description;

    /**
     * 根据code获取枚举
     */
    public static UserType fromCode(Integer code) {
        if (code == null) {
            return NORMAL;
        }
        for (UserType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return NORMAL;
    }

    /**
     * 判断是否有后台管理权限（官方账号及以上）
     */
    public boolean hasAdminAccess() {
        return this.code >= OFFICIAL.code;
    }

    /**
     * 判断是否为超级管理员
     */
    public boolean isSuperAdmin() {
        return this.code >= ADMIN.code;
    }

    /**
     * 判断是否为官方账号
     */
    public boolean isOfficial() {
        return this == OFFICIAL;
    }

    /**
     * 判断给定的code是否有后台管理权限
     */
    public static boolean hasAdminAccess(Integer code) {
        return code != null && code >= OFFICIAL.code;
    }

    /**
     * 判断给定的code是否为超级管理员
     */
    public static boolean isSuperAdmin(Integer code) {
        return code != null && code >= ADMIN.code;
    }

    /**
     * 判断给定的code是否为官方账号
     */
    public static boolean isOfficial(Integer code) {
        return code != null && code.equals(OFFICIAL.code);
    }
}
