package cn.xu.domain.like.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.like.event.LikeEvent;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.repository.ILikeRepository;
import cn.xu.domain.like.service.ILikeService;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.common.utils.RedisKeys;
import com.lmax.disruptor.RingBuffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class LikeService implements ILikeService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private RingBuffer<LikeEvent> ringBuffer;
    @Autowired
    private ILikeRepository likeRepository;

    @Override
    public void like(Long userId, Integer type, Long targetId) {
        if (userId == null || type == null || targetId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), ResponseCode.NULL_PARAMETER.getMessage());
        }

        String likeKey = RedisKeys.likeRelationKey(type, targetId);

        // 直接获取Hash中的value，减少一次查询
        Object existingStatus = redisTemplate.opsForHash().get(likeKey, userId.toString());

        if (existingStatus != null && existingStatus.toString().equals("1")) {
           throw new BusinessException("您已经点赞过了！");
        }

        // 如果Redis中没有该用户的点赞状态，检查数据库
        if (existingStatus == null) {
            boolean hasLike = likeRepository.checkStatus(userId, type, targetId);
            if (hasLike) {
                // 更新Redis中的点赞状态
                redisTemplate.opsForHash().put(likeKey, userId.toString(), "1");
                // 设置随机过期时间
                int randomInt = RandomUtil.randomInt(24 * 60 * 60); // 随机过期时间
                redisTemplate.expire(likeKey, randomInt, TimeUnit.SECONDS);
                throw new BusinessException("您已经点赞过了！");
            }
        }

        // 更新Redis中的点赞状态
        redisTemplate.opsForHash().put(likeKey, userId.toString(), "1");

        // 设置随机过期时间
        int randomInt = RandomUtil.randomInt(24 * 60 * 60); // 随机过期时间
        redisTemplate.expire(likeKey, randomInt, TimeUnit.SECONDS);

        // 发布Disruptor事件
        publishLikeEvent(LikeEvent.builder()
                .userId(userId)
                .type(LikeType.valueOf(type))
                .targetId(targetId)
                .status(true)
                .createTime(LocalDateTime.now())
                .build());
    }

    @Override
    public void unlike(Long userId, Integer type, Long targetId) {
        if (userId == null || type == null || targetId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), ResponseCode.NULL_PARAMETER.getMessage());
        }

        String likeKey = RedisKeys.likeRelationKey(type, targetId);

        // 直接获取Hash中的value，减少一次查询
        Object existingStatus = redisTemplate.opsForHash().get(likeKey, userId.toString());

        if (existingStatus != null && existingStatus.toString().equals("0")) {
            throw new BusinessException("您还没有点赞过，不能取消点赞！");
        }

        // 如果Redis中没有该用户的点赞状态，检查数据库
        if (existingStatus == null) {
            boolean hasLike = likeRepository.checkStatus(userId, type, targetId);
            if (!hasLike) {
                // 更新Redis中的点赞状态
                redisTemplate.opsForHash().put(likeKey, userId.toString(), "0");
                // 设置随机过期时间
                int randomInt = RandomUtil.randomInt(24 * 60 * 60); // 随机过期时间
                redisTemplate.expire(likeKey, randomInt, TimeUnit.SECONDS);
                throw new BusinessException("您还没有点赞过，不能取消点赞！");
            }
        }

        // 更新Redis中的点赞状态
        redisTemplate.opsForHash().put(likeKey, userId.toString(), "0");

        // 设置随机过期时间
        int randomInt = RandomUtil.randomInt(24 * 60 * 60); // 随机过期时间
        redisTemplate.expire(likeKey, randomInt, TimeUnit.SECONDS);

        // 发布Disruptor事件
        publishLikeEvent(LikeEvent.builder()
                .userId(userId)
                .type(LikeType.valueOf(type))
                .targetId(targetId)
                .status(true)
                .createTime(LocalDateTime.now())
                .build());
    }


    @Override
    public boolean checkStatus(Long userId, Integer type, Long targetId) {
        return likeRepository.checkStatus(userId, type, targetId);
    }

    private void publishLikeEvent(LikeEvent likeEvent) {
        ringBuffer.publishEvent((event, sequence) -> {
            event.setUserId(likeEvent.getUserId());
            event.setType(likeEvent.getType());
            event.setTargetId(likeEvent.getTargetId());
            event.setStatus(likeEvent.getStatus());
            event.setCreateTime(likeEvent.getCreateTime());
        });
    }
}
