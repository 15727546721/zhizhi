package cn.xu.domain.like.service.impl;

import cn.xu.common.ResponseCode;
import cn.xu.common.exception.BusinessException;
import cn.xu.domain.like.event.LikeEventPublisher;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.service.ILikeService;
import cn.xu.domain.like.service.LikeDomainService;
import cn.xu.domain.post.model.aggregate.PostAggregate;
import cn.xu.domain.post.repository.IPostRepository;
import cn.xu.infrastructure.persistent.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 点赞应用服务
 * 负责处理应用层逻辑，如参数校验、事务管理等
 */
@Slf4j
@Service
public class LikeService implements ILikeService {

    @Autowired
    private LikeDomainService likeDomainService;
    
    @Autowired
    private LikeEventPublisher likeEventPublisher;
    
    @Autowired
    private IPostRepository postRepository;
    
    @Autowired
    private PostRepository postRepositoryHelper;

    @Override
    public void like(Long userId, LikeType type, Long targetId) {
        checkParams(userId, type, targetId);
        
        try {
            // 先检查当前状态，避免重复操作
            boolean currentStatus = likeDomainService.checkLikeStatus(userId, targetId, type);
            if (currentStatus) {
                log.warn("用户已点赞，无需重复操作 - userId: {}, targetId: {}, type: {}", userId, targetId, type);
                throw new BusinessException("您已点赞，请勿重复操作");
            }
            
            likeDomainService.doLike(userId, targetId, type);
            // 发布点赞事件
            likeEventPublisher.publish(userId, targetId, type, true);
            
            // 如果是帖子点赞，更新帖子统计
            if (type == LikeType.POST && postRepository != null) {
                updatePostLikeCount(targetId, true);
            }
        } catch (BusinessException e) {
            log.error("点赞失败: userId={}, targetId={}, type={}", userId, targetId, type, e);
            // 直接抛出业务异常，让上层处理
            throw e;
        } catch (Exception e) {
            log.error("点赞失败: userId={}, targetId={}, type={}", userId, targetId, type, e);
            throw new BusinessException("点赞失败，请稍后再试！");
        }
    }

    @Override
    public void unlike(Long userId, LikeType type, Long targetId) {
        checkParams(userId, type, targetId);
        
        try {
            // 先检查当前状态，避免重复操作
            boolean currentStatus = likeDomainService.checkLikeStatus(userId, targetId, type);
            if (!currentStatus) {
                log.warn("用户未点赞，无法取消 - userId: {}, targetId: {}, type: {}", userId, targetId, type);
                throw new BusinessException("您尚未点赞，无法取消");
            }
            
            likeDomainService.cancelLike(userId, targetId, type);
            // 发布取消点赞事件
            likeEventPublisher.publish(userId, targetId, type, false);
            
            // 如果是帖子点赞，更新帖子统计
            if (type == LikeType.POST && postRepository != null) {
                updatePostLikeCount(targetId, false);
            }
        } catch (BusinessException e) {
            log.error("取消点赞失败: userId={}, targetId={}, type={}", userId, targetId, type, e);
            // 直接抛出业务异常，让上层处理
            throw e;
        } catch (Exception e) {
            log.error("取消点赞失败: userId={}, targetId={}, type={}", userId, targetId, type, e);
            throw new BusinessException("取消点赞失败，请稍后再试！");
        }
    }

    @Override
    public boolean checkStatus(Long userId, LikeType type, Long targetId) {
        if (userId == null || type == null || targetId == null) {
            // 未登录用户默认未点赞
            return false;
        }
        return likeDomainService.checkLikeStatus(userId, targetId, type);
    }
    
    @Override
    public long getLikeCount(Long targetId, LikeType type) {
        return likeDomainService.getLikeCount(targetId, type);
    }
    
    @Override
    public void checkAndRepairLikeConsistency(Long userId, Long targetId, LikeType type) {
        likeDomainService.checkAndRepairLikeConsistency(userId, targetId, type);
    }

    /**
     * 校验入参
     */
    private void checkParams(Long userId, LikeType type, Long targetId) {
        if (userId == null || type == null || targetId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), ResponseCode.NULL_PARAMETER.getMessage());
        }
    }
    
    /**
     * 更新帖子点赞数
     * 
     * @param postId 帖子ID
     * @param isIncrease 是否增加
     */
    private void updatePostLikeCount(Long postId, boolean isIncrease) {
        try {
            // 使用辅助类直接更新点赞数，确保点赞数正确更新到数据库
            long currentCount = likeDomainService.getLikeCount(postId, LikeType.POST);
            postRepositoryHelper.updatePostLikeCount(postId, currentCount);
            
            log.info("帖子点赞数更新成功: postId={}, 新点赞数={}", postId, currentCount);
        } catch (Exception e) {
            log.error("更新帖子点赞数失败: postId={}, isIncrease={}", postId, isIncrease, e);
        }
    }
}