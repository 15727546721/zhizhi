package cn.xu.repository.mapper;

import cn.xu.model.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 举报Mapper
 */
@Mapper
public interface ReportMapper {

    /**
     * 插入举报记录
     */
    int insert(Report report);

    /**
     * 根据ID查询举报
     */
    Report selectById(@Param("id") Long id);

    /**
     * 更新举报记录
     */
    int updateById(Report report);

    /**
     * 分页查询举报列表
     */
    List<Report> selectReportList(
            @Param("status") Integer status,
            @Param("targetType") Integer targetType,
            @Param("reason") Integer reason,
            @Param("reporterId") Long reporterId,
            @Param("targetUserId") Long targetUserId,
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 统计举报数量
     */
    long countReportList(
            @Param("status") Integer status,
            @Param("targetType") Integer targetType,
            @Param("reason") Integer reason,
            @Param("reporterId") Long reporterId,
            @Param("targetUserId") Long targetUserId,
            @Param("keyword") String keyword
    );

    /**
     * 查询用户的举报记录
     */
    List<Report> selectByReporterId(@Param("reporterId") Long reporterId,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit);

    /**
     * 统计用户的举报数量
     */
    long countByReporterId(@Param("reporterId") Long reporterId);

    /**
     * 检查是否已举报
     */
    int checkExists(@Param("reporterId") Long reporterId,
                    @Param("targetType") Integer targetType,
                    @Param("targetId") Long targetId);

    /**
     * 统计各状态举报数量
     */
    List<java.util.Map<String, Object>> countByStatus();

    /**
     * 批量更新状态为忽略
     */
    int batchIgnore(@Param("ids") List<Long> ids, @Param("handlerId") Long handlerId);
}
