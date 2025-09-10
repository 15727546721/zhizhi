package cn.xu.domain.report.repository;

import cn.xu.domain.report.model.entity.ReportEntity;

import java.util.List;
import java.util.Optional;

/**
 * 举报仓储接口
 * 定义举报相关的数据访问操作
 */
public interface IReportRepository {
    
    /**
     * 保存举报信息
     */
    ReportEntity save(ReportEntity report);
    
    /**
     * 根据ID查找举报
     */
    Optional<ReportEntity> findById(Long id);
    
    /**
     * 根据目标类型和目标ID查找举报列表
     */
    List<ReportEntity> findByTarget(Integer targetType, Long targetId);
    
    /**
     * 查找用户的所有举报
     */
    List<ReportEntity> findByReporterId(Long reporterId);
    
    /**
     * 分页查找举报列表
     */
    List<ReportEntity> findByPage(Integer page, Integer size);
    
    /**
     * 根据状态查找举报列表
     */
    List<ReportEntity> findByStatus(Integer status);
    
    /**
     * 统计未处理举报数量
     */
    long countPendingReports();
    
    /**
     * 删除举报
     */
    void deleteById(Long id);
}