package cn.xu.domain.user.model.valobj;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.Getter;

import java.util.regex.Pattern;

/**
 * 密码值对象
 * 封装密码的业务规则和验证逻辑
 */
@Getter
public class Password {
    
    private static final int MIN_LENGTH = 6;
    private static final int MAX_LENGTH = 20;
    
    // 至少包含一个字母和一个数字
    private static final String STRONG_PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{6,}$";
    private static final Pattern PATTERN = Pattern.compile(STRONG_PASSWORD_PATTERN);
    
    private final String value;

    public Password(String password) {
        if (password == null || password.isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        
        if (password.length() < MIN_LENGTH) {
            throw new BusinessException("密码长度不能少于" + MIN_LENGTH + "个字符");
        }
        
        if (password.length() > MAX_LENGTH) {
            throw new BusinessException("密码长度不能超过" + MAX_LENGTH + "个字符");
        }
        
        if (!PATTERN.matcher(password).matches()) {
            throw new BusinessException("密码必须包含至少一个字母和一个数字，可以包含特殊字符@$!%*?&");
        }
        
        if (isWeakPassword(password)) {
            throw new BusinessException("密码强度过弱，请使用更复杂的密码");
        }
        
        // 在构造函数中对密码进行加密
        this.value = SaSecureUtil.sha256(password);
    }

    private boolean isWeakPassword(String password) {
        // 检查常见弱密码
        String[] weakPasswords = {
            "123456", "password", "123456789", "12345678", "12345",
            "qwerty", "abc123", "111111", "password123", "admin",
            "letmein", "welcome", "monkey", "1234567890"
        };
        
        String lowerPassword = password.toLowerCase();
        for (String weak : weakPasswords) {
            if (lowerPassword.contains(weak)) {
                return true;
            }
        }
        
        // 检查是否为纯数字或纯字母
        if (password.matches("^\\d+$") || password.matches("^[a-zA-Z]+$")) {
            return true;
        }
        
        return false;
    }

    /**
     * 验证密码是否匹配
     * @param inputPassword 输入的密码
     * @param encodedPassword 已编码的密码
     * @return 是否匹配
     */
    public static boolean matches(String inputPassword, String encodedPassword) {
        // 使用SaSecureUtil对输入密码进行加密后比较
        String encryptedInput = SaSecureUtil.sha256(inputPassword);
        return encryptedInput.equals(encodedPassword);
    }

    /**
     * 获取加密后的密码
     * @return 加密后的密码
     */
    public String getEncodedValue() {
        // 返回已经加密的密码值
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password = (Password) o;
        return value.equals(password.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        // 永远不要在toString中返回真实密码
        return "******";
    }
}