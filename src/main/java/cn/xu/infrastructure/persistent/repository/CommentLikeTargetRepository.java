package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.repository.LikeTargetRepository;
import cn.xu.infrastructure.persistent.dao.CommentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 评论点赞目标仓储实现
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CommentLikeTargetRepository implements LikeTargetRepository {
    
    private final CommentMapper commentMapper;
    
    @Override
    public void updateLikeCount(Long targetId, Long increment) {
        try {
            // targetId在这里就是commentId（评论ID）
            commentMapper.updateLikeCount(targetId, increment.intValue());
            log.info("[评论点赞目标仓储] 更新评论点赞数成功: commentId={}, 增量={}", targetId, increment);
        } catch (Exception e) {
            log.error("[评论点赞目标仓储] 更新评论点赞数失败: commentId={}, 增量={}", targetId, increment, e);
            throw e;
        }
    }
    
    @Override
    public boolean supports(LikeType likeType) {
        return likeType != null && likeType.isComment();
    }
}

