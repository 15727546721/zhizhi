package cn.xu.model.dto.system;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 更新系统配置请求
 */
@Data
public class UpdateSystemConfigRequest {
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
