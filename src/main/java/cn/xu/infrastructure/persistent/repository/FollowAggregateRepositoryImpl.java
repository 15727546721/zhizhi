package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.follow.model.aggregate.FollowAggregate;
import cn.xu.domain.follow.model.valueobject.FollowStatus;
import cn.xu.domain.follow.repository.IFollowAggregateRepository;
import cn.xu.infrastructure.persistent.converter.FollowConverter;
import cn.xu.infrastructure.persistent.dao.FollowMapper;
import cn.xu.infrastructure.persistent.po.Follow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 关注关系聚合根仓储实现类
 * 通过Converter进行领域实体与持久化对象的转换，遵循DDD防腐层模式
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class FollowAggregateRepositoryImpl implements IFollowAggregateRepository {
    
    private final FollowMapper followDao;
    private final FollowConverter followConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(FollowAggregate aggregate) {
        try {
            log.info("[关注关系聚合根] 开始保存关注关系聚合根");
            
            // 保存关注关系实体
            Follow followPO = followConverter.toDataObjectFromFollowRelation(aggregate.getFollowRelation());
            
            if (followPO.getId() == null) {
                followDao.insert(followPO);
                aggregate.getFollowRelation().setId(followPO.getId());
                log.info("[关注关系聚合根] 新增关注关系成功，ID: {}", followPO.getId());
            } else {
                followDao.updateStatus(
                    followPO.getFollowerId(), 
                    followPO.getFollowedId(), 
                    followPO.getStatus()
                );
                log.info("[关注关系聚合根] 更新关注关系成功，ID: {}", followPO.getId());
            }
            
            return followPO.getId();
        } catch (Exception e) {
            log.error("[关注关系聚合根] 保存关注关系聚合根失败", e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(FollowAggregate aggregate) {
        try {
            log.info("[关注关系聚合根] 开始更新关注关系聚合根，ID: {}", aggregate.getId());
            
            // 更新关注关系实体
            Follow followPO = followConverter.toDataObjectFromFollowRelation(aggregate.getFollowRelation());
            followDao.updateStatus(
                followPO.getFollowerId(), 
                followPO.getFollowedId(), 
                followPO.getStatus()
            );
            
            log.info("[关注关系聚合根] 更新关注关系聚合根成功");
        } catch (Exception e) {
            log.error("[关注关系聚合根] 更新关注关系聚合根失败，ID: {}", aggregate.getId(), e);
            throw e;
        }
    }

    @Override
    public Optional<FollowAggregate> findById(Long id) {
        try {
            log.info("[关注关系聚合根] 开始查询关注关系聚合根，ID: {}", id);
            
            // 查询关注关系
            Follow follow = followDao.findById(id);
            if (follow == null) {
                log.info("[关注关系聚合根] 关注关系不存在，ID: {}", id);
                return Optional.empty();
            }
            
            FollowAggregate aggregate = FollowAggregate.restore(
                follow.getId(),
                follow.getFollowerId(),
                follow.getFollowedId(),
                follow.getStatus() != null ? FollowStatus.valueOf(follow.getStatus()) : null,
                follow.getCreateTime(),
                follow.getUpdateTime()
            );
            
            log.info("[关注关系聚合根] 查询关注关系聚合根成功");
            return Optional.of(aggregate);
        } catch (Exception e) {
            log.error("[关注关系聚合根] 查询关注关系聚合根失败，ID: {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<FollowAggregate> findByFollowerAndFollowed(Long followerId, Long followedId) {
        try {
            log.info("[关注关系聚合根] 开始根据关注者和被关注者查询关注关系聚合根，关注者: {}, 被关注者: {}", 
                    followerId, followedId);
            
            // 查询关注关系
            Follow follow = followDao.getByFollowerAndFollowed(followerId, followedId);
            if (follow == null) {
                log.info("[关注关系聚合根] 关注关系不存在，关注者: {}, 被关注者: {}", followerId, followedId);
                return Optional.empty();
            }
            
            FollowAggregate aggregate = FollowAggregate.restore(
                follow.getId(),
                follow.getFollowerId(),
                follow.getFollowedId(),
                follow.getStatus() != null ? FollowStatus.valueOf(follow.getStatus()) : null,
                follow.getCreateTime(),
                follow.getUpdateTime()
            );
            
            log.info("[关注关系聚合根] 查询关注关系聚合根成功");
            return Optional.of(aggregate);
        } catch (Exception e) {
            log.error("[关注关系聚合根] 根据关注者和被关注者查询关注关系聚合根失败，关注者: {}, 被关注者: {}", 
                     followerId, followedId, e);
            return Optional.empty();
        }
    }

    @Override
    public List<FollowAggregate> findFollowingList(Long followerId, Integer pageNo, Integer pageSize) {
        try {
            log.info("[关注关系聚合根] 开始查询关注列表，关注者: {}, 页码: {}, 页面大小: {}", 
                    followerId, pageNo, pageSize);
            
            int offset = (pageNo - 1) * pageSize;
            List<Follow> follows = followDao.listFollowingByPage(followerId, offset, pageSize);
            
            List<FollowAggregate> aggregates = follows.stream()
                    .map(follow -> FollowAggregate.restore(
                        follow.getId(),
                        follow.getFollowerId(),
                        follow.getFollowedId(),
                        follow.getStatus() != null ? FollowStatus.valueOf(follow.getStatus()) : null,
                        follow.getCreateTime(),
                        follow.getUpdateTime()
                    ))
                    .collect(Collectors.toList());
            
            log.info("[关注关系聚合根] 查询关注列表成功，返回数量: {}", aggregates.size());
            return aggregates;
        } catch (Exception e) {
            log.error("[关注关系聚合根] 查询关注列表失败，关注者: {}, 页码: {}, 页面大小: {}", 
                     followerId, pageNo, pageSize, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<FollowAggregate> findFollowersList(Long followedId, Integer pageNo, Integer pageSize) {
        try {
            log.info("[关注关系聚合根] 开始查询粉丝列表，被关注者: {}, 页码: {}, 页面大小: {}", 
                    followedId, pageNo, pageSize);
            
            int offset = (pageNo - 1) * pageSize;
            List<Follow> follows = followDao.listFollowersByPage(followedId, offset, pageSize);
            
            List<FollowAggregate> aggregates = follows.stream()
                    .map(follow -> FollowAggregate.restore(
                        follow.getId(),
                        follow.getFollowerId(),
                        follow.getFollowedId(),
                        follow.getStatus() != null ? FollowStatus.valueOf(follow.getStatus()) : null,
                        follow.getCreateTime(),
                        follow.getUpdateTime()
                    ))
                    .collect(Collectors.toList());
            
            log.info("[关注关系聚合根] 查询粉丝列表成功，返回数量: {}", aggregates.size());
            return aggregates;
        } catch (Exception e) {
            log.error("[关注关系聚合根] 查询粉丝列表失败，被关注者: {}, 页码: {}, 页面大小: {}", 
                     followedId, pageNo, pageSize, e);
            return Collections.emptyList();
        }
    }

    @Override
    public int countFollowing(Long followerId) {
        try {
            log.info("[关注关系聚合根] 开始统计关注数，关注者: {}", followerId);
            
            int count = followDao.countFollowing(followerId);
            log.info("[关注关系聚合根] 统计关注数成功，关注者: {}, 数量: {}", followerId, count);
            return count;
        } catch (Exception e) {
            log.error("[关注关系聚合根] 统计关注数失败，关注者: {}", followerId, e);
            return 0;
        }
    }

    @Override
    public int countFollowers(Long followedId) {
        try {
            log.info("[关注关系聚合根] 开始统计粉丝数，被关注者: {}", followedId);
            
            int count = followDao.countFollowers(followedId);
            log.info("[关注关系聚合根] 统计粉丝数成功，被关注者: {}, 数量: {}", followedId, count);
            return count;
        } catch (Exception e) {
            log.error("[关注关系聚合根] 统计粉丝数失败，被关注者: {}", followedId, e);
            return 0;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        try {
            log.info("[关注关系聚合根] 开始删除关注关系，ID: {}", id);
            
            int result = followDao.deleteById(id);
            log.info("[关注关系聚合根] 删除关注关系成功，ID: {}, 删除记录数: {}", id, result);
        } catch (Exception e) {
            log.error("[关注关系聚合根] 删除关注关系失败，ID: {}", id, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return;
            }
            
            log.info("[关注关系聚合根] 开始批量删除关注关系，数量: {}", ids.size());
            
            int result = followDao.deleteByIds(ids);
            log.info("[关注关系聚合根] 批量删除关注关系成功，删除记录数: {}", result);
        } catch (Exception e) {
            log.error("[关注关系聚合根] 批量删除关注关系失败", e);
            throw e;
        }
    }

    @Override
    public boolean existsByFollowerAndFollowed(Long followerId, Long followedId) {
        try {
            log.info("[关注关系聚合根] 开始检查关注关系是否存在，关注者: {}, 被关注者: {}", followerId, followedId);
            
            Follow follow = followDao.getByFollowerAndFollowed(followerId, followedId);
            boolean exists = follow != null;
            
            log.info("[关注关系聚合根] 检查关注关系是否存在，关注者: {}, 被关注者: {}, 结果: {}", 
                    followerId, followedId, exists);
            return exists;
        } catch (Exception e) {
            log.error("[关注关系聚合根] 检查关注关系是否存在失败，关注者: {}, 被关注者: {}", 
                     followerId, followedId, e);
            return false;
        }
    }

    @Override
    public List<Long> findMutualFollows(Long userId, Integer pageNo, Integer pageSize) {
        try {
            log.info("[关注关系聚合根] 开始查询互相关注，用户ID: {}, 页码: {}, 页面大小: {}", 
                    userId, pageNo, pageSize);
            
            int offset = (pageNo - 1) * pageSize;
            List<Long> mutualFollows = followDao.findMutualFollows(userId, offset, pageSize);
            
            log.info("[关注关系聚合根] 查询互相关注成功，返回数量: {}", mutualFollows.size());
            return mutualFollows;
        } catch (Exception e) {
            log.error("[关注关系聚合根] 查询互相关注失败，用户ID: {}, 页码: {}, 页面大小: {}", 
                     userId, pageNo, pageSize, e);
            return Collections.emptyList();
        }
    }
}