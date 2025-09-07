package cn.xu.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;

/**
 * 举报原因配置持久化对象
 */
@Data
public class ReportReasonConfig {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 原因编码
     */
    private String reasonCode;
    
    /**
     * 原因名称
     */
    private String reasonName;
    
    /**
     * 适用的目标类型 1-文章 2-评论 3-用户 4-话题 0-通用
     */
    private Integer targetType;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 是否启用 1-启用 0-禁用
     */
    private Integer isEnabled;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
}