package cn.xu.repository.mapper;

import cn.xu.model.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户反馈 Mapper
 *
 * @author xu
 * @since 2024-12-09
 */
@Mapper
public interface FeedbackMapper {

    /**
     * 插入反馈
     */
    int insert(Feedback feedback);

    /**
     * 根据ID查询
     */
    Feedback selectById(@Param("id") Long id);

    /**
     * 更新反馈
     */
    int updateById(Feedback feedback);

    /**
     * 批量删除
     */
    int deleteBatchIds(@Param("ids") List<Long> ids);

    /**
     * 分页查询反馈列表（管理端）
     */
    List<Feedback> selectFeedbackList(
            @Param("type") Integer type,
            @Param("status") Integer status,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 统计反馈数量（管理端）
     */
    long countFeedback(
            @Param("type") Integer type,
            @Param("status") Integer status
    );

    /**
     * 查询用户的反馈列表
     */
    List<Feedback> selectByUserId(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 统计用户的反馈数量
     */
    long countByUserId(@Param("userId") Long userId);

    /**
     * 按状态统计反馈数量
     */
    Long countByStatus(@Param("status") Integer status);
}
