package cn.xu.domain.like.repository;

import cn.xu.domain.article.event.ArticleEvent;

/**
 * 点赞功能的Repository接口
 */
public interface ILikeRepository {


    /**
     * 插入文章点赞记录
     *
     * @param event 文章事件
     */
    void insertArticleLikeRecord(ArticleEvent event);
}
