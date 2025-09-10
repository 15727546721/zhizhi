package cn.xu.domain.follow.event;

import cn.xu.domain.follow.service.IFollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FollowEventListener {

    private final IFollowService followService;

    @EventListener
    public void handleFollowEvent(FollowEvent followEvent) {
        log.info("[关注事件] 开始处理：{}", followEvent);
        // 关注事件的处理已经在FollowService中完成，这里可以添加其他需要的处理逻辑
        // 比如发送通知、更新缓存等
        
        // 示例：记录关注行为到日志或统计系统
        log.info("[关注事件] 用户 {} {} 用户 {}", 
            followEvent.getFollowerId(), 
            followEvent.getStatus() == cn.xu.domain.follow.model.valueobject.FollowStatus.FOLLOWED ? "关注了" : "取消关注了",
            followEvent.getFolloweeId());
    }
}