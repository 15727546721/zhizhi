package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.CollectFolderArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CollectFolderArticleMapper {
    /**
     * 插入收藏夹文章关联记录
     *
     * @param collectFolderArticle 收藏夹文章关联对象
     * @return 插入记录数
     */
    int insert(CollectFolderArticle collectFolderArticle);

    /**
     * 删除收藏夹文章关联记录
     *
     * @param folderId  收藏夹ID
     * @param articleId 文章ID
     * @return 删除记录数
     */
    int deleteByFolderIdAndArticleId(@Param("folderId") Long folderId, @Param("articleId") Long articleId);

    /**
     * 根据收藏夹ID删除关联记录
     *
     * @param folderId 收藏夹ID
     * @return 删除记录数
     */
    int deleteByFolderId(@Param("folderId") Long folderId);

    /**
     * 根据文章ID删除关联记录
     *
     * @param articleId 文章ID
     * @return 删除记录数
     */
    int deleteByArticleId(@Param("articleId") Long articleId);

    /**
     * 根据收藏夹ID查询关联记录列表
     *
     * @param folderId 收藏夹ID
     * @return 关联记录列表
     */
    List<CollectFolderArticle> selectByFolderId(@Param("folderId") Long folderId);

    /**
     * 根据用户ID和文章ID查询关联记录
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return 关联记录列表
     */
    List<CollectFolderArticle> selectByUserIdAndArticleId(@Param("userId") Long userId, @Param("articleId") Long articleId);

    /**
     * 根据收藏夹ID和文章ID查询关联记录
     *
     * @param folderId  收藏夹ID
     * @param articleId 文章ID
     * @return 关联记录对象
     */
    CollectFolderArticle selectByFolderIdAndArticleId(@Param("folderId") Long folderId, @Param("articleId") Long articleId);

    /**
     * 检查文章是否已被收藏到指定收藏夹
     *
     * @param folderId  收藏夹ID
     * @param articleId 文章ID
     * @return 是否已收藏
     */
    boolean existsByFolderIdAndArticleId(@Param("folderId") Long folderId, @Param("articleId") Long articleId);

    /**
     * 统计收藏夹中的文章数量
     *
     * @param folderId 收藏夹ID
     * @return 文章数量
     */
    int countByFolderId(@Param("folderId") Long folderId);
}