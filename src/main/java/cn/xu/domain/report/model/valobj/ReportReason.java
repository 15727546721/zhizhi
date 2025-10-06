package cn.xu.domain.report.model.valobj;

import cn.xu.common.exception.BusinessException;
import lombok.Getter;

/**
 * 举报原因值对象
 * 封装举报原因的业务规则和验证逻辑
 */
@Getter
public class ReportReason {
    
    private static final int MAX_LENGTH = 200;
    
    private final String value;

    public ReportReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("举报原因不能为空");
        }
        
        String trimmedReason = reason.trim();
        if (trimmedReason.length() > MAX_LENGTH) {
            throw new BusinessException("举报原因长度不能超过" + MAX_LENGTH + "个字符");
        }
        
        this.value = trimmedReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportReason that = (ReportReason) o;
        return value.equals(that.value);
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