package cn.xu.infrastructure.persistent.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户权限关联实体类
 * 用于存储用户与权限的直接关联关系
 * 
 * @author Lily
 */
@Data
public class UserPermission implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 权限ID
     */
    private Long permissionId;
    
    /**
     * 创建时间
     */
    private Date createTime;
}