package cn.xu.domain.article.repository;

import cn.xu.api.system.model.dto.article.ArticleRequest;
import cn.xu.api.web.model.vo.article.ArticleListVO;
import cn.xu.api.web.model.vo.article.ArticlePageVO;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.valobj.ArticleStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 文章仓储接口
 * 遵循DDD原则，通过聚合根访问实体
 * 该接口用于处理文章实体级别的操作
 */
public interface IArticleRepository {
    
    /**
     * 保存文章实体
     * @param articleEntity 文章实体
     * @return 文章ID
     */
    Long save(ArticleEntity articleEntity);

    /**
     * 根据ID查找文章
     * @param id 文章ID
     * @return 文章实体
     */
    Optional<ArticleEntity> findById(Long id);

    /**
     * 更新文章实体
     * @param articleEntity 文章实体
     */
    void update(ArticleEntity articleEntity);

    /**
     * 根据ID删除文章
     * @param id 文章ID
     */
    void deleteById(Long id);

    /**
     * 批量删除文章
     * @param articleIds 文章ID列表
     */
    void deleteByIds(List<Long> articleIds);

    /**
     * 分页查询文章
     * @param page 页码
     * @param size 页面大小
     * @return 文章列表
     */
    List<ArticleEntity> queryArticleByPage(int page, int size);

    /**
     * 分页查询文章（返回VO）
     * @param articleRequest 查询请求
     * @return 文章页面VO列表
     */
    List<ArticlePageVO> queryArticle(ArticleRequest articleRequest);

    /**
     * 根据分类ID查询文章（返回VO）
     * @param categoryId 分类ID
     * @return 文章列表VO
     */
    List<ArticleListVO> queryArticleByCategoryId(Long categoryId);

    /**
     * 根据用户ID查询文章（返回VO）
     * @param userId 用户ID
     * @return 文章列表VO
     */
    List<ArticleListVO> queryArticleByUserId(Long userId);

    /**
     * 获取所有已发布文章
     * @return 已发布文章列表
     */
    List<ArticleEntity> findAllPublishedArticles();

    /**
     * 获取所有文章
     * @return 所有文章列表
     */
    List<ArticleEntity> findAll();

    /**
     * 更新文章点赞数
     * @param articleId 文章ID
     * @param likeCount 点赞数
     */
    void updateArticleLikeCount(Long articleId, Long likeCount);

    /**
     * 批量更新文章点赞数
     * @param likeCounts key为文章ID，value为点赞数的Map
     */
    void batchUpdateArticleLikeCount(Map<Long, Long> likeCounts);

    /**
     * 更新文章状态
     * @param status 状态值
     * @param id 文章ID
     */
    void updateArticleStatus(Integer status, Long id);

    /**
     * 根据用户ID查询草稿文章列表（返回VO）
     * @param userId 用户ID
     * @return 草稿文章列表VO
     */
    List<ArticleListVO> queryDraftArticleListByUserId(Long userId);

    /**
     * 根据分类ID分页查询文章列表
     * @param categoryId 分类ID
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 文章列表
     */
    List<ArticleEntity> getArticlePageListByCategoryId(Long categoryId, Integer pageNo, Integer pageSize);

    /**
     * 分页查询文章列表
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 文章列表
     */
    List<ArticleEntity> getArticlePageList(Integer pageNo, Integer pageSize);
    
    /**
     * 分页查询文章列表（支持排序）
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @param sortBy 排序方式
     * @return 文章列表
     */
    List<ArticleEntity> getArticlePageListWithSort(Integer pageNo, Integer pageSize, String sortBy);
}
