package cn.xu.elasticsearch.service;

import cn.xu.elasticsearch.core.ElasticsearchOperations;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Elasticsearch 统一服务
 * <p>提供索引管理和搜索的统一入口</p>
 * 
 * <p>功能：</p>
 * <ul>
 *   <li>索引文档的增删改查</li>
 *   <li>搜索查询</li>
 *   <li>索引管理</li>
 *   <li>健康检查</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class ElasticsearchService {

    private final ElasticsearchOperations esOps;

    // ==================== 健康检查 ====================

    /**
     * 检查 ES 是否可用
     */
    public boolean isAvailable() {
        return esOps.isAvailable();
    }

    // ==================== 索引操作 ====================

    /**
     * 保存文档
     */
    public <T> T save(T entity) {
        return esOps.save(entity);
    }

    /**
     * 批量保存文档
     */
    public <T> Iterable<T> saveAll(Iterable<T> entities) {
        return esOps.saveAll(entities);
    }

    /**
     * 删除文档
     */
    public <T> String delete(String id, Class<T> clazz) {
        return esOps.delete(id, clazz);
    }

    /**
     * 删除实体
     */
    public <T> String delete(T entity) {
        return esOps.delete(entity);
    }

    // ==================== 查询操作 ====================

    /**
     * 根据 ID 查询
     */
    public <T> T get(String id, Class<T> clazz) {
        return esOps.get(id, clazz);
    }

    /**
     * 搜索并转换为 Page
     */
    public <T> Page<T> search(Query query, Pageable pageable, Class<T> clazz) {
        return esOps.searchPage(query, pageable, clazz);
    }

    /**
     * 搜索并转换为 Page（带排序）
     */
    public <T> Page<T> search(Query query, Pageable pageable, String sortField, SortOrder sortOrder, Class<T> clazz) {
        return esOps.searchPage(query, pageable, sortField, sortOrder, clazz);
    }

    // ==================== 统计操作 ====================

    /**
     * 统计文档数量
     */
    public <T> long count(Query query, Class<T> clazz) {
        return esOps.count(query, clazz);
    }

    /**
     * 判断文档是否存在
     */
    public <T> boolean exists(String id, Class<T> clazz) {
        return esOps.exists(id, clazz);
    }

    // ==================== 索引管理 ====================

    /**
     * 创建索引
     */
    public <T> boolean createIndex(Class<T> clazz) {
        return esOps.createIndex(clazz);
    }

    /**
     * 删除索引
     */
    public <T> boolean deleteIndex(Class<T> clazz) {
        return esOps.deleteIndex(clazz);
    }

    /**
     * 判断索引是否存在
     */
    public <T> boolean indexExists(Class<T> clazz) {
        return esOps.indexExists(clazz);
    }

    /**
     * 刷新索引
     */
    public <T> void refreshIndex(Class<T> clazz) {
        esOps.refreshIndex(clazz);
    }
}
