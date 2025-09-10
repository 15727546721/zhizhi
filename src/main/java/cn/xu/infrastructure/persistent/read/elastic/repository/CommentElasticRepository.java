package cn.xu.infrastructure.persistent.read.elastic.repository;

import cn.xu.infrastructure.persistent.read.elastic.model.CommentIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface CommentElasticRepository extends ElasticsearchRepository<CommentIndex, Long> {
    List<CommentIndex> findByTargetIdAndTargetTypeAndParentId(Long targetId, Integer targetType, Long parentId);
    List<CommentIndex> findByParentIdOrderByHotScoreDesc(Long parentId);
}