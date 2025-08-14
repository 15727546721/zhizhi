package cn.xu.domain.follow.event;

import cn.xu.infrastructure.persistent.dao.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class FollowEventListener {

    private final UserMapper userMapper;
    private final TransactionTemplate transactionTemplate;

    @EventListener
    public void handleFollowEvent(FollowEvent followEvent) {
        log.info("[关注事件] 开始处理：{}", followEvent);
        //关注者的关注数+1
        //被关注者的粉丝数+1
        transactionTemplate.execute(status -> {
            try {
                switch (followEvent.getStatus()) {
                    case FOLLOWED:
                        userMapper.updateFollowCount(followEvent.getFollowerId(), 1);
                        userMapper.updateFansCount(followEvent.getFolloweeId(), 1);
                        break;
                    case UNFOLLOWED:
                        userMapper.updateFollowCount(followEvent.getFollowerId(), -1);
                        userMapper.updateFansCount(followEvent.getFolloweeId(), -1);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                log.error("关注事件处理异常", e);
                status.setRollbackOnly();
            }
            return null;
        });
    }
}
