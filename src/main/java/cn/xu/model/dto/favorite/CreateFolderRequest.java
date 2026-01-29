package cn.xu.model.dto.favorite;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建收藏夹请求
 */
@Data
public class CreateFolderRequest {
    
    @NotBlank(message = "收藏夹名称不能为空")
    @Size(max = 50, message = "收藏夹名称不能超过50个字符")
    private String name;
    
    @Size(max = 200, message = "描述不能超过200个字符")
    private String description;
    
    /** 是否公开，默认私密 */
    private Boolean isPublic = false;
}
