package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.Report;
import cn.xu.infrastructure.persistent.po.ReportReasonConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 举报数据访问接口
 */
@Mapper
public interface IReportDao {
    
    /**
     * 插入举报记录
     */
    void insert(Report report);
    
    /**
     * 更新举报记录
     */
    void update(Report report);
    
    /**
     * 根据ID查询举报
     */
    Report selectById(Long id);
    
    /**
     * 根据目标类型和目标ID查询举报
     */
    List<Report> selectByTarget(@Param("targetType") Integer targetType, @Param("targetId") Long targetId);
    
    /**
     * 根据举报人ID查询举报
     */
    List<Report> selectByReporterId(Long reporterId);
    
    /**
     * 分页查询举报
     */
    List<Report> selectByPage(@Param("offset") Integer offset, @Param("limit") Integer limit);
    
    /**
     * 根据状态查询举报
     */
    List<Report> selectByStatus(Integer status);
    
    /**
     * 统计未处理举报数量
     */
    long countPendingReports();
    
    /**
     * 删除举报
     */
    void deleteById(Long id);
    
    /**
     * 查询所有启用的举报原因
     */
    List<ReportReasonConfig> selectAllEnabledReasons();
    
    /**
     * 根据目标类型查询举报原因
     */
    List<ReportReasonConfig> selectReasonsByTargetType(Integer targetType);
}