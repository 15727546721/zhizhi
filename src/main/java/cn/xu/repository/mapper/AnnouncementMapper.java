package cn.xu.repository.mapper;

import cn.xu.model.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公告Mapper
 */
@Mapper
public interface AnnouncementMapper {

    /**
     * 插入公告
     */
    int insert(Announcement announcement);

    /**
     * 根据ID查询
     */
    Announcement selectById(@Param("id") Long id);

    /**
     * 更新公告
     */
    int updateById(Announcement announcement);

    /**
     * 删除公告
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除公告
     */
    int deleteBatchIds(@Param("ids") List<Long> ids);

    /**
     * 获取已发布的公告列表（用户端）
     */
    List<Announcement> selectPublishedList(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计已发布的公告数量
     */
    long countPublished();

    /**
     * 获取公告列表（管理端，支持筛选）
     */
    List<Announcement> selectListWithFilters(@Param("type") Integer type, 
                                              @Param("status") Integer status,
                                              @Param("keyword") String keyword,
                                              @Param("offset") int offset, 
                                              @Param("limit") int limit);

    /**
     * 统计公告数量（管理端）
     */
    long countWithFilters(@Param("type") Integer type, 
                          @Param("status") Integer status,
                          @Param("keyword") String keyword);
}
