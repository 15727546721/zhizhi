package cn.xu.domain.follow.event;

import cn.xu.infrastructure.persistent.dao.UserMapper;
import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;

@Slf4j
@Component
public class FollowEventHandler implements EventHandler<FollowEvent> {

    @Resource
    private UserMapper userDao;
    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public void onEvent(FollowEvent followEvent, long sequence, boolean endOfBatch) throws Exception {
        log.info("[关注事件]处理事件：{}", followEvent);
        //关注者的关注数+1
        //被关注者的粉丝数+1
        transactionTemplate.execute(status -> {
            try {
                switch (followEvent.getStatus()) {
                    case FOLLOWED:
                        userDao.updateFollowCount(followEvent.getFollowerId(), 1);
                        userDao.updateFansCount(followEvent.getFolloweeId(), 1);
                        break;
                    case UNFOLLOWED:
                        userDao.updateFollowCount(followEvent.getFollowerId(), -1);
                        userDao.updateFansCount(followEvent.getFolloweeId(), -1);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                // 处理异常
                log.error("关注事件处理异常", e);
                status.setRollbackOnly();
            }
            return 1;
        });
    }
}
