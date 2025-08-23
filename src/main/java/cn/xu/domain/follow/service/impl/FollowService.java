package cn.xu.domain.follow.service.impl;

import cn.xu.domain.follow.event.FollowEvent;
import cn.xu.domain.follow.event.FollowEventPublisher;
import cn.xu.domain.follow.model.entity.UserFollowEntity;
import cn.xu.domain.follow.model.valueobject.FollowStatus;
import cn.xu.domain.follow.repository.IFollowRepository;
import cn.xu.domain.follow.service.IFollowService;
import cn.xu.infrastructure.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class FollowService implements IFollowService {

    @Resource
    private IFollowRepository userFollowRepository;
    @Resource
    private FollowEventPublisher followEventPublisher;



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(Long followerId, Long followedId) {
        // 不能关注自己
        if (followerId.equals(followedId)) {
            throw new BusinessException("不能关注自己");
        }

        UserFollowEntity existingFollow = userFollowRepository.getByFollowerAndFollowed(followerId, followedId);
        if (existingFollow != null) {
            if (existingFollow.getStatus() == FollowStatus.FOLLOWED) {
                return;
            }
            userFollowRepository.updateStatus(followerId, followedId, FollowStatus.FOLLOWED.getValue());
        } else {
            UserFollowEntity newFollow = new UserFollowEntity()
                    .setFollowerId(followerId)
                    .setFollowedId(followedId)
                    .setStatus(FollowStatus.FOLLOWED);
            userFollowRepository.save(newFollow);
        }
        // 推送关注事件
        pushFollowEvent(FollowEvent.builder()
               .followerId(followerId)
               .followeeId(followedId)
               .status(FollowStatus.FOLLOWED)
               .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(Long followerId, Long followedId) {
        UserFollowEntity existingFollow = userFollowRepository.getByFollowerAndFollowed(followerId, followedId);
        if (existingFollow != null && existingFollow.getStatus() == FollowStatus.FOLLOWED) {
            userFollowRepository.updateStatus(followerId, followedId, FollowStatus.UNFOLLOWED.getValue());
        }
        // 推送取消关注事件
        pushFollowEvent(FollowEvent.builder()
               .followerId(followerId)
               .followeeId(followedId)
               .status(FollowStatus.UNFOLLOWED)
               .build());
    }

    @Override
    public boolean isFollowing(Long followerId, Long followedId) {
        UserFollowEntity follow = userFollowRepository.getByFollowerAndFollowed(followerId, followedId);
        return follow != null && follow.getStatus() == FollowStatus.FOLLOWED;
    }

    @Override
    public List<UserFollowEntity> getFollowingList(Long followerId) {
        return userFollowRepository.listByFollowerId(followerId);
    }

    @Override
    public List<UserFollowEntity> getFollowersList(Long followedId) {
        return userFollowRepository.listByFollowedId(followedId);
    }

    @Override
    public int getFollowingCount(Long followerId) {
        return userFollowRepository.countFollowing(followerId);
    }

    @Override
    public int getFollowersCount(Long followedId) {
        return userFollowRepository.countFollowers(followedId);
    }

    @Override
    public boolean checkStatus(long followerId, long followedId) {
        if (followerId == 0 || followedId == 0) {
            // 未登录用户默认未关注
            return false;
        }
         Integer status = userFollowRepository.findStatus(followerId, followedId);
         if (status == null || status == 0) {
             return false;
         }
         return true;
    }

    private void pushFollowEvent(FollowEvent followEvent) {
        followEventPublisher.publish(followEvent);
    }
} 