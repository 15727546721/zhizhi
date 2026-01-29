package cn.xu.model.dto.favorite;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新收藏夹请求
 */
@Data
public class UpdateFolderRequest {
    
    @Size(max = 50, message = "收藏夹名称不能超过50个字符")
    private String name;
    
    @Size(max = 200, message = "描述不能超过200个字符")
    private String description;
    
    /** 是否公开 */
    private Boolean isPublic;
}
