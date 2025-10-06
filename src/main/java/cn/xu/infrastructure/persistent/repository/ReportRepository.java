package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.report.model.entity.ReportEntity;
import cn.xu.domain.report.repository.IReportRepository;
import cn.xu.infrastructure.persistent.converter.ReportConverter;
import cn.xu.infrastructure.persistent.dao.IReportDao;
import cn.xu.infrastructure.persistent.po.Report;
import cn.xu.infrastructure.persistent.po.ReportReasonConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 举报仓储实现类
 */
@Repository
@RequiredArgsConstructor
public class ReportRepository implements IReportRepository {
    
    private final IReportDao reportDao;
    private final ReportConverter reportConverter;
    
    @Override
    public ReportEntity save(ReportEntity report) {
        Report reportPO = reportConverter.toDataObject(report);
        if (reportPO.getId() == null) {
            reportDao.insert(reportPO);
            report.setId(reportPO.getId());
        } else {
            reportDao.update(reportPO);
        }
        return report;
    }
    
    @Override
    public Optional<ReportEntity> findById(Long id) {
        Report reportPO = reportDao.selectById(id);
        return Optional.ofNullable(reportConverter.toDomainEntity(reportPO));
    }
    
    @Override
    public List<ReportEntity> findByTarget(Integer targetType, Long targetId) {
        List<Report> reportPOs = reportDao.selectByTarget(targetType, targetId);
        return reportPOs.stream()
                .map(report -> reportConverter.toDomainEntity(report))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ReportEntity> findByReporterId(Long reporterId) {
        List<Report> reportPOs = reportDao.selectByReporterId(reporterId);
        return reportPOs.stream()
                .map(report -> reportConverter.toDomainEntity(report))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ReportEntity> findByPage(Integer page, Integer size) {
        int offset = page * size;
        List<Report> reportPOs = reportDao.selectByPage(offset, size);
        return reportPOs.stream()
                .map(report -> reportConverter.toDomainEntity(report))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ReportEntity> findByStatus(Integer status) {
        List<Report> reportPOs = reportDao.selectByStatus(status);
        return reportPOs.stream()
                .map(report -> reportConverter.toDomainEntity(report))
                .collect(Collectors.toList());
    }
    
    @Override
    public long countPendingReports() {
        return reportDao.countPendingReports();
    }
    
    @Override
    public void deleteById(Long id) {
        reportDao.deleteById(id);
    }
    
    /**
     * 获取所有启用的举报原因
     */
    public List<ReportReasonConfig> findAllEnabledReasons() {
        return reportDao.selectAllEnabledReasons();
    }
    
    /**
     * 根据目标类型获取举报原因
     */
    public List<ReportReasonConfig> findReasonsByTargetType(Integer targetType) {
        return reportDao.selectReasonsByTargetType(targetType);
    }
}