package cn.xu.domain.follow.service;

import cn.xu.domain.follow.model.aggregate.FollowAggregate;
import cn.xu.domain.follow.repository.IFollowAggregateRepository;
import cn.xu.domain.user.model.aggregate.UserAggregate;
import cn.xu.domain.user.repository.IUserAggregateRepository;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 关注管理领域服务
 * 负责处理关注关系的核心业务逻辑
 */
@Service
@RequiredArgsConstructor
public class FollowManagementDomainService {
    
    private static final Logger log = LoggerFactory.getLogger(FollowManagementDomainService.class);
    
    private final IFollowAggregateRepository followAggregateRepository;
    private final IUserAggregateRepository userAggregateRepository;
    
    /**
     * 关注用户
     * 
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void followUser(Long followerId, Long followedId) {
        log.info("[关注管理] 开始关注用户 - 关注者: {}, 被关注者: {}", followerId, followedId);
        
        try {
            // 验证参数
            validateFollowParams(followerId, followedId);
            
            // 检查关注关系是否已存在
            if (followAggregateRepository.existsByFollowerAndFollowed(followerId, followedId)) {
                FollowAggregate existingFollow = followAggregateRepository
                        .findByFollowerAndFollowed(followerId, followedId)
                        .orElseThrow(() -> new BusinessException("关注关系不存在"));
                
                // 如果已经是关注状态，直接返回
                if (existingFollow.isFollowed()) {
                    log.info("[关注管理] 关注关系已存在且为关注状态，无需重复关注");
                    return;
                }
                
                // 如果是取消关注状态，重新关注
                existingFollow.follow();
                followAggregateRepository.update(existingFollow);
                
                // 发布领域事件
                publishDomainEvents(existingFollow);
            } else {
                // 创建新的关注关系
                FollowAggregate newFollow = FollowAggregate.create(followerId, followedId);
                followAggregateRepository.save(newFollow);
                
                // 发布领域事件
                publishDomainEvents(newFollow);
            }
            
            // 更新用户关注数和粉丝数
            updateUserFollowCounts(followerId, followedId, true);
            
            log.info("[关注管理] 关注用户成功 - 关注者: {}, 被关注者: {}", followerId, followedId);
        } catch (Exception e) {
            log.error("[关注管理] 关注用户失败 - 关注者: {}, 被关注者: {}", followerId, followedId, e);
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException("关注用户失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 取消关注用户
     * 
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void unfollowUser(Long followerId, Long followedId) {
        log.info("[关注管理] 开始取消关注用户 - 关注者: {}, 被关注者: {}", followerId, followedId);
        
        try {
            // 验证参数
            validateFollowParams(followerId, followedId);
            
            // 检查关注关系是否存在
            FollowAggregate existingFollow = followAggregateRepository
                    .findByFollowerAndFollowed(followerId, followedId)
                    .orElseThrow(() -> new BusinessException("关注关系不存在"));
            
            // 如果已经是取消关注状态，直接返回
            if (!existingFollow.isFollowed()) {
                log.info("[关注管理] 关注关系已为取消关注状态，无需重复取消");
                return;
            }
            
            // 取消关注
            existingFollow.unfollow();
            followAggregateRepository.update(existingFollow);
            
            // 发布领域事件
            publishDomainEvents(existingFollow);
            
            // 更新用户关注数和粉丝数
            updateUserFollowCounts(followerId, followedId, false);
            
            log.info("[关注管理] 取消关注用户成功 - 关注者: {}, 被关注者: {}", followerId, followedId);
        } catch (Exception e) {
            log.error("[关注管理] 取消关注用户失败 - 关注者: {}, 被关注者: {}", followerId, followedId, e);
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException("取消关注用户失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 获取关注状态
     * 
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     * @return 是否已关注
     */
    public boolean isFollowing(Long followerId, Long followedId) {
        try {
            return followAggregateRepository
                    .findByFollowerAndFollowed(followerId, followedId)
                    .map(FollowAggregate::isFollowed)
                    .orElse(false);
        } catch (Exception e) {
            log.error("[关注管理] 获取关注状态失败 - 关注者: {}, 被关注者: {}", followerId, followedId, e);
            return false;
        }
    }
    
    /**
     * 获取用户的关注列表
     * 
     * @param followerId 关注者ID
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 关注关系聚合根列表
     */
    public List<FollowAggregate> getFollowingList(Long followerId, Integer pageNo, Integer pageSize) {
        try {
            return followAggregateRepository.findFollowingList(followerId, pageNo, pageSize);
        } catch (Exception e) {
            log.error("[关注管理] 获取关注列表失败 - 关注者: {}, 页码: {}, 页面大小: {}", 
                     followerId, pageNo, pageSize, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取用户的粉丝列表
     * 
     * @param followedId 被关注者ID
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 关注关系聚合根列表
     */
    public List<FollowAggregate> getFollowersList(Long followedId, Integer pageNo, Integer pageSize) {
        try {
            return followAggregateRepository.findFollowersList(followedId, pageNo, pageSize);
        } catch (Exception e) {
            log.error("[关注管理] 获取粉丝列表失败 - 被关注者: {}, 页码: {}, 页面大小: {}", 
                     followedId, pageNo, pageSize, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取用户关注数
     * 
     * @param followerId 关注者ID
     * @return 关注数
     */
    public int getFollowingCount(Long followerId) {
        try {
            return followAggregateRepository.countFollowing(followerId);
        } catch (Exception e) {
            log.error("[关注管理] 获取关注数失败 - 关注者: {}", followerId, e);
            return 0;
        }
    }
    
    /**
     * 获取用户粉丝数
     * 
     * @param followedId 被关注者ID
     * @return 粉丝数
     */
    public int getFollowersCount(Long followedId) {
        try {
            return followAggregateRepository.countFollowers(followedId);
        } catch (Exception e) {
            log.error("[关注管理] 获取粉丝数失败 - 被关注者: {}", followedId, e);
            return 0;
        }
    }
    
    /**
     * 获取互相关注列表
     * 
     * @param userId 用户ID
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 互相关注的用户ID列表
     */
    public List<Long> getMutualFollows(Long userId, Integer pageNo, Integer pageSize) {
        try {
            return followAggregateRepository.findMutualFollows(userId, pageNo, pageSize);
        } catch (Exception e) {
            log.error("[关注管理] 获取互相关注列表失败 - 用户: {}, 页码: {}, 页面大小: {}", 
                     userId, pageNo, pageSize, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 验证关注参数
     * 
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     */
    private void validateFollowParams(Long followerId, Long followedId) {
        // 验证关注关系的有效性
        FollowAggregate.create(followerId, followedId); // 这会触发验证
        
        // 检查用户是否存在
        UserAggregate follower = userAggregateRepository.findById(followerId)
                .orElseThrow(() -> new BusinessException("关注者不存在"));
        
        UserAggregate followed = userAggregateRepository.findById(followedId)
                .orElseThrow(() -> new BusinessException("被关注者不存在"));
        
        // 检查被关注用户状态
        if (!followed.isNormal()) {
            throw new BusinessException("被关注用户状态异常，无法关注");
        }
    }
    
    /**
     * 更新用户关注数和粉丝数
     * 
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     * @param isFollowing 是否为关注操作
     */
    private void updateUserFollowCounts(Long followerId, Long followedId, boolean isFollowing) {
        try {
            // 更新关注者关注数
            UserAggregate follower = userAggregateRepository.findById(followerId)
                    .orElseThrow(() -> new BusinessException("关注者不存在"));
            
            if (isFollowing) {
                follower.followUser();
            } else {
                follower.unfollowUser();
            }
            userAggregateRepository.update(follower);
            
            // 更新被关注者粉丝数
            UserAggregate followed = userAggregateRepository.findById(followedId)
                    .orElseThrow(() -> new BusinessException("被关注者不存在"));
            
            if (isFollowing) {
                followed.gainFollower();
            } else {
                followed.loseFollower();
            }
            userAggregateRepository.update(followed);
        } catch (Exception e) {
            log.error("[关注管理] 更新用户关注数和粉丝数失败 - 关注者: {}, 被关注者: {}", followerId, followedId, e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 发布领域事件
     * 
     * @param aggregate 关注关系聚合根
     */
    private void publishDomainEvents(FollowAggregate aggregate) {
        // 这里可以集成事件发布机制
        // 暂时只是记录事件
        aggregate.pullDomainEvents().forEach(event -> {
            log.info("[关注管理] 领域事件: {}", event.getClass().getSimpleName());
        });
    }
}