package cn.xu.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 更新系统配置DTO
 */
@Data
public class UpdateSystemConfigDTO {
    /**
     * 配置键
     */
    @NotBlank(message = "配置键不能为空")
    private String configKey;
    
    /**
     * 配置值
     */
    @NotBlank(message = "配置值不能为空")
    private String configValue;
}

