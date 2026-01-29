package cn.xu.model.enums;

import lombok.Getter;

/**
 * 搜索策略类型枚举
 */
@Getter
public enum SearchStrategyType {
    
    AUTO("auto", "自动选择"),
    MYSQL("mysql", "MySQL搜索"),
    ELASTICSEARCH("elasticsearch", "Elasticsearch搜索");
    
    private final String code;
    private final String description;
    
    SearchStrategyType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 根据code获取枚举
     */
    public static SearchStrategyType fromCode(String code) {
        if (code == null) {
            return AUTO;
        }
        for (SearchStrategyType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return AUTO;
    }
    
    /**
     * 判断是否为指定策略
     */
    public boolean is(String code) {
        return this.code.equalsIgnoreCase(code);
    }
}
