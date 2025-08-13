package cn.xu.domain.article.repository;

import cn.xu.domain.article.model.entity.ArticleCollectEntity;
import org.apache.ibatis.annotations.Param;

public interface IArticleCollectRepository {

    /**
     * 根据用户ID和文章ID查询收藏记录
     *
     * @param userId
     * @param articleId
     * @return 文章收藏记录
     */
    ArticleCollectEntity findByUserIdAndArticleId(@Param("userId") Long userId, @Param("articleId") Long articleId);
}
