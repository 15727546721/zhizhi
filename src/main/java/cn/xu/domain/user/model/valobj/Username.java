package cn.xu.domain.user.model.valobj;

import cn.xu.common.exception.BusinessException;
import lombok.Getter;

/**
 * 用户名值对象
 * 封装用户名的业务规则和验证逻辑
 */
@Getter
public class Username {
    
    private static final int MIN_LENGTH = 4;
    private static final int MAX_LENGTH = 20;
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$";
    
    private final String value;

    public Username(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        
        String trimmedUsername = username.trim();
        
        if (trimmedUsername.length() < MIN_LENGTH) {
            throw new BusinessException("用户名长度不能少于" + MIN_LENGTH + "个字符");
        }
        
        if (trimmedUsername.length() > MAX_LENGTH) {
            throw new BusinessException("用户名长度不能超过" + MAX_LENGTH + "个字符");
        }
        
        if (!trimmedUsername.matches(USERNAME_PATTERN)) {
            throw new BusinessException("用户名只能包含字母、数字、下划线和中文字符");
        }
        
        if (isReservedUsername(trimmedUsername)) {
            throw new BusinessException("该用户名为系统保留，请选择其他用户名");
        }
        
        this.value = trimmedUsername;
    }

    private boolean isReservedUsername(String username) {
        // 系统保留用户名
        String[] reservedNames = {
//            "admin", "administrator", "root", "system", "support",
//            "service", "test", "guest", "user", "null", "undefined",
//            "管理员", "系统", "测试", "客服"
        };
        
        String lowerUsername = username.toLowerCase();
        for (String reserved : reservedNames) {
            if (lowerUsername.equals(reserved.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Username username = (Username) o;
        return value.equals(username.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}