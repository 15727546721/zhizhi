package cn.xu.domain.like.service.impl;

import cn.xu.application.common.ResponseCode;
import cn.xu.domain.like.LuaScriptLoader;
import cn.xu.domain.like.event.LikeEvent;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.repository.ILikeRepository;
import cn.xu.domain.like.service.ILikeService;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.common.utils.RedisKeys;
import com.lmax.disruptor.RingBuffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class LikeService implements ILikeService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private RingBuffer<LikeEvent> ringBuffer;
    @Autowired
    private LuaScriptLoader luaScriptLoader;
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
        Long value = redisTemplate.execute(
                luaScriptLoader.getLikeScript(),
                Arrays.asList(relationKey),
                args
        );


        if (value == 2) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "已经点过赞了，不能重复点赞！");
        }

        // 发布Disruptor事件
        if (value == 1 || value == 0){
            publishLikeEvent(userId, type, targetId, value == 1);
        }
    }

    @Override
    public boolean checkStatus(Long userId, Integer type, Long targetId) {
        return likeRepository.findStatus(userId, type, targetId) == 1;
    }

    private void publishLikeEvent(Long userId, Integer type, Long targetId, boolean isLiked) {
        ringBuffer.publishEvent((event, sequence) -> {
            event.setUserId(userId);
            event.setType(LikeType.valueOf(type));
            event.setTargetId(targetId);
            event.setStatus(isLiked);
            event.setCreateTime(LocalDateTime.now());
        });
    }
}