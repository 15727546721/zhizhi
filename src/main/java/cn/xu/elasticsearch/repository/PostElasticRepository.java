package cn.xu.elasticsearch.repository;

import cn.xu.elasticsearch.model.PostIndex;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.time.LocalDateTime;

public interface PostElasticRepository extends ElasticsearchRepository<PostIndex, Long> {

    @Query("{\"bool\": {\"filter\": [{\"range\": {\"publishTime\": {\"gte\": \"?0\", \"lte\": \"?1\"}}}]}}")
    Page<PostIndex> findByPublishTimeBetweenOrderByHotScoreDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<PostIndex> findByTitleContainingOrderByPublishTimeDesc(String keyword, Pageable pageable);

    @Query("{\"bool\": {\"should\": [" +
           "{\"match\": {\"title\": {\"query\": \"?0\", \"boost\": 2.0}}}," +
           "{\"match\": {\"description\": {\"query\": \"?0\", \"boost\": 1.0}}}" +
           "], \"minimum_should_match\": 1}}")
    Page<PostIndex> searchByTitleAndDescription(String keyword, Pageable pageable);
}
