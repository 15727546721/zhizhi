package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.follow.model.entity.UserFollowEntity;
import cn.xu.domain.follow.repository.IFollowRepository;
import cn.xu.infrastructure.persistent.converter.FollowConverter;
import cn.xu.infrastructure.persistent.dao.FollowMapper;
import cn.xu.infrastructure.persistent.po.Follow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 关注关系仓储实现类
 * 通过FollowConverter进行领域实体与持久化对象的转换，遵循DDD防腐层模式
 * 
 * @author xu
 */
@Repository
@RequiredArgsConstructor
public class FollowRepository implements IFollowRepository {

    private final FollowMapper userFollowDao;
    private final FollowConverter followConverter;

    @Override
    public void save(UserFollowEntity entity) {
        Follow po = followConverter.toDataObject(entity);
        userFollowDao.insert(po);
        entity.setId(po.getId());
    }

    @Override
    public void updateStatus(Long followerId, Long followedId, Integer status) {
        userFollowDao.updateStatus(followerId, followedId, status);
    }

    @Override
    public UserFollowEntity getByFollowerAndFollowed(Long followerId, Long followedId) {
        Follow po = userFollowDao.getByFollowerAndFollowed(followerId, followedId);
        return followConverter.toDomainEntity(po);
    }

    @Override
    public List<UserFollowEntity> listByFollowerId(Long followerId) {
        List<Follow> follows = userFollowDao.listByFollowerId(followerId);
        return followConverter.toDomainEntities(follows);
    }

    @Override
    public List<UserFollowEntity> listByFollowedId(Long followedId) {
        List<Follow> follows = userFollowDao.listByFollowedId(followedId);
        return followConverter.toDomainEntities(follows);
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


} 