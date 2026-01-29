package cn.xu.elasticsearch.core;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Elasticsearch 操作封装类
 * <p>统一所有 ES 访问操作，提供降级和异常处理</p>
 * 
 * <p>设计目标：</p>
 * <ul>
 *   <li>统一 ES 访问入口</li>
 *   <li>统一异常处理和日志</li>
 *   <li>提供降级机制</li>
 *   <li>简化业务代码</li>
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
public class ElasticsearchOperations {

    private final org.springframework.data.elasticsearch.core.ElasticsearchOperations elasticsearchTemplate;

    // ==================== 健康检查 ====================

    /**
     * 检查 ES 是否可用
     */
    public boolean isAvailable() {
        try {
            // 简单的 ping 操作
            elasticsearchTemplate.indexOps(org.springframework.data.elasticsearch.core.IndexOperations.class);
            return true;
        } catch (Exception e) {
            log.warn("Elasticsearch 不可用: {}", e.getMessage());
            return false;
        }
    }

    // ==================== 索引操作 ====================

    /**
     * 保存文档
     */
    public <T> T save(T entity) {
        try {
            return elasticsearchTemplate.save(entity);
        } catch (Exception e) {
            log.error("保存文档失败: entity={}", entity.getClass().getSimpleName(), e);
            throw new ElasticsearchException("保存文档失败", e);
        }
    }

    /**
     * 批量保存文档
     */
    public <T> Iterable<T> saveAll(Iterable<T> entities) {
        try {
            return elasticsearchTemplate.save(entities);
        } catch (Exception e) {
            log.error("批量保存文档失败", e);
            throw new ElasticsearchException("批量保存文档失败", e);
        }
    }

    /**
     * 根据 ID 删除文档
     */
    public <T> String delete(String id, Class<T> clazz) {
        try {
            return elasticsearchTemplate.delete(id, clazz);
        } catch (Exception e) {
            log.error("删除文档失败: id={}, class={}", id, clazz.getSimpleName(), e);
            throw new ElasticsearchException("删除文档失败", e);
        }
    }

    /**
     * 删除实体
     */
    public <T> String delete(T entity) {
        try {
            return elasticsearchTemplate.delete(entity);
        } catch (Exception e) {
            log.error("删除实体失败: entity={}", entity.getClass().getSimpleName(), e);
            throw new ElasticsearchException("删除实体失败", e);
        }
    }

    // ==================== 查询操作 ====================

    /**
     * 根据 ID 查询
     */
    public <T> T get(String id, Class<T> clazz) {
        try {
            return elasticsearchTemplate.get(id, clazz);
        } catch (Exception e) {
            log.error("查询文档失败: id={}, class={}", id, clazz.getSimpleName(), e);
            return null;
        }
    }

    /**
     * 执行搜索查询
     */
    public <T> SearchHits<T> search(Query query, Class<T> clazz) {
        try {
            NativeQuery searchQuery = NativeQuery.builder()
                    .withQuery(query)
                    .build();
            return elasticsearchTemplate.search(searchQuery, clazz);
        } catch (Exception e) {
            log.error("搜索失败: class={}", clazz.getSimpleName(), e);
            throw new ElasticsearchException("搜索失败", e);
        }
    }

    /**
     * 执行搜索查询（带分页）
     */
    public <T> SearchHits<T> search(Query query, Pageable pageable, Class<T> clazz) {
        try {
            NativeQuery searchQuery = NativeQuery.builder()
                    .withQuery(query)
                    .withPageable(pageable)
                    .build();
            return elasticsearchTemplate.search(searchQuery, clazz);
        } catch (Exception e) {
            log.error("搜索失败: class={}, page={}", clazz.getSimpleName(), pageable.getPageNumber(), e);
            throw new ElasticsearchException("搜索失败", e);
        }
    }

    /**
     * 执行搜索查询（带分页和排序）
     */
    public <T> SearchHits<T> search(Query query, Pageable pageable, String sortField, SortOrder sortOrder, Class<T> clazz) {
        try {
            NativeQuery searchQuery = NativeQuery.builder()
                    .withQuery(query)
                    .withPageable(pageable)
                    .withSort(s -> s.field(f -> f.field(sortField).order(sortOrder)))
                    .build();
            return elasticsearchTemplate.search(searchQuery, clazz);
        } catch (Exception e) {
            log.error("搜索失败: class={}, sortField={}", clazz.getSimpleName(), sortField, e);
            throw new ElasticsearchException("搜索失败", e);
        }
    }

    /**
     * 执行 NativeQuery 查询
     */
    public <T> SearchHits<T> search(NativeQuery query, Class<T> clazz) {
        try {
            return elasticsearchTemplate.search(query, clazz);
        } catch (Exception e) {
            log.error("搜索失败: class={}", clazz.getSimpleName(), e);
            throw new ElasticsearchException("搜索失败", e);
        }
    }

    /**
     * 搜索并转换为 Page
     */
    public <T> Page<T> searchPage(Query query, Pageable pageable, Class<T> clazz) {
        try {
            SearchHits<T> searchHits = search(query, pageable, clazz);
            List<T> content = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());
            return new PageImpl<>(content, pageable, searchHits.getTotalHits());
        } catch (Exception e) {
            log.error("搜索失败: class={}", clazz.getSimpleName(), e);
            throw new ElasticsearchException("搜索失败", e);
        }
    }

    /**
     * 搜索并转换为 Page（带排序）
     */
    public <T> Page<T> searchPage(Query query, Pageable pageable, String sortField, SortOrder sortOrder, Class<T> clazz) {
        try {
            SearchHits<T> searchHits = search(query, pageable, sortField, sortOrder, clazz);
            List<T> content = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());
            return new PageImpl<>(content, pageable, searchHits.getTotalHits());
        } catch (Exception e) {
            log.error("搜索失败: class={}, sortField={}", clazz.getSimpleName(), sortField, e);
            throw new ElasticsearchException("搜索失败", e);
        }
    }

    // ==================== 统计操作 ====================

    /**
     * 统计文档数量
     */
    public <T> long count(Query query, Class<T> clazz) {
        try {
            NativeQuery searchQuery = NativeQuery.builder()
                    .withQuery(query)
                    .build();
            return elasticsearchTemplate.count(searchQuery, clazz);
        } catch (Exception e) {
            log.error("统计失败: class={}", clazz.getSimpleName(), e);
            return 0L;
        }
    }

    /**
     * 判断文档是否存在
     */
    public <T> boolean exists(String id, Class<T> clazz) {
        try {
            return elasticsearchTemplate.exists(id, clazz);
        } catch (Exception e) {
            log.error("检查文档存在失败: id={}, class={}", id, clazz.getSimpleName(), e);
            return false;
        }
    }

    // ==================== 索引管理 ====================

    /**
     * 创建索引
     */
    public <T> boolean createIndex(Class<T> clazz) {
        try {
            return elasticsearchTemplate.indexOps(clazz).create();
        } catch (Exception e) {
            log.error("创建索引失败: class={}", clazz.getSimpleName(), e);
            return false;
        }
    }

    /**
     * 删除索引
     */
    public <T> boolean deleteIndex(Class<T> clazz) {
        try {
            return elasticsearchTemplate.indexOps(clazz).delete();
        } catch (Exception e) {
            log.error("删除索引失败: class={}", clazz.getSimpleName(), e);
            return false;
        }
    }

    /**
     * 判断索引是否存在
     */
    public <T> boolean indexExists(Class<T> clazz) {
        try {
            return elasticsearchTemplate.indexOps(clazz).exists();
        } catch (Exception e) {
            log.error("检查索引存在失败: class={}", clazz.getSimpleName(), e);
            return false;
        }
    }

    /**
     * 刷新索引
     */
    public <T> void refreshIndex(Class<T> clazz) {
        try {
            elasticsearchTemplate.indexOps(clazz).refresh();
        } catch (Exception e) {
            log.error("刷新索引失败: class={}", clazz.getSimpleName(), e);
        }
    }

    // ==================== 获取底层模板 ====================

    /**
     * 获取底层 ElasticsearchOperations（供特殊场景使用）
     */
    public org.springframework.data.elasticsearch.core.ElasticsearchOperations getTemplate() {
        return elasticsearchTemplate;
    }
}
