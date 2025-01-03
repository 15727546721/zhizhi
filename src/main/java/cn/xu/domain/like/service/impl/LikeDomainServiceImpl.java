package cn.xu.domain.like.service.impl;

import cn.xu.domain.like.model.Like;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.repository.ILikeRepository;
import cn.xu.domain.like.service.LikeDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeDomainServiceImpl implements LikeDomainService {

    private final ILikeRepository likeRepository;

    @Override
    public boolean like(Long userId, Long targetId, LikeType type) {
        // 检查是否已经点赞
        boolean liked = likeRepository.isLiked(userId, targetId, type);
        if (liked) {
            log.info("用户[{}]已经点赞过{}[{}]", userId, type.getDescription(), targetId);
            return false;
        }

        // 创建点赞记录
        Like like = Like.create(userId, targetId, type);
        likeRepository.save(like);
        return true;
    }

    @Override
    public boolean unlike(Long userId, Long targetId, LikeType type) {
        // 检查是否已经点赞
        boolean liked = likeRepository.isLiked(userId, targetId, type);
        if (!liked) {
            log.info("用户[{}]没有点赞过{}[{}]", userId, type.getDescription(), targetId);
            return false;
        }

        // 创建取消点赞记录
        Like like = Like.create(userId, targetId, type);
        like.cancel();
        likeRepository.save(like);
        return true;
    }

    @Override
    public Long getLikeCount(Long targetId, LikeType type) {
        return likeRepository.getLikeCount(targetId, type);
    }

    @Override
    public boolean isLiked(Long userId, Long targetId, LikeType type) {
        return likeRepository.isLiked(userId, targetId, type);
    }
} 