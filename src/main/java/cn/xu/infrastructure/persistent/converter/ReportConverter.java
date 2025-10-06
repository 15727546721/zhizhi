package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.report.model.entity.ReportEntity;
import cn.xu.infrastructure.persistent.po.Report;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 举报转换器
 * 用于在领域实体和持久化对象之间进行转换
 */
@Component
public class ReportConverter {
    
    /**
     * 将领域实体转换为持久化对象
     */
    public static Report toDataObject(ReportEntity reportEntity) {
        if (reportEntity == null) {
            return null;
        }
        
        Report report = new Report();
        report.setId(reportEntity.getId());
        report.setTargetType(reportEntity.getTargetType());
        report.setTargetId(reportEntity.getTargetId());
        report.setReporterId(reportEntity.getReporterId());
        report.setReason(reportEntity.getReason());
        report.setDetail(reportEntity.getDetail());
        report.setStatus(reportEntity.getStatus());
        report.setCreateTime(localDateTimeToDate(reportEntity.getCreateTime()));
        report.setHandleTime(localDateTimeToDate(reportEntity.getHandleTime()));
        report.setHandlerId(reportEntity.getHandlerId());
        report.setHandleResult(reportEntity.getHandleResult());
        
        return report;
    }
    
    /**
     * 将持久化对象转换为领域实体
     */
    public static ReportEntity toDomainEntity(Report report) {
        if (report == null) {
            return null;
        }
        
        return ReportEntity.builder()
                .id(report.getId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reporterId(report.getReporterId())
                .reason(report.getReason())
                .detail(report.getDetail())
                .status(report.getStatus())
                .createTime(dateToLocalDateTime(report.getCreateTime()))
                .handleTime(dateToLocalDateTime(report.getHandleTime()))
                .handlerId(report.getHandlerId())
                .handleResult(report.getHandleResult())
                .build();
    }
    
    /**
     * LocalDateTime转Date
     */
    private static Date localDateTimeToDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Timestamp.valueOf(localDateTime);
    }
    
    /**
     * Date转LocalDateTime
     */
    private static LocalDateTime dateToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return new Timestamp(date.getTime()).toLocalDateTime();
    }
}