package cn.xu.elasticsearch.core;

/**
 * Elasticsearch 索引名称管理器
 * <p>统一管理所有索引名称</p>
 * 
 * <p>命名规范：模块_资源（如：post_index, comment_index）</p>
 */
public class IndexNameManager {

    // ==================== 索引名称常量 ====================

    /**
     * 帖子索引
     */
    public static final String POST_INDEX = "posts";

    /**
     * 评论索引
     */
    public static final String COMMENT_INDEX = "comments";

    /**
     * 用户索引（预留）
     */
    public static final String USER_INDEX = "users";

    /**
     * 标签索引（预留）
     */
    public static final String TAG_INDEX = "tags";

    // ==================== 索引别名 ====================

    /**
     * 帖子索引别名
     */
    public static final String POST_INDEX_ALIAS = "posts_alias";

    /**
     * 评论索引别名
     */
    public static final String COMMENT_INDEX_ALIAS = "comments_alias";

    // ==================== 工具方法 ====================

    /**
     * 获取带版本的索引名称
     * @param indexName 索引名称
     * @param version 版本号
     * @return 带版本的索引名称
     */
    public static String getVersionedIndexName(String indexName, String version) {
        return indexName + "_v" + version;
    }

    /**
     * 获取带日期的索引名称
     * @param indexName 索引名称
     * @param date 日期（格式：yyyyMMdd）
     * @return 带日期的索引名称
     */
    public static String getDateIndexName(String indexName, String date) {
        return indexName + "_" + date;
    }
}
