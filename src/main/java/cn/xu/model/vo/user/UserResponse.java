package cn.xu.model.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户响应数据
 * 用于返回用户的详细信息
 * 
 * @author zhizhi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "用户响应数据")
public class UserResponse implements java.io.Serializable {
    
    @Schema(description = "用户ID")
    private Long id;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "邮箱")
    private String email;
    
    @Schema(description = "昵称")
    private String nickname;
    
    @Schema(description = "头像")
    private String avatar;
    
    @Schema(description = "性别")
    private Integer gender;
    
    @Schema(description = "手机号")
    private String phone;
    
    @Schema(description = "地区")
    private String region;
    
    @Schema(description = "生日")
    private String birthday;
    
    @Schema(description = "个人描述")
    private String description;
    
    @Schema(description = "用户状态")
    private Integer status;
    
    @Schema(description = "关注数")
    private Long followCount;
    
    @Schema(description = "粉丝数")
    private Long fansCount;
    
    @Schema(description = "获赞数")
    private Long likeCount;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}