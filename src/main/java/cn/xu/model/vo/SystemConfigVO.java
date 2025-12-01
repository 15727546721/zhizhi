package cn.xu.model.vo;

import lombok.Data;

/**
 * 系统配置VO
 */
@Data
public class SystemConfigVO {
    /**
     * 配置ID
     */
    private Long id;
    
    /**
     * 配置键
     */
    private String configKey;
    
    /**
     * 配置值
     */
    private String configValue;
    
    /**
     * 配置描述
     */
    private String configDesc;
}
