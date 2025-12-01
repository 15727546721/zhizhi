package cn.xu.model.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "用户登录响应数据")
public class UserLoginResponse implements java.io.Serializable {
    @Schema(description = "用户信息")
    UserResponse userInfo;
    
    @Schema(description = "登录令牌")
    String token;
}