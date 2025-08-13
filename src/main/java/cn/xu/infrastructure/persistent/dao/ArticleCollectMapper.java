package cn.xu.infrastructure.persistent.dao;


import cn.xu.infrastructure.persistent.po.ArticleCollect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleCollectMapper {
    /**
     * 根据用户ID和文章ID查询收藏记录
     *
     * @param userId
     * @param articleId
     * @return 文章收藏记录
     */
    ArticleCollect findByUserIdAndArticleId(@Param("userId") Long userId, @Param("articleId") Long articleId);
}
