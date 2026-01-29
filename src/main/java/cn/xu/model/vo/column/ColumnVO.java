package cn.xu.model.vo.column;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 专栏视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 专栏ID */
    private Long id;
    
    /** 所有者ID */
    private Long userId;
    
    /** 所有者用户名 */
    private String userName;
    
    /** 所有者头像 */
    private String userAvatar;
    
    /** 专栏名称 */
    private String name;
    
    /** 描述 */
    private String description;
    
    /** 封面图URL */
    private String coverUrl;
    
    /** 状态: 0-草稿 1-已发布 2-已归档 */
    private Integer status;
    
    /** 文章数 */
    private Integer postCount;
    
    /** 订阅数 */
    private Integer subscribeCount;
    
    /** 当前用户是否已订阅 */
    private Boolean isSubscribed;
    
    /** 是否推荐 */
    private Boolean isRecommended;
    
    /** 最后发文时间 */
    private LocalDateTime lastPostTime;
    
    /** 创建时间 */
    private LocalDateTime createTime;
}
