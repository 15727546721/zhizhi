package cn.xu.repository.mapper;

import cn.xu.model.entity.UserSettings;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户设置Mapper接口
 * <p>处理用户设置的数据库操作</p>
 
 */
@Mapper
public interface UserSettingsMapper {
    
    /**
     * 插入用户设置
     */
    void insert(UserSettings userSettings);
    
    /**
     * 更新用户设置
     */
    void update(UserSettings userSettings);
    
    /**
     * 根据用户ID查询
     */
    UserSettings selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据邮箱验证令牌查询
     */
    UserSettings selectByEmailVerifyToken(@Param("token") String token);
    
    /**
     * 设置邮箱验证令牌
     */
    void setEmailVerifyToken(@Param("userId") Long userId, 
                             @Param("token") String token, 
                             @Param("expireTime") java.time.LocalDateTime expireTime);
    
    /**
     * 清空邮箱验证令牌（验证成功后调用）
     */
    void clearEmailVerifyToken(@Param("userId") Long userId);
    
    /**
     * 设置密码重置令牌
     */
    void setPasswordResetToken(@Param("userId") Long userId, 
                               @Param("token") String token, 
                               @Param("expireTime") java.time.LocalDateTime expireTime);
    
    /**
     * 根据密码重置令牌查询
     */
    UserSettings selectByPasswordResetToken(@Param("token") String token);
    
    /**
     * 清空密码重置令牌
     */
    void clearPasswordResetToken(@Param("userId") Long userId);
    
    /**
     * 删除用户设置（软删除，实际不使用）
     */
    void deleteByUserId(@Param("userId") Long userId);
}
