package cn.xu.domain.like.service.impl;

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

@Service
public class LikeService implements ILikeService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private RingBuffer<LikeEvent> ringBuffer;
    @Autowired
    private ILikeRepository likeRepository;

    @Override
    public void like(Long userId, Integer type, Long targetId, Integer status) {
        if (userId == null || type == null || targetId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), ResponseCode.NULL_PARAMETER.getMessage());
        }

        String relationKey = RedisKeys.likeRelationKey(type, targetId);
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

        List<String> args = Arrays.asList(
                userId.toString(),
                status.toString(), // 1-点赞，0-取消
                timestamp
        );

        // 调用Redis Lua脚本, 执行点赞操作
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        // Lua 脚本路径
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/like.lua")));
        // 返回值类型
        script.setResultType(Long.class);
        Long execute = redisTemplate.execute(script, Collections.singletonList(relationKey), args.toArray());


        if (execute == 2) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "已经点过赞了，不能重复点赞！");
        }

        // 发布Disruptor事件
        if (execute == 1 || execute == 0) {
            publishLikeEvent(LikeEvent.builder()
                   .userId(userId)
                   .type(LikeType.valueOf(type))
                   .targetId(targetId)
                   .status(status == 1)
                   .createTime(LocalDateTime.now())
                   .build());
        }
    }

    @Override
    public boolean checkStatus(Long userId, Integer type, Long targetId) {
        Integer status = likeRepository.findStatus(userId, type, targetId);
        if (status == null || status == 0) {
            return false;
        }
        return true;
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
