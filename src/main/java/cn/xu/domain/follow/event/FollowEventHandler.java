package cn.xu.domain.follow.event;

import cn.xu.infrastructure.persistent.dao.IUserDao;
import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;

@Slf4j
@Component
public class FollowEventHandler implements EventHandler<FollowEvent> {

    @Resource
    private IUserDao userDao;
    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public void onEvent(FollowEvent followEvent, long sequence, boolean endOfBatch) throws Exception {
        log.info("[关注事件]处理事件：{}", followEvent);
        //关注者的关注数+1
        //被关注者的粉丝数+1
        transactionTemplate.execute(status -> {
            try {
                userDao.updateFollowCount(followEvent.getFollowerId(), 1);
                userDao.updateFansCount(followEvent.getFolloweeId(), 1);
            } catch (Exception e) {
                status.setRollbackOnly();
                log.error("[关注计数]更新失败", e);
            }
            return 1;
        });
    }
}
