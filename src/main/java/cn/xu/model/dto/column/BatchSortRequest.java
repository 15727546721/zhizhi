package cn.xu.model.dto.column;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 批量排序请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchSortRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 排序列表 */
    @NotEmpty(message = "排序列表不能为空")
    @Valid
    private List<PostSortDTO> sortList;
}
