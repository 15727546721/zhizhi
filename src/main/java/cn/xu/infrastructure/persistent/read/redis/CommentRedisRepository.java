package cn.xu.infrastructure.persistent.read.redis;

import cn.xu.api.web.model.dto.comment.FindChildCommentItemVO;
import cn.xu.api.web.model.dto.comment.FindCommentItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentRedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    // 存储一级评论
    public void cacheTopComments(String key, List<FindCommentItemVO> comments, int minutes) {
        redisTemplate.opsForValue().set(
                key,
                comments,
                Duration.ofMinutes(minutes)
        );
    }

    // 获取一级评论
    @SuppressWarnings("unchecked")
    public List<FindCommentItemVO> getTopComments(String key) {
        return (List<FindCommentItemVO>) redisTemplate.opsForValue().get(key);
    }

    // 存储子评论
    public void cacheChildComments(String key, List<FindChildCommentItemVO> comments, int minutes) {
        redisTemplate.opsForValue().set(
                key,
                comments,
                Duration.ofMinutes(minutes)
        );
    }

    // 获取子评论
    @SuppressWarnings("unchecked")
    public List<FindChildCommentItemVO> getChildComments(String key) {
        return (List<FindChildCommentItemVO>) redisTemplate.opsForValue().get(key);
    }

    // 评论点赞计数器
    public Long incrementLikeCount(Long commentId) {
        return redisTemplate.opsForHash().increment(
                "comment:counters",
                "like:" + commentId,
                1
        );
    }
}