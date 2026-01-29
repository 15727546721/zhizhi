package cn.xu.model.vo.column;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 专栏文章导航视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnPostNavigationVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 上一篇文章ID */
    private Long previousPostId;
    
    /** 上一篇文章标题 */
    private String previousPostTitle;
    
    /** 下一篇文章ID */
    private Long nextPostId;
    
    /** 下一篇文章标题 */
    private String nextPostTitle;
}
