package cn.xu.repository.mapper;

import cn.xu.model.entity.Column;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 专栏Mapper接口
 */
@Mapper
public interface ColumnMapper {
    
    /**
     * 插入专栏
     */
    void insert(Column column);
    
    /**
     * 更新专栏
     */
    void update(Column column);
    
    /**
     * 根据ID查询
     */
    Column selectById(@Param("id") Long id);
    
    /**
     * 根据用户ID查询所有专栏
     */
    List<Column> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID和状态查询专栏
     */
    List<Column> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);
    
    /**
     * 删除专栏
     */
    void deleteById(@Param("id") Long id);
    
    /**
     * 增加文章计数
     */
    void incrementPostCount(@Param("id") Long id);
    
    /**
     * 减少文章计数
     */
    void decrementPostCount(@Param("id") Long id);
    
    /**
     * 增加订阅计数
     */
    void incrementSubscribeCount(@Param("id") Long id);
    
    /**
     * 减少订阅计数
     */
    void decrementSubscribeCount(@Param("id") Long id);
    
    /**
     * 更新最后发文时间
     */
    void updateLastPostTime(@Param("id") Long id, @Param("time") LocalDateTime time);
    
    /**
     * 统计用户专栏数量
     */
    int countByUserId(@Param("userId") Long userId);
    
    /**
     * 分页查询已发布专栏(按最后发文时间)
     */
    List<Column> selectPublishedByLastPostTime(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 分页查询已发布专栏(按订阅数)
     */
    List<Column> selectPublishedBySubscribeCount(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 统计已发布专栏总数
     */
    int countPublished();
    
    /**
     * 搜索专栏
     */
    List<Column> searchByKeyword(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 统计搜索结果数
     */
    int countSearchByKeyword(@Param("keyword") String keyword);
    
    /**
     * 查询推荐专栏
     */
    List<Column> selectRecommended(@Param("limit") int limit);
    
    /**
     * 根据文章ID查询所属专栏
     */
    List<Column> selectByPostId(@Param("postId") Long postId);
    
    /**
     * 根据ID列表批量查询专栏
     */
    List<Column> selectByIds(@Param("ids") List<Long> ids);
    
    /**
     * 根据条件查询专栏列表（管理端）
     */
    List<Column> findByConditions(@Param("status") Integer status,
                                   @Param("isRecommended") Integer isRecommended,
                                   @Param("userId") Long userId,
                                   @Param("offset") int offset,
                                   @Param("limit") int limit);

    /**
     * 根据条件统计专栏数量（管理端）
     */
    int countByConditions(@Param("status") Integer status,
                          @Param("isRecommended") Integer isRecommended,
                          @Param("userId") Long userId);

    /**
     * 统计所有专栏数量
     */
    int countAll();

    /**
     * 根据状态统计专栏数量
     */
    int countByStatus(@Param("status") Integer status);

    /**
     * 统计推荐专栏数量
     */
    int countRecommended();

    /**
     * 统计总订阅数
     */
    long sumSubscribeCount();

    /**
     * 更新专栏状态
     */
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新推荐状态
     */
    void updateRecommended(@Param("id") Long id, @Param("isRecommended") Integer isRecommended);
}
