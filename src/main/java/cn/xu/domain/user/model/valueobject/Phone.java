package cn.xu.domain.user.model.valueobject;

import lombok.Getter;

import java.util.regex.Pattern;

@Getter
public class Phone {
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);

    private final String number;

    public Phone(String number) {
        if (number != null && !PHONE_PATTERN.matcher(number).matches()) {
            throw new IllegalArgumentException("无效的手机号码格式");
        }
        this.number = number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Phone phone = (Phone) o;
        return number != null ? number.equals(phone.number) : phone.number == null;
    }

    @Override
    public int hashCode() {
        return number != null ? number.hashCode() : 0;
    }

    @Override
    public String toString() {
        return number;
    }
} 