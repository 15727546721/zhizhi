package cn.xu.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;

/**
 * 举报持久化对象
 */
@Data
public class Report {
    
    /**
     * 举报ID
     */
    private Long id;
    
    /**
     * 举报类型 1-文章 2-评论 3-用户 4-话题
     */
    private Integer targetType;
    
    /**
     * 被举报的目标ID
     */
    private Long targetId;
    
    /**
     * 举报人ID
     */
    private Long reporterId;
    
    /**
     * 举报原因
     */
    private String reason;
    
    /**
     * 举报详情
     */
    private String detail;
    
    /**
     * 举报状态 0-待处理 1-已处理 2-已忽略
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 处理时间
     */
    private Date handleTime;
    
    /**
     * 处理人ID
     */
    private Long handlerId;
    
    /**
     * 处理结果
     */
    private String handleResult;
}