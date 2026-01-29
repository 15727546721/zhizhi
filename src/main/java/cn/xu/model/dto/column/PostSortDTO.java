package cn.xu.model.dto.column;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 文章排序DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSortDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 帖子ID */
    @NotNull(message = "帖子ID不能为空")
    private Long postId;
    
    /** 排序值 */
    @NotNull(message = "排序值不能为空")
    private Integer sort;
}
