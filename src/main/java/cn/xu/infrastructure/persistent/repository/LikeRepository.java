package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.like.model.LikeEntity;
import cn.xu.domain.like.repository.ILikeRepository;
import cn.xu.infrastructure.persistent.dao.ILikeDao;
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

    private final ILikeDao likeDao;

    @Override
    public void save(LikeEntity likeEntity) {
        likeDao.insert(convertToLike(likeEntity));
    }

    @Override
    public Like findByUserIdAndTypeAndTargetId(Long userId, int type, Long targetId) {
        return likeDao.findByUserIdAndTargetIdAndType(userId, type, targetId);
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
