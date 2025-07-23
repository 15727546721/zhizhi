package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.like.model.LikeEntity;
import cn.xu.domain.like.repository.ILikeRepository;
import cn.xu.infrastructure.persistent.dao.LikeMapper;
import cn.xu.infrastructure.persistent.po.Like;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * MySQL点赞仓储实现 - 只存储点赞关系，不存储点赞数量
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class LikeRepository implements ILikeRepository {

    private final LikeMapper likeDao;

    @Override
    public void save(LikeEntity likeEntity) {
        likeDao.save(convertToLike(likeEntity));
    }

    @Override
    public void updateStatus(Long userId, Integer type, Long targetId, Integer status) {
        likeDao.updateStatus(userId, type, targetId, status);
    }

    @Override
    public Like findByUserIdAndTypeAndTargetId(Long userId, int type, Long targetId) {
        return likeDao.findByUserIdAndTypeAndTargetId(userId, type, targetId);
    }

    @Override
    public boolean checkStatus(Long userId, Integer type, Long targetId) {
        Integer status = likeDao.checkStatus(userId, type, targetId);
        if (status == null || status == 0) {
            return false;
        }
        return true;
    }

    private Like convertToLike(LikeEntity likeEntity) {
        return Like.builder()
                .id(likeEntity.getId())
                .userId(likeEntity.getUserId())
                .targetId(likeEntity.getTargetId())
                .type(likeEntity.getType())
                .status(likeEntity.getStatus())
                .createTime(likeEntity.getCreateTime())
                .build();
    }
}
