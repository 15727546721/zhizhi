package cn.xu.domain.report.service;

import cn.xu.common.exception.BusinessException;
import cn.xu.domain.report.model.entity.ReportEntity;
import cn.xu.domain.report.repository.IReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 举报领域服务
 * 处理举报相关的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportDomainService {
    
    private final IReportRepository reportRepository;
    
    /**
     * 创建举报
     */
    public ReportEntity createReport(Long reporterId, Integer targetType, Long targetId, String reason, String detail) {
        try {
            // 创建举报实体
            ReportEntity report = ReportEntity.create(reporterId, targetType, targetId, reason, detail);
            
            // 保存举报
            ReportEntity savedReport = reportRepository.save(report);
            
            log.info("用户 {} 举报了类型为 {} 的目标 {}，举报ID: {}", 
                    reporterId, targetType, targetId, savedReport.getId());
            
            return savedReport;
        } catch (Exception e) {
            log.error("创建举报失败，reporterId: {}, targetType: {}, targetId: {}", 
                    reporterId, targetType, targetId, e);
            throw new BusinessException("举报失败，请稍后重试");
        }
    }
    
    /**
     * 处理举报
     */
    public ReportEntity handleReport(Long reportId, Long handlerId, String handleResult, boolean isHandled) {
        try {
            // 查找举报
            Optional<ReportEntity> reportOpt = reportRepository.findById(reportId);
            if (!reportOpt.isPresent()) {
                throw new BusinessException("举报信息不存在");
            }
            
            ReportEntity report = reportOpt.get();
            
            // 检查举报状态
            if (!report.isPending()) {
                throw new BusinessException("该举报已被处理");
            }
            
            // 处理举报
            report.handle(handlerId, handleResult, isHandled);
            
            // 保存处理结果
            ReportEntity updatedReport = reportRepository.save(report);
            
            log.info("管理员 {} 处理了举报 {}，处理结果: {}", 
                    handlerId, reportId, isHandled ? "已处理" : "已忽略");
            
            return updatedReport;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("处理举报失败，reportId: {}, handlerId: {}", reportId, handlerId, e);
            throw new BusinessException("处理举报失败，请稍后重试");
        }
    }
    
    /**
     * 根据ID获取举报详情
     */
    public Optional<ReportEntity> getReportById(Long reportId) {
        if (reportId == null || reportId <= 0) {
            throw new BusinessException("举报ID不能为空");
        }
        
        return reportRepository.findById(reportId);
    }
    
    /**
     * 获取用户的所有举报
     */
    public List<ReportEntity> getReportsByReporterId(Long reporterId) {
        if (reporterId == null || reporterId <= 0) {
            throw new BusinessException("用户ID不能为空");
        }
        
        return reportRepository.findByReporterId(reporterId);
    }
    
    /**
     * 分页获取举报列表
     */
    public List<ReportEntity> getReportsByPage(Integer page, Integer size) {
        if (page == null || page < 0) {
            page = 0;
        }
        
        if (size == null || size <= 0) {
            size = 10;
        }
        
        return reportRepository.findByPage(page, size);
    }
    
    /**
     * 根据状态获取举报列表
     */
    public List<ReportEntity> getReportsByStatus(Integer status) {
        if (status == null) {
            throw new BusinessException("状态不能为空");
        }
        
        return reportRepository.findByStatus(status);
    }
    
    /**
     * 获取未处理举报数量
     */
    public long getPendingReportCount() {
        return reportRepository.countPendingReports();
    }
}