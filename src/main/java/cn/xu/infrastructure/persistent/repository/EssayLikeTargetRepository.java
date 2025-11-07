package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.repository.LikeTargetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 随笔点赞目标仓储实现
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class EssayLikeTargetRepository implements LikeTargetRepository {
    
    private final EssayRepository essayRepository;
    
    @Override
    public void updateLikeCount(Long targetId, Long increment) {
        try {
            essayRepository.updateLikeCount(targetId, increment.intValue());
            log.info("[随笔点赞目标仓储] 更新随笔点赞数成功: essayId={}, 增量={}", targetId, increment);
        } catch (Exception e) {
            log.error("[随笔点赞目标仓储] 更新随笔点赞数失败: essayId={}, 增量={}", targetId, increment, e);
            throw e;
        }
    }
    
    @Override
    public boolean supports(LikeType likeType) {
        return likeType != null && likeType.isEssay();
    }
}

