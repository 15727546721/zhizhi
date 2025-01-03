package cn.xu.domain.like.service.impl;

import cn.xu.domain.like.command.LikeCommand;
import cn.xu.domain.like.event.LikeEvent;
import cn.xu.domain.like.model.Like;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.repository.ILikeRepository;
import cn.xu.domain.like.service.LikeDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 点赞领域服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeDomainServiceImpl implements LikeDomainService {

    private final ILikeRepository likeRepository;

    @Override
    public LikeEvent handleLike(LikeCommand command) {
        // 参数校验
        validateCommand(command);

        // 检查是否已点赞
        if (likeRepository.isLiked(command.getUserId(), command.getTargetId(), command.getType())) {
            log.info("用户[{}]已经点赞过{}[{}]", 
                    command.getUserId(), command.getType().getDescription(), command.getTargetId());
            return null;
        }

        // 创建点赞实体
        Like like = Like.builder()
                .userId(command.getUserId())
                .targetId(command.getTargetId())
                .type(command.getType())
                .createTime(LocalDateTime.now())
                .build();
        like.like();
        
        // 保存点赞记录
        likeRepository.save(like);
        
        // 创建并返回点赞事件
        return LikeEvent.builder()
                .userId(command.getUserId())
                .targetId(command.getTargetId())
                .type(command.getType())
                .liked(true)
                .occurredTime(LocalDateTime.now())
                .build();
    }

    @Override
    public LikeEvent handleUnlike(LikeCommand command) {
        // 参数校验
        validateCommand(command);

        // 检查是否已点赞
        if (!likeRepository.isLiked(command.getUserId(), command.getTargetId(), command.getType())) {
            log.info("用户[{}]没有点赞过{}[{}]", 
                    command.getUserId(), command.getType().getDescription(), command.getTargetId());
            return null;
        }

        // 创建点赞实体
        Like like = Like.builder()
                .userId(command.getUserId())
                .targetId(command.getTargetId())
                .type(command.getType())
                .createTime(LocalDateTime.now())
                .build();
        like.unlike();
        
        // 删除点赞记录
        likeRepository.delete(command.getUserId(), command.getTargetId(), command.getType());
        
        // 创建并返回取消点赞事件
        return LikeEvent.builder()
                .userId(command.getUserId())
                .targetId(command.getTargetId())
                .type(command.getType())
                .liked(false)
                .occurredTime(LocalDateTime.now())
                .build();
    }

    @Override
    public Long getLikeCount(Long targetId, LikeType type) {
        return likeRepository.getLikeCount(targetId, type);
    }

    @Override
    public boolean isLiked(Long userId, Long targetId, LikeType type) {
        return likeRepository.isLiked(userId, targetId, type);
    }

    private void validateCommand(LikeCommand command) {
        if (command.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (command.getTargetId() == null) {
            throw new IllegalArgumentException("目标ID不能为空");
        }
        if (command.getType() == null) {
            throw new IllegalArgumentException("点赞类型不能为空");
        }
    }
} 