package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.repository.LikeTargetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 帖子点赞目标仓储实现
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PostLikeTargetRepository implements LikeTargetRepository {
    
    private final PostRepository postRepository;
    
    @Override
    public void updateLikeCount(Long targetId, Long increment) {
        try {
            postRepository.updatePostLikeCount(targetId, increment);
            log.info("[帖子点赞目标仓储] 更新帖子点赞数成功: postId={}, 增量={}", targetId, increment);
        } catch (Exception e) {
            log.error("[帖子点赞目标仓储] 更新帖子点赞数失败: postId={}, 增量={}", targetId, increment, e);
            throw e;
        }
    }
    
    @Override
    public boolean supports(LikeType likeType) {
        return likeType != null && likeType.isPost();
    }
}

