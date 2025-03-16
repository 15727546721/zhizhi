package cn.xu.domain.like.event;

import cn.xu.domain.like.LuaScriptLoader;
import cn.xu.infrastructure.common.utils.RedisKeys;
import cn.xu.infrastructure.persistent.dao.ILikeDao;
import cn.xu.infrastructure.persistent.po.Like;
import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;


/**
 * 点赞事件处理器
 */
@Slf4j
@Component
public class LikeEventHandler implements EventHandler<LikeEvent> {

    @Resource
    private LuaScriptLoader luaScriptLoader;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ILikeDao likeDao;

    @Override
    public void onEvent(LikeEvent event, long sequence, boolean endOfBatch) {
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
            RedisScript<Long> likeScript = luaScriptLoader.getLikeScript("classpath:redis/like_count.lua");
            redisTemplate.execute(likeScript, Arrays.asList(countKey), event.getStatus() ? 1 : -1);
        } catch (Exception e) {
            log.error("点赞记录查询失败", e);
            event.markAsFailed(e);
        }
    }

}