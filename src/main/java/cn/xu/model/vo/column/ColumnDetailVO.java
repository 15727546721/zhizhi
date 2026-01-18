package cn.xu.model.vo.column;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 专栏详情视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ColumnDetailVO extends ColumnVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 最近文章(前5篇) */
    private List<ColumnPostVO> recentPosts;
    
    /** 是否所有者 */
    private Boolean isOwner;
}
