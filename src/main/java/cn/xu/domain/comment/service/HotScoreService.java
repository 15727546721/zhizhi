package cn.xu.domain.comment.service;

import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.model.valueobject.CommentType;
import cn.xu.domain.comment.repository.ICommentRepository;
import cn.xu.infrastructure.cache.RedisKeyManager;
import cn.xu.infrastructure.persistent.read.redis.CommentRedisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Set;

@Service
@Slf4j
public class HotScoreService {

    @Resource
    private ICommentRepository commentRepository;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final int MAX_HOT_SIZE = 100;

    private static final String LUA_UPDATE_HOT_SCRIPT =
            "-- KEYS[1]: ZSet key\n" +
                    "-- ARGV[1]: commentId\n" +
                    "-- ARGV[2]: likeCount\n" +
                    "-- ARGV[3]: replyCount\n" +
                    "-- ARGV[4]: createTime (epoch seconds)\n" +
                    "-- ARGV[5]: maxSize\n" +
                    "\n" +
                    "local zsetKey = KEYS[1]\n" +
                    "local commentId = ARGV[1]\n" +
                    "local likeCount = tonumber(ARGV[2])\n" +
                    "local replyCount = tonumber(ARGV[3])\n" +
                    "local createTime = tonumber(ARGV[4])\n" +
                    "local maxSize = tonumber(ARGV[5])\n" +
                    "\n" +
                    "local now = tonumber(redis.call('TIME')[1])\n" +
                    "local hours = (now - createTime) / 3600\n" +
                    "local decay = hours + 2\n" +
                    "local hotScore = (likeCount * 2 + replyCount * 3) / decay\n" +
                    "\n" +
                    "redis.call('ZADD', zsetKey, hotScore, commentId)\n" +
                    "\n" +
                    "local count = redis.call('ZCARD', zsetKey)\n" +
                    "if count > maxSize then\n" +
                    "    redis.call('ZREMRANGEBYRANK', zsetKey, 0, count - maxSize - 1)\n" +
                    "end\n" +
                    "\n" +
                    "return hotScore\n";

    private final DefaultRedisScript<Double> redisScript;

    public HotScoreService() {
        redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(LUA_UPDATE_HOT_SCRIPT);
        redisScript.setResultType(Double.class);
    }

    /**
     * 根据commentId更新该评论的热度，调用Lua脚本处理
     */
    public void updateHotScore(Long commentId) {
        CommentEntity comment = commentRepository.findById(commentId);
        if (comment == null) {
            log.warn("updateHotScore: 评论不存在, commentId={}", commentId);
            return;
        }

        String redisKey;
        // 设计规则：parentId为空是一级评论，存一级ZSet；否则存二级回复ZSet
        if (comment.getParentId() == null) {
            redisKey = RedisKeyManager.commentHotRankKey(CommentType.valueOf(comment.getTargetType()), comment.getTargetId());
        } else {
            redisKey = RedisKeyManager.replyHotRankKey(CommentType.valueOf(comment.getTargetType()), comment.getTargetId(), comment.getParentId());
        }

        Long createEpochSecond = comment.getCreateTime().atZone(ZoneId.systemDefault()).toEpochSecond();

        // 调用Lua脚本
        Double hotScore = redisTemplate.execute(
                redisScript,
                Collections.singletonList(redisKey),
                commentId.toString(),
                comment.getLikeCount().toString(),
                comment.getReplyCount().toString(),
                createEpochSecond.toString(),
                String.valueOf(MAX_HOT_SIZE)
        );

        if (hotScore != null) {
            // 可选：同步更新数据库热度字段（非必须）
//            commentRepository.updateHotScore(commentId, hotScore);
            log.info("更新热度成功 commentId={} hotScore={}", commentId, hotScore);
        } else {
            log.warn("更新热度失败 commentId={}", commentId);
        }
    }

    /**
     * 定时衰减所有热度数据，建议每天凌晨执行
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void decayAllHotScores() {
        log.info("开始执行热度衰减任务...");
        // 1. 查找所有一级评论热度ZSet key（模糊匹配）
        Set<String> keys = redisTemplate.keys("comment:hot:*");
        if (keys == null || keys.isEmpty()) {
            log.info("无热度数据，跳过衰减");
            return;
        }

        final double DECAY_FACTOR = 0.9; // 每天衰减10%

        for (String key : keys) {
            Set<ZSetOperations.TypedTuple<Object>> items = redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);
            if (items == null) continue;

            for (ZSetOperations.TypedTuple<Object> item : items) {
                String member = item.getValue().toString();
                Double oldScore = item.getScore();
                if (oldScore == null) continue;

                double newScore = oldScore * DECAY_FACTOR;

                if (newScore < 1.0) {
                    redisTemplate.opsForZSet().remove(key, member);
                    log.debug("衰减后热度过低，删除 member={} key={}", member, key);
                } else {
                    redisTemplate.opsForZSet().add(key, member, newScore);
                    log.debug("衰减更新 member={} key={} 新热度={}", member, key, newScore);
                }
            }
        }
        log.info("热度衰减任务完成");
    }
}
