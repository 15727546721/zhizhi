package cn.xu.domain.article.repository;

import cn.xu.domain.article.model.entity.ArticleCollectEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章收藏仓储接口
 * 遵循DDD原则，只处理文章收藏领域实体的操作
 */
public interface IArticleCollectRepository {

    /**
     * 根据用户ID和文章ID查询收藏记录
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return 文章收藏记录
     */
    ArticleCollectEntity findByUserIdAndArticleId(@Param("userId") Long userId, @Param("articleId") Long articleId);

    /**
     * 保存文章收藏记录
     *
     * @param articleCollectEntity 文章收藏实体
     * @return 收藏记录ID
     */
    Long save(ArticleCollectEntity articleCollectEntity);

    /**
     * 更新文章收藏记录
     *
     * @param articleCollectEntity 文章收藏实体
     */
    void update(ArticleCollectEntity articleCollectEntity);

    /**
     * 根据用户ID和文章ID删除收藏记录
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     */
    void deleteByUserIdAndArticleId(@Param("userId") Long userId, @Param("articleId") Long articleId);

    /**
     * 根据用户ID查询收藏的文章ID列表
     *
     * @param userId 用户ID
     * @return 收藏的文章ID列表
     */
    List<Long> findArticleIdsByUserId(Long userId);

    /**
     * 根据用户ID统计收藏的文章数量
     *
     * @param userId 用户ID
     * @return 收藏的文章数量
     */
    int countByUserId(Long userId);

    /**
     * 批量保存文章收藏记录
     *
     * @param articleCollectEntities 文章收藏实体列表
     * @return 成功保存的记录数量
     */
    int batchSave(List<ArticleCollectEntity> articleCollectEntities);

    /**
     * 批量删除文章收藏记录
     *
     * @param userId     用户ID
     * @param articleIds 文章ID列表
     * @return 成功删除的记录数量
     */
    int batchDeleteByUserIdAndArticleIds(@Param("userId") Long userId, @Param("articleIds") List<Long> articleIds);
}