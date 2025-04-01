package cn.xu.domain.user.model.valobj;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.Getter;

@Getter
public class Password {
    private final String value;

    public Password(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 6 || rawPassword.length() > 20) {
            throw new BusinessException("密码长度必须在6-20个字符之间");
        }
        this.value = rawPassword;
    }

    public String encode() {
        return SaSecureUtil.sha256(this.value);
    }

    public boolean matches(String encodedPassword) {
        return encodedPassword.equals(SaSecureUtil.sha256(this.value));
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
        return "******";
    }
} 