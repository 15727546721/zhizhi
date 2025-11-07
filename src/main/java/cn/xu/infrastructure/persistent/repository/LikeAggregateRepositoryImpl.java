package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.like.model.LikeStatus;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.model.aggregate.LikeAggregate;
import cn.xu.domain.like.repository.ILikeAggregateRepository;
import cn.xu.infrastructure.persistent.converter.LikeConverter;
import cn.xu.infrastructure.persistent.dao.LikeMapper;
import cn.xu.infrastructure.persistent.po.Like;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 点赞聚合根仓储实现类
 * 通过Converter进行领域实体与持久化对象的转换，遵循DDD防腐层模式
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class LikeAggregateRepositoryImpl implements ILikeAggregateRepository {
    
    private final LikeMapper likeDao;
    private final LikeConverter likeConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(LikeAggregate aggregate) {
        try {
            log.info("[点赞聚合根] 开始保存点赞聚合根");
            
            // 保存点赞实体
            Like likePO = likeConverter.toDataObjectFromAggregate(aggregate);
            
            if (likePO.getId() == null) {
                likeDao.save(likePO);
                aggregate.setId(likePO.getId());
                log.info("[点赞聚合根] 新增点赞成功，ID: {}", likePO.getId());
            } else {
                likeDao.update(likePO); // 使用update方法更新记录
                log.info("[点赞聚合根] 更新点赞成功，ID: {}", likePO.getId());
            }
            
            return likePO.getId();
        } catch (Exception e) {
            log.error("[点赞聚合根] 保存点赞聚合根失败", e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(LikeAggregate aggregate) {
        try {
            log.info("[点赞聚合根] 开始更新点赞聚合根，ID: {}", aggregate.getId());
            
            // 更新点赞实体
            Like likePO = likeConverter.toDataObjectFromAggregate(aggregate);
            likeDao.update(likePO); // 使用update方法更新记录
            
            log.info("[点赞聚合根] 更新点赞聚合根成功");
        } catch (Exception e) {
            log.error("[点赞聚合根] 更新点赞聚合根失败，ID: {}", aggregate.getId(), e);
            throw e;
        }
    }

    @Override
    public Optional<LikeAggregate> findById(Long id) {
        try {
            log.info("[点赞聚合根] 开始查询点赞聚合根，ID: {}", id);
            
            // 查询点赞关系
            Like like = likeDao.findById(id);
            if (like == null) {
                log.info("[点赞聚合根] 点赞关系不存在，ID: {}", id);
                return Optional.empty();
            }
            
            LikeAggregate aggregate = LikeAggregate.restore(
                like.getId(),
                like.getUserId(),
                like.getTargetId(),
                like.getType() != null ? LikeType.valueOf(like.getType()) : null,
                like.getStatus() != null ? LikeStatus.valueOf(like.getStatus()) : null,
                like.getCreateTime(),
                like.getCreateTime() // 使用创建时间作为更新时间
            );
            
            log.info("[点赞聚合根] 查询点赞聚合根成功");
            return Optional.of(aggregate);
        } catch (Exception e) {
            log.error("[点赞聚合根] 查询点赞聚合根失败，ID: {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<LikeAggregate> findByUserAndTarget(Long userId, LikeType type, Long targetId) {
        try {
            log.info("[点赞聚合根] 开始根据用户和目标查询点赞聚合根，用户: {}, 类型: {}, 目标: {}", 
                    userId, type, targetId);
            
            // 查询点赞关系
            Like like = likeDao.findByUserIdAndTypeAndTargetId(userId, type.getCode(), targetId);
            if (like == null) {
                log.info("[点赞聚合根] 点赞关系不存在，用户: {}, 类型: {}, 目标: {}", userId, type, targetId);
                return Optional.empty();
            }
            
            LikeAggregate aggregate = LikeAggregate.restore(
                like.getId(),
                like.getUserId(),
                like.getTargetId(),
                like.getType() != null ? LikeType.valueOf(like.getType()) : null,
                like.getStatus() != null ? LikeStatus.valueOf(like.getStatus()) : null,
                like.getCreateTime(),
                like.getCreateTime() // 使用创建时间作为更新时间
            );
            
            log.info("[点赞聚合根] 查询点赞聚合根成功");
            return Optional.of(aggregate);
        } catch (Exception e) {
            log.error("[点赞聚合根] 根据用户和目标查询点赞聚合根失败，用户: {}, 类型: {}, 目标: {}", 
                     userId, type, targetId, e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        try {
            log.info("[点赞聚合根] 开始删除点赞关系，ID: {}", id);
            
            likeDao.deleteById(id);
            log.info("[点赞聚合根] 删除点赞关系成功，ID: {}", id);
        } catch (Exception e) {
            log.error("[点赞聚合根] 删除点赞关系失败，ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public boolean existsByUserAndTarget(Long userId, LikeType type, Long targetId) {
        try {
            log.info("[点赞聚合根] 开始检查点赞关系是否存在，用户: {}, 类型: {}, 目标: {}", userId, type, targetId);
            
            Like like = likeDao.findByUserIdAndTypeAndTargetId(userId, type.getCode(), targetId);
            boolean exists = like != null;
            
            log.info("[点赞聚合根] 检查点赞关系是否存在，用户: {}, 类型: {}, 目标: {}, 结果: {}", 
                    userId, type, targetId, exists);
            return exists;
        } catch (Exception e) {
            log.error("[点赞聚合根] 检查点赞关系是否存在失败，用户: {}, 类型: {}, 目标: {}", 
                     userId, type, targetId, e);
            return false;
        }
    }

    @Override
    public long countByTarget(Long targetId, LikeType type) {
        try {
            log.info("[点赞聚合根] 开始统计目标点赞数，目标: {}, 类型: {}", targetId, type);
            
            long count = likeDao.countByTargetIdAndType(targetId, type.getCode());
            log.info("[点赞聚合根] 统计目标点赞数成功，目标: {}, 类型: {}, 数量: {}", targetId, type, count);
            return count;
        } catch (Exception e) {
            log.error("[点赞聚合根] 统计目标点赞数失败，目标: {}, 类型: {}", targetId, type, e);
            return 0;
        }
    }
    
    @Override
    public List<Long> findLikedTargetIdsByUser(Long userId, LikeType type) {
        try {
            log.info("[点赞聚合根] 开始获取用户点赞的目标ID列表，用户: {}, 类型: {}", userId, type);
            
            if (userId == null || type == null) {
                return new ArrayList<>();
            }
            
            List<Long> targetIds = likeDao.selectLikedTargetIdsByUserId(userId, type.getCode());
            log.info("[点赞聚合根] 获取用户点赞的目标ID列表成功，用户: {}, 类型: {}, 数量: {}", 
                    userId, type, targetIds != null ? targetIds.size() : 0);
            return targetIds != null ? targetIds : new ArrayList<>();
        } catch (Exception e) {
            log.error("[点赞聚合根] 获取用户点赞的目标ID列表失败，用户: {}, 类型: {}", userId, type, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Long> findUserIdsByTarget(Long targetId, LikeType type) {
        try {
            log.info("[点赞聚合根] 开始获取点赞目标的用户ID列表，目标: {}, 类型: {}", targetId, type);
            
            if (targetId == null || type == null) {
                return new ArrayList<>();
            }
            
            List<Long> userIds = likeDao.selectUserIdsByTargetId(targetId, type.getCode());
            log.info("[点赞聚合根] 获取点赞目标的用户ID列表成功，目标: {}, 类型: {}, 数量: {}", 
                    targetId, type, userIds != null ? userIds.size() : 0);
            return userIds != null ? userIds : new ArrayList<>();
        } catch (Exception e) {
            log.error("[点赞聚合根] 获取点赞目标的用户ID列表失败，目标: {}, 类型: {}", targetId, type, e);
            return new ArrayList<>();
        }
    }
}