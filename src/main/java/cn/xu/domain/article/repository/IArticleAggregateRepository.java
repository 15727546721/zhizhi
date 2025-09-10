package cn.xu.domain.article.repository;

import cn.xu.domain.article.model.aggregate.ArticleAggregate;

import java.util.List;
import java.util.Optional;

/**
 * 文章聚合根仓储接口
 * 遵循DDD原则，只处理聚合根的操作
 */
public interface IArticleAggregateRepository {
    
    /**
     * 保存文章聚合根
     * @param aggregate 文章聚合根
     * @return 聚合根ID
     */
    Long save(ArticleAggregate aggregate);
    
    /**
     * 更新文章聚合根
     * @param aggregate 文章聚合根
     */
    void update(ArticleAggregate aggregate);
    
    /**
     * 根据ID查找文章聚合根
     * @param id 文章ID
     * @return 文章聚合根
     */
    Optional<ArticleAggregate> findById(Long id);
    
    /**
     * 根据用户ID查找文章聚合根列表
     * @param userId 用户ID
     * @return 文章聚合根列表
     */
    List<ArticleAggregate> findByUserId(Long userId);
    
    /**
     * 根据分类查找已发布的文章聚合根列表（分页）
     * @param categoryId 分类ID
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 文章聚合根列表
     */
    List<ArticleAggregate> findPublishedByCategoryId(Long categoryId, Integer pageNo, Integer pageSize);
    
    /**
     * 查找已发布的文章聚合根列表（分页）
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 文章聚合根列表
     */
    List<ArticleAggregate> findPublishedArticles(Integer pageNo, Integer pageSize);
    
    /**
     * 查找所有已发布的文章（用于热度计算）
     * @return 已发布文章聚合根列表
     */
    List<ArticleAggregate> findAllPublished();
    
    /**
     * 删除文章聚合根
     * @param id 文章ID
     */
    void deleteById(Long id);
    
    /**
     * 批量删除文章聚合根
     * @param ids 文章ID列表
     */
    void deleteByIds(List<Long> ids);
    
    /**
     * 检查文章是否存在
     * @param id 文章ID
     * @return 是否存在
     */
    boolean existsById(Long id);
    
    /**
     * 根据用户ID统计已发布文章数量
     * @param userId 用户ID
     * @return 文章数量
     */
    Long countPublishedByUserId(Long userId);
}