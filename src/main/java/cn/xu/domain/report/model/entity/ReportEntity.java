package cn.xu.domain.report.model.entity;

import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 举报实体
 * 用于处理用户举报内容的业务逻辑
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportEntity {
    
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
    private LocalDateTime createTime;
    
    /**
     * 处理时间
     */
    private LocalDateTime handleTime;
    
    /**
     * 处理人ID
     */
    private Long handlerId;
    
    /**
     * 处理结果
     */
    private String handleResult;
    
    /**
     * 举报类型枚举
     */
    public enum ReportType {
        ARTICLE(1, "文章"),
        COMMENT(2, "评论"),
        USER(3, "用户"),
        ESSAY(4, "话题");
        
        private final int code;
        private final String desc;
        
        ReportType(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public int getCode() {
            return code;
        }
        
        public String getDesc() {
            return desc;
        }
        
        public static ReportType fromCode(int code) {
            for (ReportType type : values()) {
                if (type.getCode() == code) {
                    return type;
                }
            }
            throw new BusinessException("不支持的举报类型: " + code);
        }
    }
    
    /**
     * 举报状态枚举
     */
    public enum ReportStatus {
        PENDING(0, "待处理"),
        HANDLED(1, "已处理"),
        IGNORED(2, "已忽略");
        
        private final int code;
        private final String desc;
        
        ReportStatus(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public int getCode() {
            return code;
        }
        
        public String getDesc() {
            return desc;
        }
    }
    
    /**
     * 创建新的举报实体
     */
    public static ReportEntity create(Long reporterId, Integer targetType, Long targetId, String reason, String detail) {
        if (reporterId == null || reporterId <= 0) {
            throw new BusinessException("举报人ID不能为空");
        }
        
        if (targetType == null) {
            throw new BusinessException("举报类型不能为空");
        }
        
        if (targetId == null || targetId <= 0) {
            throw new BusinessException("被举报目标ID不能为空");
        }
        
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("举报原因不能为空");
        }
        
        return ReportEntity.builder()
                .reporterId(reporterId)
                .targetType(targetType)
                .targetId(targetId)
                .reason(reason.trim())
                .detail(detail != null ? detail.trim() : "")
                .status(ReportStatus.PENDING.getCode())
                .createTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 处理举报
     */
    public void handle(Long handlerId, String handleResult, boolean isHandled) {
        if (handlerId == null || handlerId <= 0) {
            throw new BusinessException("处理人ID不能为空");
        }
        
        if (handleResult == null || handleResult.trim().isEmpty()) {
            throw new BusinessException("处理结果不能为空");
        }
        
        this.handlerId = handlerId;
        this.handleResult = handleResult.trim();
        this.handleTime = LocalDateTime.now();
        this.status = isHandled ? ReportStatus.HANDLED.getCode() : ReportStatus.IGNORED.getCode();
    }
    
    /**
     * 是否已处理
     */
    public boolean isHandled() {
        return status != null && status.equals(ReportStatus.HANDLED.getCode());
    }
    
    /**
     * 是否被忽略
     */
    public boolean isIgnored() {
        return status != null && status.equals(ReportStatus.IGNORED.getCode());
    }
    
    /**
     * 是否待处理
     */
    public boolean isPending() {
        return status == null || status.equals(ReportStatus.PENDING.getCode());
    }
}