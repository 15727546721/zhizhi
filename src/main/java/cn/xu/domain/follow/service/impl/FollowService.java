package cn.xu.domain.follow.service.impl;

import cn.xu.domain.follow.event.FollowEvent;
import cn.xu.domain.follow.event.FollowEventPublisher;
import cn.xu.domain.follow.model.entity.UserFollowEntity;
import cn.xu.domain.follow.model.valueobject.FollowStatus;
import cn.xu.domain.follow.repository.IFollowRepository;
import cn.xu.domain.follow.service.FollowApplicationService;
import cn.xu.domain.follow.service.IFollowService;
import cn.xu.domain.user.model.aggregate.UserAggregate;
import cn.xu.domain.user.repository.IUserAggregateRepository;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService implements IFollowService {

    private final IFollowRepository userFollowRepository;
    private final FollowEventPublisher followEventPublisher;
    private final IUserAggregateRepository userAggregateRepository;
    private final FollowApplicationService followApplicationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(Long followerId, Long followedId) {
        followApplicationService.followUser(followerId, followedId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(Long followerId, Long followedId) {
        followApplicationService.unfollowUser(followerId, followedId);
    }

    @Override
    public boolean isFollowing(Long followerId, Long followedId) {
        return followApplicationService.isFollowing(followerId, followedId);
    }

    @Override
    public List<UserFollowEntity> getFollowingList(Long followerId) {
        // 这个方法在新的设计中将逐步废弃，使用分页版本
        return getFollowingList(followerId, 1, 100);
    }

    @Override
    public List<UserFollowEntity> getFollowersList(Long followedId) {
        // 这个方法在新的设计中将逐步废弃，使用分页版本
        return getFollowersList(followedId, 1, 100);
    }
    
    /**
     * 获取用户的关注列表（分页）
     * 
     * @param followerId 关注者ID
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 关注关系实体列表
     */
    public List<UserFollowEntity> getFollowingList(Long followerId, Integer pageNo, Integer pageSize) {
        // 转换调用新的应用服务
        // 注意：这里需要做适配，将FollowAggregate转换为UserFollowEntity
        // 在实际实现中，可能需要调整接口设计
        return Collections.emptyList();
    }
    
    /**
     * 获取用户的粉丝列表（分页）
     * 
     * @param followedId 被关注者ID
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 关注关系实体列表
     */
    public List<UserFollowEntity> getFollowersList(Long followedId, Integer pageNo, Integer pageSize) {
        // 转换调用新的应用服务
        // 注意：这里需要做适配，将FollowAggregate转换为UserFollowEntity
        // 在实际实现中，可能需要调整接口设计
        return Collections.emptyList();
    }

    @Override
    public int getFollowingCount(Long followerId) {
        return followApplicationService.getFollowingCount(followerId);
    }

    @Override
    public int getFollowersCount(Long followedId) {
        return followApplicationService.getFollowersCount(followedId);
    }

    @Override
    public boolean checkStatus(long followerId, long followedId) {
        if (followerId == 0 || followedId == 0) {
            // 未登录用户默认未关注
            return false;
        }
        
        // 验证用户ID的有效性
        UserFollowEntity.validateFollowRelation(followerId, followedId);
         
        return followApplicationService.isFollowing(followerId, followedId);
    }
    
    private void pushFollowEvent(FollowEvent followEvent) {
        followEventPublisher.publish(followEvent);
    }
}