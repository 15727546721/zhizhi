package cn.xu.domain.like.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.like.event.LikeEventPublisher;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.repository.ILikeRepository;
import cn.xu.domain.like.service.ILikeService;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.common.utils.ArticleHotScoreCacheHelper;
import cn.xu.infrastructure.common.utils.RedisKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LikeService implements ILikeService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private LikeEventPublisher likeEventPublisher;

    @Autowired
    private ILikeRepository likeRepository;

    @Autowired
    private ArticleHotScoreCacheHelper hotScoreCacheHelper;

    @Override
    public void like(Long userId, Integer type, Long targetId) {
        checkParams(userId, type, targetId);

        String likeKey = RedisKeys.likeRelationKey(type, targetId);
        String userKey = userId.toString();

        try {
            Boolean member = redisTemplate.opsForSet().isMember(likeKey, userId.toString());
            if (Boolean.TRUE.equals(member)) {
                throw new BusinessException("您已经点赞过了！");
            }

            if (Boolean.FALSE.equals(member) && likeRepository.checkStatus(userId, type, targetId)) {
                // 数据库已有点赞，缓存写入并返回异常
                redisTemplate.opsForSet().add(likeKey, userKey);
                throw new BusinessException("您已经点赞过了！");
            }

            // 写入 Redis 缓存点赞状态
            redisTemplate.opsForSet().add(likeKey, userKey);
        } catch (Exception e) {
            log.error("点赞失败", e);
        }

        // 发布异步事件（写库等后续处理）
        likeEventPublisher.publish(userId, targetId, LikeType.valueOf(type), true);
    } 

    @Override
    public void unlike(Long userId, Integer type, Long targetId) {
        checkParams(userId, type, targetId);

        String likeKey = RedisKeys.likeRelationKey(type, targetId);
        String countKey = RedisKeys.likeCountKey(type);
        String userKey = userId.toString();

        try {
            Object existingStatus = redisTemplate.opsForHash().get(likeKey, userKey);

            if ("0".equals(existingStatus)) {
                throw new BusinessException("您还未点赞，无法取消！");
            }

            if (existingStatus == null && !likeRepository.checkStatus(userId, type, targetId)) {
                // 数据库无点赞，缓存写入并返回异常
                redisTemplate.opsForHash().put(likeKey, userKey, "0");
                redisTemplate.expire(likeKey, randomExpire(), TimeUnit.SECONDS);
                throw new BusinessException("您还未点赞，无法取消！");
            }

            // 写入 Redis 缓存取消点赞状态
            redisTemplate.opsForHash().put(likeKey, userKey, "0");
            redisTemplate.expire(likeKey, randomExpire(), TimeUnit.SECONDS);

            // 点赞计数器 -1
            redisTemplate.opsForValue().decrement(countKey);
        } catch (Exception e) {
            log.error("取消点赞失败", e);
        }

        // 发布异步事件
        likeEventPublisher.publish(userId, targetId, LikeType.valueOf(type), false);
    }

    @Override
    public boolean checkStatus(Long userId, Integer type, Long targetId) {
        checkParams(userId, type, targetId);
        return likeRepository.checkStatus(userId, type, targetId);
    }

    /**
     * 校验入参
     */
    private void checkParams(Long userId, Integer type, Long targetId) {
        if (userId == null || type == null || targetId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), ResponseCode.NULL_PARAMETER.getMessage());
        }
    }

    /**
     * 随机过期时间，防止缓存雪崩
     */
    private int randomExpire() {
        return RandomUtil.randomInt(3600, 86400); // 1小时至24小时随机过期秒数
    }
}
