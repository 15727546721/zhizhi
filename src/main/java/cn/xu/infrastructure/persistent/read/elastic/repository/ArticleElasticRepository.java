package cn.xu.infrastructure.persistent.read.elastic.repository;

import cn.xu.infrastructure.persistent.read.elastic.model.ArticleIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface ArticleElasticRepository extends ElasticsearchRepository<ArticleIndex, Long> {

    // 按热度排序，过滤指定时间区间（日榜、周榜、月榜）
    @Query("{\"bool\": {\"filter\": [{\"range\": {\"publishTime\": {\"gte\": \"?0\", \"lte\": \"?1\"}}}]}}")
    Page<ArticleIndex> findByPublishTimeBetweenOrderByHotScoreDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // 按发布时间倒序（最简单时间降序搜索）
    Page<ArticleIndex> findByTitleContainingOrderByPublishTimeDesc(String keyword, Pageable pageable);
}
