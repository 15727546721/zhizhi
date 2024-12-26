package cn.xu.infrastructure.persistent.dao;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ILikeDao {
    /**
     * 插入文章点赞记录
     * @param articleId 文章id
     * @param userId 用户id
     * @param value 点赞值(1点赞，0取消点赞)
     * @param type 点赞类型-1文章
     */
    void insertArticleLikeRecord(Long articleId, Long userId, int value, int type);
}
