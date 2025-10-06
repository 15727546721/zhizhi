package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.cache.ICacheService;
import cn.xu.domain.like.model.LikeEntity;
import cn.xu.domain.like.model.LikeStatus;
import cn.xu.domain.like.model.LikeType;
import cn.xu.infrastructure.cache.LikeCacheRepository;
import cn.xu.infrastructure.cache.RedisKeyManager;
import cn.xu.infrastructure.persistent.converter.LikeConverter;
import cn.xu.infrastructure.persistent.dao.LikeMapper;
import cn.xu.infrastructure.persistent.po.Like;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * MySQL点赞仓储实现 - 只存储点赞关系，不存储点赞数量
 * @deprecated 请使用LikeAggregateRepositoryImpl替代
 */
@Slf4j
@Repository
@RequiredArgsConstructor
@Deprecated
public class LikeRepository implements cn.xu.domain.like.repository.ILikeRepository {

    private final LikeMapper likeDao; // MySQL操作类
    private final ICacheService cacheService; // 使用领域层缓存服务接口
    private final LikeCacheRepository likeCacheRepository; // 点赞缓存仓储
    private final LikeConverter likeConverter; // 领域实体与持久化对象转换器

    /**
     * 保存点赞记录
     */
    @Override
    public void saveLike(LikeEntity likeEntity) {
        // 保存记录
        Like like = likeConverter.toDataObject(likeEntity);
        if (like.getId() == null) {
            likeDao.save(like);
            likeEntity.setId(like.getId());
        } else {
            likeDao.save(like); // 使用save方法更新记录
        }

        // 同步更新 Redis 点赞数和用户点赞关系
        if (likeEntity.isLiked()) {
            incrementLikeCount(likeEntity.getTargetId(), likeEntity.getType());
            likeCacheRepository.addUserLikeRelation(
                likeEntity.getUserId(), 
                likeEntity.getTargetId(), 
                likeEntity.getType()
            );
        } else {
            decrementLikeCount(likeEntity.getTargetId(), likeEntity.getType());
            likeCacheRepository.removeUserLikeRelation(
                likeEntity.getUserId(), 
                likeEntity.getTargetId(), 
                likeEntity.getType()
            );
        }
    }

    /**
     * 取消点赞操作
     */
    @Override
    public void remove(Long userId, Long targetId, LikeType type) {
        // 1. 查找已点赞的记录
        Like like = likeDao.findByUserIdAndTypeAndTargetId(userId, type.getCode(), targetId);
        if (like == null) {
            log.warn("[取消点赞] 找不到点赞记录 - userId: {}, targetId: {}", userId, targetId);
            return;
        }

        // 2. 点赞记录，更新状态为"已取消点赞"
        like.setStatus(LikeStatus.UNLIKED.getCode());
        likeDao.save(like); // 使用save方法更新记录

        // 3. 减少 Redis 中的点赞数
        decrementLikeCount(targetId, type);
        
        // 4. 移除用户点赞关系缓存
        likeCacheRepository.removeUserLikeRelation(userId, targetId, type);
    }

    /**
     * 更新点赞状态
     */
    @Override
    public void updateStatus(Long userId, LikeType type, Long targetId, Integer status) {
        // 1. 更新状态
        likeDao.updateStatus(userId, type.getCode(), targetId, status);

        // 2. 点赞状态变更时同步更新 Redis 点赞数
        if (status == 1) {
            incrementLikeCount(targetId, type);
            likeCacheRepository.addUserLikeRelation(userId, targetId, type);
        } else if (status == 0) {
            decrementLikeCount(targetId, type);
            likeCacheRepository.removeUserLikeRelation(userId, targetId, type);
        }
    }

    /**
     * 根据用户ID、目标类型和目标ID查询点赞记录
     */
    @Override
    public LikeEntity findByUserIdAndTypeAndTargetId(Long userId, LikeType type, Long targetId) {
        Like like = likeDao.findByUserIdAndTypeAndTargetId(userId, type.getCode(), targetId);
        return likeConverter.toDomainEntity(like);
    }

    /**
     * 检查用户是否点赞某个目标
     */
    @Override
    public boolean checkStatus(Long userId, LikeType type, Long targetId) {
        // 优先从缓存检查用户点赞关系
        boolean cachedResult = likeCacheRepository.checkUserLikeRelation(userId, targetId, type);
        if (cachedResult) {
            return true;
        }
        
        // 缓存未命中，从数据库检查
        Integer status = likeDao.checkStatus(userId, type.getCode(), targetId);
        boolean dbResult = status != null && status == 1;
        
        // 如果数据库中有点赞记录，同步到缓存
        if (dbResult) {
            likeCacheRepository.addUserLikeRelation(userId, targetId, type);
        }
        
        return dbResult;
    }

    /**
     * 增加Redis中的点赞数
     */
    @Override
    public void incrementLikeCount(Long targetId, LikeType type) {
        cacheService.incrementCount(RedisKeyManager.likeCountKey(type, targetId), 1);
        likeCacheRepository.incrementLikeCount(targetId, type, 1);
    }

    /**
     * 减少Redis中的点赞数
     */
    @Override
    public void decrementLikeCount(Long targetId, LikeType type) {
        cacheService.incrementCount(RedisKeyManager.likeCountKey(type, targetId), -1);
        likeCacheRepository.incrementLikeCount(targetId, type, -1);
    }

}