package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.follow.model.entity.UserFollowEntity;
import cn.xu.domain.follow.model.valueobject.FollowStatus;
import cn.xu.domain.follow.repository.IUserFollowRepository;
import cn.xu.infrastructure.persistent.dao.IUserFollowDao;
import cn.xu.infrastructure.persistent.po.UserFollow;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class UserFollowRepository implements IUserFollowRepository {

    @Resource
    private IUserFollowDao userFollowDao;

    @Override
    public void save(UserFollowEntity entity) {
        UserFollow po = convertToPO(entity);
        userFollowDao.insert(po);
    }

    @Override
    public void updateStatus(Long followerId, Long followedId, Integer status) {
        userFollowDao.updateStatus(followerId, followedId, status);
    }

    @Override
    public UserFollowEntity getByFollowerAndFollowed(Long followerId, Long followedId) {
        UserFollow po = userFollowDao.getByFollowerAndFollowed(followerId, followedId);
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

    private UserFollow convertToPO(UserFollowEntity entity) {
        if (entity == null) {
            return null;
        }
        UserFollow po = new UserFollow();
        BeanUtils.copyProperties(entity, po);
        po.setStatus(entity.getStatus().getValue());
        return po;
    }

    private UserFollowEntity convertToEntity(UserFollow po) {
        if (po == null) {
            return null;
        }
        UserFollowEntity entity = new UserFollowEntity();
        BeanUtils.copyProperties(po, entity);
        entity.setStatus(FollowStatus.valueOf(po.getStatus()));
        return entity;
    }
} 