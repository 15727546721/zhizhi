package cn.xu.domain.like.event;

import cn.xu.domain.like.repository.ILikeRepository;
import cn.xu.infrastructure.persistent.po.Like;
import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 点赞事件处理器
 */
@Slf4j
@Component
public class LikeEventHandler implements EventHandler<LikeEvent> {
    @Autowired
    private ILikeRepository likeRepository;

    @Override
    public void onEvent(LikeEvent event, long sequence, boolean endOfBatch) {
        // 更新MySQL点赞记录
        Like like = likeRepository.findByUserIdAndTypeAndTargetId(
                event.getUserId(), event.getType().getValue(), event.getTargetId());

        // 推送到计数服务处理
    }
}