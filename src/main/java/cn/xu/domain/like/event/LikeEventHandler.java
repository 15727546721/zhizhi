package cn.xu.domain.like.event;

import cn.xu.infrastructure.common.utils.RedisKeys;
import cn.xu.infrastructure.persistent.dao.ILikeDao;
import cn.xu.infrastructure.persistent.po.Like;
import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;


/**
 * 点赞事件处理器
 */
@Slf4j
@Component
public class LikeEventHandler implements EventHandler<LikeEvent> {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ILikeDao likeDao;

    @Override
    public void onEvent(LikeEvent event, long sequence, boolean endOfBatch) {
        log.info("处理点赞事件: {}", event);
        try {
            // 更新MySQL点赞记录
            Like like = likeDao.findByUserIdAndTypeAndTargetId(
                    event.getUserId(), event.getType().getValue(), event.getTargetId());
            if (like == null) {
                likeDao.save(Like.builder()
                        .userId(event.getUserId())
                        .targetId(event.getTargetId())
                        .type(event.getType().getValue())
                        .status(event.getStatus() == null ? 1 : (event.getStatus() ? 1 : 0))
                        .createTime(event.getCreateTime())
                        .build());
            } else {
                // 更新点赞状态
                likeDao.updateStatus(event.getUserId(),
                        event.getType().getValue(),
                        event.getTargetId(),
                        event.getStatus() ? 1 : 0);
            }
            String countKey = RedisKeys.likeCountKey(event.getType().getValue(), event.getTargetId());
            // 调用Redis Lua脚本, 执行点赞操作
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            // Lua 脚本路径
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/like_count.lua")));
            // 返回值类型
            script.setResultType(Long.class);
            Long execute = redisTemplate.execute(script, Collections.singletonList(countKey), event.getStatus() ? 1 : -1);
            if (execute == null || execute == -1) {
                log.error("点赞计数有误");
            }
        } catch (Exception e) {
            log.error("点赞记录查询失败", e);
            event.markAsFailed(e);
        }
    }

}