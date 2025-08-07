package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.like.model.LikeEntity;
import cn.xu.domain.like.repository.ILikeRepository;
import cn.xu.infrastructure.common.utils.RedisKeys;
import cn.xu.infrastructure.persistent.dao.LikeMapper;
import cn.xu.infrastructure.persistent.po.Like;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MySQL点赞仓储实现 - 只存储点赞关系，不存储点赞数量
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class LikeRepository implements ILikeRepository {

    private final LikeMapper likeDao; // MySQL操作类
    private final RedisTemplate<String, Object> redisTemplate; // Redis操作类

    /**
     * 保存点赞记录，若用户已经点赞则更新状态为“已点赞”
     * @param userId 用户ID
     * @param targetId 目标ID（例如文章ID）
     * @param type 类型（例如文章、评论等）
     */
    @Override
    public void saveLike(long userId, long targetId, int type) {
        // 1. 判断用户是否已经点赞
        Like existingLike = likeDao.findByUserIdAndTypeAndTargetId(userId, type, targetId);

        if (existingLike == null) {
            // 2. 用户没有点赞，插入新记录
            Like like = Like.builder()
                    .userId(userId)
                    .targetId(targetId)
                    .type(type)
                    .createTime(LocalDateTime.now())
                    .status(1)  // 初始状态为已点赞
                    .build();
            likeDao.save(like);

            // 3. 增加 Redis 中的点赞数
            incrementLikeCount(targetId, type);
        } else if (existingLike.getStatus() == 0) {
            // 4. 已取消点赞，则更新为已点赞
            existingLike.setStatus(1);
            likeDao.updateStatus(userId, type, targetId, 1);

            // 5. 增加 Redis 中的点赞数
            incrementLikeCount(targetId, type);
        } else {
            log.warn("[点赞操作] 用户已点赞，无需重复操作 - userId: {}, targetId: {}", userId, targetId);
        }
    }

    /**
     * 取消点赞操作，更新状态为“已取消点赞”
     * @param targetId 目标ID（例如文章ID）
     * @param type 类型（例如文章、评论等）
     */
    @Override
    public void remove(long userId, long targetId, int type) {
        // 1. 查找已点赞的记录
        Like like = likeDao.findByUserIdAndTypeAndTargetId(userId, type, targetId);

        // 2. 点赞记录，更新状态为“已取消点赞”
        like.setStatus(0);
        likeDao.updateStatus(like.getUserId(), type, targetId, 0);

        // 3. 减少 Redis 中的点赞数
        decrementLikeCount(targetId, type);
    }

    /**
     * 更新点赞状态，例如从已点赞更新为已取消点赞
     * @param userId 用户ID
     * @param type 类型（例如文章、评论等）
     * @param targetId 目标ID（例如文章ID）
     * @param status 点赞状态
     */
    @Override
    public void updateStatus(Long userId, Integer type, Long targetId, Integer status) {
        // 1. 更新状态
        likeDao.updateStatus(userId, type, targetId, status);

        // 2. 点赞状态变更时同步更新 Redis 点赞数
        if (status == 1) {
            incrementLikeCount(targetId, type);
        } else if (status == 0) {
            decrementLikeCount(targetId, type);
        }
    }

    /**
     * 根据用户ID、目标类型和目标ID查询点赞记录
     * @param userId 用户ID
     * @param type 类型（例如文章、评论等）
     * @param targetId 目标ID（例如文章ID）
     * @return 点赞记录
     */
    @Override
    public Like findByUserIdAndTypeAndTargetId(Long userId, int type, Long targetId) {
        return likeDao.findByUserIdAndTypeAndTargetId(userId, type, targetId);
    }

    /**
     * 检查用户是否点赞某个目标
     * @param userId 用户ID
     * @param type 类型（例如文章、评论等）
     * @param targetId 目标ID（例如文章ID）
     * @return 是否点赞
     */
    @Override
    public boolean checkStatus(Long userId, Integer type, Long targetId) {
        Integer status = likeDao.checkStatus(userId, type, targetId);
        return status != null && status == 1;
    }

    /**
     * 增加Redis中的点赞数
     * @param targetId 目标ID（例如文章ID）
     * @param type 类型（例如文章、评论等）
     */
    @Override
    public void incrementLikeCount(Long targetId, int type) {
        redisTemplate.opsForHash().increment(RedisKeys.likeCountKey(type), String.valueOf(targetId), 1);
    }

    /**
     * 减少Redis中的点赞数
     * @param targetId 目标ID（例如文章ID）
     * @param type 类型（例如文章、评论等）
     */
    @Override
    public void decrementLikeCount(Long targetId, int type) {
        redisTemplate.opsForHash().increment(RedisKeys.likeCountKey(type), String.valueOf(targetId), -1);
    }

    /**
     * 将LikeEntity转换为Like对象
     * @param likeEntity LikeEntity对象
     * @return Like对象
     */
    private Like convertToLike(LikeEntity likeEntity) {
        return Like.builder()
                .id(likeEntity.getId())
                .userId(likeEntity.getUserId())
                .targetId(likeEntity.getTargetId())
                .type(likeEntity.getType())
                .createTime(likeEntity.getCreateTime())
                .build();
    }
}
