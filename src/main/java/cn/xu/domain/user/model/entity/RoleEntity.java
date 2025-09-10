package cn.xu.domain.user.model.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 角色领域实体
 * 封装角色相关的业务逻辑
 */
@Data
@Builder
public class RoleEntity {
    private Long id;
    private String name;
    private String code;
    private String desc;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<String> permissions = new ArrayList<>();

    // ==================== 业务方法 ====================

    /**
     * 检查角色是否具有指定权限
     */
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }

    /**
     * 添加权限
     */
    public void addPermission(String permission) {
        if (permission == null || permission.trim().isEmpty()) {
            return;
        }
        
        if (permissions == null) {
            permissions = new ArrayList<>();
        }
        
        if (!permissions.contains(permission)) {
            permissions.add(permission);
        }
    }

    /**
     * 移除权限
     */
    public void removePermission(String permission) {
        if (permissions != null) {
            permissions.remove(permission);
        }
    }

    /**
     * 判断是否为管理员角色
     */
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(this.code) || "administrator".equalsIgnoreCase(this.code);
    }

    /**
     * 判断是否为普通用户角色
     */
    public boolean isUser() {
        return "user".equalsIgnoreCase(this.code);
    }
} 