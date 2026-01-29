package cn.xu.repository;

import cn.xu.model.entity.Column;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 专栏仓储接口
 */
public interface ColumnRepository {
    
    /**
     * 保存专栏
     */
    void save(Column column);
    
    /**
     * 更新专栏
     */
    void update(Column column);
    
    /**
     * 根据ID查询
     */
    Column findById(Long id);
    
    /**
     * 根据用户ID查询所有专栏
     */
    List<Column> findByUserId(Long userId);
    
    /**
     * 根据用户ID和状态查询专栏
     */
    List<Column> findByUserIdAndStatus(Long userId, Integer status);
    
    /**
     * 删除专栏
     */
    void deleteById(Long id);
    
    /**
     * 增加文章计数
     */
    void incrementPostCount(Long id);
    
    /**
     * 减少文章计数
     */
    void decrementPostCount(Long id);
    
    /**
     * 增加订阅计数
     */
    void incrementSubscribeCount(Long id);
    
    /**
     * 减少订阅计数
     */
    void decrementSubscribeCount(Long id);
    
    /**
     * 更新最后发文时间
     */
    void updateLastPostTime(Long id, LocalDateTime time);
    
    /**
     * 统计用户专栏数量
     */
    int countByUserId(Long userId);
    
    /**
     * 分页查询已发布专栏(按最后发文时间)
     */
    List<Column> findPublishedByLastPostTime(int offset, int limit);
    
    /**
     * 分页查询已发布专栏(按订阅数)
     */
    List<Column> findPublishedBySubscribeCount(int offset, int limit);
    
    /**
     * 统计已发布专栏总数
     */
    int countPublished();
    
    /**
     * 搜索专栏
     */
    List<Column> searchByKeyword(String keyword, int offset, int limit);
    
    /**
     * 统计搜索结果数
     */
    int countSearchByKeyword(String keyword);
    
    /**
     * 查询推荐专栏
     */
    List<Column> findRecommended(int limit);
    
    /**
     * 根据文章ID查询所属专栏
     */
    List<Column> findByPostId(Long postId);
    
    /**
     * 根据ID列表批量查询专栏
     */
    List<Column> findByIds(List<Long> ids);
    
    /**
     * 根据条件查询专栏列表（管理端）
     */
    List<Column> findByConditions(Integer status, Integer isRecommended, Long userId, int offset, int limit);

    /**
     * 根据条件统计专栏数量（管理端）
     */
    int countByConditions(Integer status, Integer isRecommended, Long userId);

    /**
     * 统计所有专栏数量
     */
    int countAll();

    /**
     * 根据状态统计专栏数量
     */
    int countByStatus(Integer status);

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
    void updateStatus(Long id, Integer status);

    /**
     * 更新推荐状态
     */
    void updateRecommended(Long id, Integer isRecommended);
}
