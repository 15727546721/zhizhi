package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.follow.model.entity.UserFollowEntity;
import cn.xu.domain.follow.model.valueobject.FollowStatus;
import cn.xu.domain.follow.repository.IFollowRepository;
import cn.xu.infrastructure.persistent.dao.FollowMapper;
import cn.xu.infrastructure.persistent.po.Follow;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class FollowRepository implements IFollowRepository {

    @Resource
    private FollowMapper userFollowDao;

    @Override
    public void save(UserFollowEntity entity) {
        Follow po = convertToPO(entity);
        userFollowDao.insert(po);
    }

    @Override
    public void updateStatus(Long followerId, Long followedId, Integer status) {
        userFollowDao.updateStatus(followerId, followedId, status);
    }

    @Override
    public UserFollowEntity getByFollowerAndFollowed(Long followerId, Long followedId) {
        Follow po = userFollowDao.getByFollowerAndFollowed(followerId, followedId);
        return convertToEntity(po);
    }

    @Override
    public List<UserFollowEntity> listByFollowerId(Long followerId) {
        return userFollowDao.listByFollowerId(followerId).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserFollowEntity> listByFollowedId(Long followedId) {
        return userFollowDao.listByFollowedId(followedId).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public int countFollowing(Long followerId) {
        return userFollowDao.countFollowing(followerId);
    }

    @Override
    public int countFollowers(Long followedId) {
        return userFollowDao.countFollowers(followedId);
    }

    @Override
    public Integer findStatus(long followerId, long followedId) {
        return userFollowDao.findStatus(followerId, followedId);
    }

    private Follow convertToPO(UserFollowEntity entity) {
        if (entity == null) {
            return null;
        }
        Follow po = new Follow();
        BeanUtils.copyProperties(entity, po);
        po.setStatus(entity.getStatus().getValue());
        return po;
    }

    private UserFollowEntity convertToEntity(Follow po) {
        if (po == null) {
            return null;
        }
        UserFollowEntity entity = new UserFollowEntity();
        BeanUtils.copyProperties(po, entity);
        entity.setStatus(FollowStatus.valueOf(po.getStatus()));
        return entity;
    }
} 