package cn.xu.domain.article.service;

import cn.xu.domain.article.model.entity.ArticleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 文章查询策略接口
 * 定义文章查询的不同实现方式
 */
public interface ArticleQueryStrategy {

    /**
     * 根据标题搜索文章
     * @param title 标题关键词
     * @param pageable 分页参数
     * @return 文章分页结果
     */
    Page<ArticleEntity> searchByTitle(String title, Pageable pageable);

    /**
     * 获取热门文章排行
     * @param rankType 排行类型（日榜、周榜、月榜）
     * @param pageable 分页参数
     * @return 文章分页结果
     */
    Page<ArticleEntity> getHotRank(String rankType, Pageable pageable);

    /**
     * 根据分类ID获取文章列表
     * @param categoryId 分类ID
     * @param pageable 分页参数
     * @return 文章分页结果
     */
    Page<ArticleEntity> getArticlesByCategory(Long categoryId, Pageable pageable);

    /**
     * 根据用户ID获取文章列表
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 文章分页结果
     */
    Page<ArticleEntity> getArticlesByUser(Long userId, Pageable pageable);

    /**
     * 获取文章详情
     * @param articleId 文章ID
     * @return 文章实体
     */
    ArticleEntity getArticleDetail(Long articleId);
}