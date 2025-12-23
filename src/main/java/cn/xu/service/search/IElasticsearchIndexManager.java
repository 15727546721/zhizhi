package cn.xu.service.search;

import cn.xu.model.entity.Post;

/**
 * Elasticsearch 索引管理接口
 * 提供对文章索引的操作，包括文章的索引、更新、删除和查询等功能

 */
public interface IElasticsearchIndexManager {

    /**
     * 索引某篇文章
     *
     * @param post 文章对象
     */
    void indexPost(Post post);

    /**
     * 索引文章，支持重试机制
     *
     * @param post 文章对象
     * @return 是否索引成功
     */
    boolean indexPostWithRetry(Post post);

    /**
     * 更新已索引的文章
     * 如果文章已经存在，进行更新索引操作
     *
     * @param post 文章对象
     */
    void updateIndexedPost(Post post);

    /**
     * 删除文章索引
     *
     * @param postId 文章ID
     */
    void removeIndexedPost(Long postId);

    /**
     * 获取索引的文章数量
     *
     * @return 索引中文章的数量
     */
    long count();

    /**
     * 判断Elasticsearch服务是否可用
     *
     * @return 是否可用
     */
    boolean isAvailable();
}
