package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.like.model.Like;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.repository.ILikeRepository;
import cn.xu.infrastructure.persistent.dao.ILikeDao;
import cn.xu.infrastructure.persistent.po.LikePO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MySQL点赞仓储实现 - 只存储点赞关系，不存储点赞数量
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class LikeRepository implements ILikeRepository {

    private final ILikeDao likeDao;

    @Override
    public void save(Like like) {
        try {
            LikePO po = convertToPO(like);
            if (like.isLiked()) {
                // 如果记录不存在则插入，存在则更新
                LikePO existingPO = likeDao.findByUserIdAndTargetIdAndType(
                        like.getUserId(), like.getTargetId(), like.getType().getCode());
                if (existingPO == null) {
                    po.setCreateTime(LocalDateTime.now());
                    po.setUpdateTime(LocalDateTime.now());
                    po.setStatus(1);
                    likeDao.insert(po);
                } else {
                    po.setId(existingPO.getId());
                    po.setUpdateTime(LocalDateTime.now());
                    po.setStatus(1);
                    likeDao.update(po);
                }
            } else {
                // 更新状态为取消点赞
                po.setUpdateTime(LocalDateTime.now());
                po.setStatus(0);
                likeDao.update(po);
            }
            log.info("MySQL保存点赞记录成功: {}", like);
        } catch (Exception e) {
            log.error("MySQL保存点赞记录失败: {}", e.getMessage());
            throw new RuntimeException("MySQL保存点赞记录失败", e);
        }
    }

    @Override
    public Long getLikeCount(Long targetId, LikeType type) {
        try {
            return likeDao.countByTargetIdAndType(targetId, type.getCode());
        } catch (Exception e) {
            log.error("MySQL获取点赞数量失败: targetId={}, type={}, error={}", 
                    targetId, type, e.getMessage());
            return 0L;
        }
    }

    @Override
    public boolean isLiked(Long userId, Long targetId, LikeType type) {
        try {
            LikePO po = likeDao.findByUserIdAndTargetIdAndType(userId, targetId, type.getCode());
            return po != null && po.getStatus() == 1;
        } catch (Exception e) {
            log.error("MySQL获取点赞状态失败: userId={}, targetId={}, type={}, error={}", 
                    userId, targetId, type, e.getMessage());
            return false;
        }
    }

    @Override
    public void delete(Long userId, Long targetId, LikeType type) {
        try {
            LikePO po = likeDao.findByUserIdAndTargetIdAndType(userId, targetId, type.getCode());
            if (po != null) {
                po.setStatus(0);
                po.setUpdateTime(LocalDateTime.now());
                likeDao.update(po);
                log.info("MySQL删除点赞记录成功: userId={}, targetId={}, type={}", 
                        userId, targetId, type);
            }
        } catch (Exception e) {
            log.error("MySQL删除点赞记录失败: userId={}, targetId={}, type={}, error={}", 
                    userId, targetId, type, e.getMessage());
            throw new RuntimeException("MySQL删除点赞记录失败", e);
        }
    }

    @Override
    public Set<Long> getLikedUserIds(Long targetId, LikeType type) {
        try {
            return likeDao.getLikedUserIds(targetId, type.getCode());
        } catch (Exception e) {
            log.error("MySQL获取点赞用户ID列表失败: targetId={}, type={}, error={}", 
                    targetId, type, e.getMessage());
            return null;
        }
    }

    @Override
    public Set<Like> getPageByType(LikeType type, Integer offset, Integer limit) {
        try {
            Set<LikePO> pos = likeDao.getPageByType(type.getCode(), offset, limit);
            return pos.stream()
                    .map(this::convertToDomain)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("MySQL分页获取点赞记录失败: type={}, offset={}, limit={}, error={}", 
                    type, offset, limit, e.getMessage());
            return null;
        }
    }

    @Override
    public Long countByType(LikeType type) {
        try {
            return likeDao.countByType(type.getCode());
        } catch (Exception e) {
            log.error("MySQL获取点赞记录总数失败: type={}, error={}", type, e.getMessage());
            return 0L;
        }
    }

    @Override
    public void syncToCache(Long targetId, LikeType type) {
        // MySQL不需要实现缓存同步
        log.debug("MySQL不需要实现缓存同步操作");
    }

    @Override
    public void cleanExpiredCache() {
        // MySQL不需要实现缓存清理
        log.debug("MySQL不需要实现缓存清理操作");
    }

    private LikePO convertToPO(Like like) {
        LikePO po = new LikePO();
        po.setUserId(like.getUserId());
        po.setTargetId(like.getTargetId());
        po.setType(like.getType().getCode());
        po.setStatus(like.isLiked() ? 1 : 0);
        
        // 如果是新记录，设置创建时间和更新时间
        if (like.getId() == null) {
            po.setCreateTime(LocalDateTime.now());
            po.setUpdateTime(LocalDateTime.now());
        } else {
            po.setId(like.getId());
            po.setUpdateTime(LocalDateTime.now());
        }
        
        return po;
    }

    private Like convertToDomain(LikePO po) {
        // 创建Like对象，注意：这里会默认设置liked=true和当前时间
        Like like = Like.create(po.getUserId(), po.getTargetId(), LikeType.fromCode(po.getType()));
        
        // 设置ID
        like.setId(po.getId());
        
        // 如果数据库中的状态是未点赞，需要调用cancel方法
        if (po.getStatus() == 0) {
            like.cancel();
        }
        
        return like;
    }
}
