package cn.xu.domain.user.model.valobj;

import cn.xu.common.exception.BusinessException;
import lombok.Getter;

import java.util.regex.Pattern;

/**
 * 手机号值对象
 * 封装手机号的业务规则和验证逻辑
 */
@Getter
public class Phone {
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);

    private final String value;

    public Phone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new BusinessException("手机号不能为空");
        }
        
        String trimmedPhone = phone.trim();
        if (!PHONE_PATTERN.matcher(trimmedPhone).matches()) {
            throw new BusinessException("手机号格式不正确，请输入有效的11位手机号");
        }
        
        this.value = trimmedPhone;
    }
    
    /**
     * 获取手机号值
     * @return 手机号字符串
     */
    public String getValue() {
        return value;
    }
    
    /**
     * 获取脱敏手机号（保留前3位和后4位）
     * @return 脱敏后的手机号
     */
    public String getMaskedValue() {
        if (value == null || value.length() != 11) {
            return value;
        }
        return value.substring(0, 3) + "****" + value.substring(7);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Phone phone = (Phone) o;
        return value.equals(phone.value);
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