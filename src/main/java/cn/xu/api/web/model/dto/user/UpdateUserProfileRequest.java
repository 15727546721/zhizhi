package cn.xu.api.web.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 更新用户资料请求DTO
 * 只包含用户可自行修改的字段，符合DDD原则
 * 
 * @author zhizhi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新用户资料请求")
public class UpdateUserProfileRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "昵称", example = "张三")
    @Size(min = 2, max = 12, message = "昵称长度必须在2-12个字符之间")
    private String nickname;
    
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;
    
    @Schema(description = "性别：1-男，2-女", example = "1")
    private Integer gender;
    
    @Schema(description = "手机号", example = "13800138000")
    @Size(max = 11, message = "手机号长度不能超过11位")
    private String phone;
    
    @Schema(description = "地区", example = "北京市")
    private String region;
    
    @Schema(description = "生日，格式：YYYY-MM-DD", example = "1990-01-01")
    private String birthday;
    
    @Schema(description = "个人简介", example = "这是我的个人简介")
    @Size(max = 200, message = "个人简介长度不能超过200个字符")
    private String description;
}

