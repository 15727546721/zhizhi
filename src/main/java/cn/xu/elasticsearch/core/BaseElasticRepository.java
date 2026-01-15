package cn.xu.elasticsearch.core;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;

/**
 * Elasticsearch Repository 基类
 * <p>提供通用的 ES 操作方法，减少重复代码</p>
 * 
 * <p>所有 ElasticRepository 应继承此类，通过 ElasticsearchOperations 访问 ES</p>
 */
@Slf4j
public abstract class BaseElasticRepository {

    @Autowired
    protected ElasticsearchOperations esOps;

    // ==================== 索引操作 ====================

    /**
     * 保存文档
     */
    protected <T> T save(T entity) {
        return esOps.save(entity);
    }

    /**
     * 批量保存文档
     */
    protected <T> Iterable<T> saveAll(Iterable<T> entities) {
        return esOps.saveAll(entities);
    }

    /**
     * 删除文档
     */
    protected <T> String delete(String id, Class<T> clazz) {
        return esOps.delete(id, clazz);
    }

    /**
     * 删除实体
     */
    protected <T> String delete(T entity) {
        return esOps.delete(entity);
    }

    // ==================== 查询操作 ====================

    /**
     * 根据 ID 查询
     */
    protected <T> T get(String id, Class<T> clazz) {
        return esOps.get(id, clazz);
    }

    /**
     * 执行搜索
     */
    protected <T> SearchHits<T> search(Query query, Class<T> clazz) {
        return esOps.search(query, clazz);
    }

    /**
     * 执行搜索（带分页）
     */
    protected <T> SearchHits<T> search(Query query, Pageable pageable, Class<T> clazz) {
        return esOps.search(query, pageable, clazz);
    }

    /**
     * 执行搜索（带分页和排序）
     */
    protected <T> SearchHits<T> search(Query query, Pageable pageable, String sortField, SortOrder sortOrder, Class<T> clazz) {
        return esOps.search(query, pageable, sortField, sortOrder, clazz);
    }

    /**
     * 搜索并转换为 Page
     */
    protected <T> Page<T> searchPage(Query query, Pageable pageable, Class<T> clazz) {
        return esOps.searchPage(query, pageable, clazz);
    }

    /**
     * 搜索并转换为 Page（带排序）
     */
    protected <T> Page<T> searchPage(Query query, Pageable pageable, String sortField, SortOrder sortOrder, Class<T> clazz) {
        return esOps.searchPage(query, pageable, sortField, sortOrder, clazz);
    }

    // ==================== 统计操作 ====================

    /**
     * 统计文档数量
     */
    protected <T> long count(Query query, Class<T> clazz) {
        return esOps.count(query, clazz);
    }

    /**
     * 判断文档是否存在
     */
    protected <T> boolean exists(String id, Class<T> clazz) {
        return esOps.exists(id, clazz);
    }

    // ==================== 索引管理 ====================

    /**
     * 创建索引
     */
    protected <T> boolean createIndex(Class<T> clazz) {
        return esOps.createIndex(clazz);
    }

    /**
     * 删除索引
     */
    protected <T> boolean deleteIndex(Class<T> clazz) {
        return esOps.deleteIndex(clazz);
    }

    /**
     * 判断索引是否存在
     */
    protected <T> boolean indexExists(Class<T> clazz) {
        return esOps.indexExists(clazz);
    }

    /**
     * 刷新索引
     */
    protected <T> void refreshIndex(Class<T> clazz) {
        esOps.refreshIndex(clazz);
    }

    /**
     * 获取底层 ElasticsearchOperations（供子类特殊场景使用）
     */
    protected ElasticsearchOperations getEsOps() {
        return esOps;
    }
}
