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
import java.util.Map;

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
        } catch (Exception e) {
            log.error("保存点赞记录失败: {}", e.getMessage());
            throw new RuntimeException("保存点赞记录失败", e);
        }
    }

    @Override
    public Long getLikeCount(Long targetId, LikeType type) {
        try {
            return likeDao.countByTargetIdAndType(targetId, type.getCode());
        } catch (Exception e) {
            log.error("获取点赞数量失败: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    public boolean isLiked(Long userId, Long targetId, LikeType type) {
        try {
            LikePO po = likeDao.findByUserIdAndTargetIdAndType(userId, targetId, type.getCode());
            return po != null && po.getStatus() == 1;
        } catch (Exception e) {
            log.error("获取点赞状态失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void batchUpdateLikeCount(Map<String, Long> likeCounts) {
        // MySQL不再存储和更新点赞数量，此方法不再需要实现
        log.debug("MySQL不存储点赞数量，忽略批量更新操作");
    }

    @Override
    public void delete(Long userId, Long targetId, LikeType type) {
        try {
            LikePO po = new LikePO();
            po.setUserId(userId);
            po.setTargetId(targetId);
            po.setType(type.getCode());
            po.setStatus(0);
            po.setUpdateTime(LocalDateTime.now());
            likeDao.update(po);
        } catch (Exception e) {
            log.error("删除点赞记录失败: {}", e.getMessage());
            throw new RuntimeException("删除点赞记录失败", e);
        }
    }

    private LikePO convertToPO(Like like) {
        LikePO po = new LikePO();
        po.setUserId(like.getUserId());
        po.setTargetId(like.getTargetId());
        po.setType(like.getType().getCode());
        po.setStatus(like.isLiked() ? 1 : 0);
        po.setCreateTime(like.getCreateTime());
        po.setUpdateTime(like.getUpdateTime());
        if (like.getId() != null) {
            po.setId(like.getId());
        }
        return po;
    }
}
