package cn.xu.repository.read.elastic.repository;

import cn.xu.repository.read.elastic.model.PostIndex;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.time.LocalDateTime;

public interface PostElasticRepository extends ElasticsearchRepository<PostIndex, Long> {

    // 按热度排序，过滤指定时间区间（日榜、周榜、月榜）
    @Query("{\"bool\": {\"filter\": [{\"range\": {\"publishTime\": {\"gte\": \"?0\", \"lte\": \"?1\"}}}]}}")
    Page<PostIndex> findByPublishTimeBetweenOrderByHotScoreDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // 按发布时间倒序（简单搜索，仅搜索title）
    Page<PostIndex> findByTitleContainingOrderByPublishTimeDesc(String keyword, Pageable pageable);

    // 多字段搜索（title和description），按相关性排序，然后按时间排序
    // 注意：ES索引中不包含status字段，只索引已发布的帖子，所以不需要status过滤
    @Query("{\"bool\": {\"should\": [" +
           "{\"match\": {\"title\": {\"query\": \"?0\", \"boost\": 2.0}}}," +
           "{\"match\": {\"description\": {\"query\": \"?0\", \"boost\": 1.0}}}" +
           "], \"minimum_should_match\": 1}}")
    Page<PostIndex> searchByTitleAndDescription(String keyword, Pageable pageable);
}