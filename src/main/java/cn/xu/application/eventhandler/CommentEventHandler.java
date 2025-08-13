package cn.xu.application.eventhandler;

import cn.xu.domain.comment.event.CommentCreatedEvent;
import cn.xu.domain.comment.event.CommentLikedEvent;
import cn.xu.domain.comment.model.valueobject.HotScorePolicy;
import cn.xu.infrastructure.persistent.read.elastic.model.CommentIndex;
import cn.xu.infrastructure.persistent.read.elastic.repository.CommentElasticRepository;
import cn.xu.infrastructure.persistent.read.redis.CommentRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component("appCommentEventHandler")
@RequiredArgsConstructor
public class CommentEventHandler {

    private final ElasticsearchOperations elasticsearchOperations;
    private final CommentElasticRepository commentElasticRepository;
    private final CommentRedisRepository commentRedisRepository;

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;

    @EventListener
    public void handle(CommentCreatedEvent event) {
        // 构建ES索引对象
        CommentIndex index = buildCommentIndex(event);

        // 保存到ES
        elasticsearchOperations.save(index, IndexCoordinates.of("comment_index"));

        // 更新父评论回复数
        if (event.getParentId() != null && event.getParentId() > 0) {
            updateParentCommentStats(event.getParentId());
        }

        // 清除相关缓存
        clearCommentCache(event.getTargetId(), event.getTargetType());
    }

    @EventListener
    public void handle(CommentLikedEvent event) {
        // 1. 更新Redis点赞计数
        commentRedisRepository.incrementLikeCount(event.getCommentId());

        // 2. 更新ES点赞数和热度
        Map<String, Object> params = new HashMap<>();
        params.put("likeDelta", 1);
        params.put("scoreDelta", 1.0);

        String script = "ctx._source.likeCount += params.likeDelta; ctx._source.hotScore += params.scoreDelta";

        UpdateQuery updateQuery = UpdateQuery.builder(event.getCommentId().toString())
                .withScript(script)
                .withParams(params)
                .build();

        elasticsearchOperations.update(updateQuery, IndexCoordinates.of("comment_index"));

        // 3. 清除相关缓存
        clearCommentCacheForComment(event.getCommentId());
    }

    private CommentIndex buildCommentIndex(CommentCreatedEvent event) {
        CommentIndex index = new CommentIndex();
        index.setId(event.getId());
        index.setTargetType(event.getTargetType());
        index.setTargetId(event.getTargetId());
        index.setParentId(event.getParentId());
        index.setUserId(event.getUserId());
        index.setReplyUserId(event.getReplyUserId());
        index.setContent(event.getContent());
        index.setLikeCount(0L);
        index.setReplyCount(0L);
        index.setCreateTime(LocalDateTime.now());

        // 初始热度计算
        index.setHotScore(HotScorePolicy.calculate(0, 0, index.getCreateTime()));

        return index;
    }

    private void updateParentCommentStats(Long parentId) {
        Map<String, Object> params = new HashMap<>();
        params.put("scoreDelta", 0.7);

        String script = "ctx._source.replyCount += 1; ctx._source.hotScore += params.scoreDelta";

        UpdateQuery updateQuery = UpdateQuery.builder(parentId.toString())
                .withScript(script)
                .withParams(params)
                .build();

        elasticsearchOperations.update(updateQuery, IndexCoordinates.of("comment_index"));

        updateTopReplies(parentId);
    }

    private void updateTopReplies(Long parentId) {
        List<CommentIndex> childComments = commentElasticRepository.findByParentIdOrderByHotScoreDesc(parentId);

        List<CommentIndex.SubComment> topReplies = childComments.stream()
                .limit(2)
                .map(this::convertToSubComment)
                .collect(Collectors.toList());

        Map<String, Object> params = new HashMap<>();
        params.put("topReplies", topReplies);

        String script = "ctx._source.topReplies = params.topReplies";

        UpdateQuery updateQuery = UpdateQuery.builder(parentId.toString())
                .withScript(script)
                .withParams(params)
                .build();

        elasticsearchOperations.update(updateQuery, IndexCoordinates.of("comment_index"));
    }

    private CommentIndex.SubComment convertToSubComment(CommentIndex comment) {
        CommentIndex.SubComment sub = new CommentIndex.SubComment();
        sub.setId(comment.getId());
        sub.setContent(comment.getContent());
        sub.setLikeCount(comment.getLikeCount());
        sub.setCreateTime(comment.getCreateTime());
        // 可以扩展设置用户头像、昵称等
        return sub;
    }

    private void clearCommentCache(Long targetId, Integer targetType) {
        String pattern = "comment:top:" + targetId + ":" + targetType + "*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private void clearCommentCacheForComment(Long commentId) {
        String pattern = "comment:child:" + commentId + "*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
