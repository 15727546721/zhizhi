package cn.xu.model.dto.column;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 创建专栏DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnCreateDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 专栏名称 */
    @NotBlank(message = "专栏名称不能为空")
    @Size(min = 2, max = 50, message = "专栏名称长度为2-50个字符")
    private String name;
    
    /** 描述 */
    @Size(max = 200, message = "描述不能超过200个字符")
    private String description;
    
    /** 封面图URL */
    private String coverUrl;
    
    /** 状态: 0-草稿 1-已发布 */
    private Integer status;
}
