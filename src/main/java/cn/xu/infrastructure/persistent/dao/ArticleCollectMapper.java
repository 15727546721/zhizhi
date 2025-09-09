package cn.xu.infrastructure.persistent.dao;


import cn.xu.infrastructure.persistent.po.ArticleCollect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    /**
     * 插入文章收藏记录
     *
     * @param articleCollect 文章收藏PO
     * @return 影响行数
     */
    int insert(ArticleCollect articleCollect);

    /**
     * 根据ID更新文章收藏记录
     *
     * @param articleCollect 文章收藏PO
     * @return 影响行数
     */
    int updateById(ArticleCollect articleCollect);

    /**
     * 根据用户ID和文章ID删除收藏记录
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return 影响行数
     */
    int deleteByUserIdAndArticleId(@Param("userId") Long userId, @Param("articleId") Long articleId);

    /**
     * 根据用户ID查询收藏的文章ID列表
     *
     * @param userId 用户ID
     * @return 收藏的文章ID列表
     */
    List<Long> findArticleIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID统计收藏的文章数量
     *
     * @param userId 用户ID
     * @return 收藏的文章数量
     */
    int countByUserId(@Param("userId") Long userId);

    /**
     * 批量插入文章收藏记录
     *
     * @param articleCollects 文章收藏PO列表
     * @return 影响行数
     */
    int batchInsert(@Param("articleCollects") List<ArticleCollect> articleCollects);

    /**
     * 批量删除文章收藏记录
     *
     * @param userId     用户ID
     * @param articleIds 文章ID列表
     * @return 影响行数
     */
    int batchDeleteByUserIdAndArticleIds(@Param("userId") Long userId, @Param("articleIds") List<Long> articleIds);
}