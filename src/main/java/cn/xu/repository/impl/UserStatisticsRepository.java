package cn.xu.repository.impl;

import cn.xu.model.entity.UserStatistics;
import cn.xu.repository.mapper.UserStatisticsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户统计Repository
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserStatisticsRepository {
    
    private final UserStatisticsMapper mapper;
    
    /**
     * 获取统计数据
     */
    public UserStatistics getByUserId(Long userId) {
        return mapper.selectByUserId(userId);
    }
    
    /**
     * 获取或创建统计记录
     */
    @Transactional
    public UserStatistics getOrCreate(Long userId) {
        UserStatistics stats = mapper.selectByUserId(userId);
        if (stats == null) {
            stats = UserStatistics.createNew(userId);
            try {
                mapper.insert(stats);
                log.info("创建用户统计记录: userId={}", userId);
            } catch (Exception e) {
                // 并发情况下可能已经创建，重新查询
                stats = mapper.selectByUserId(userId);
                if (stats == null) {
                    throw e;
                }
            }
        }
        return stats;
    }
    
    /**
     * 保存统计
     */
    @Transactional
    public void save(UserStatistics statistics) {
        mapper.insert(statistics);
    }
    
    /**
     * 更新统计
     */
    @Transactional
    public void update(UserStatistics statistics) {
        mapper.update(statistics);
    }
    
    /**
     * 增加发帖数
     */
    @Transactional
    public void incrementPostCount(Long userId) {
        getOrCreate(userId);
        int result = mapper.incrementPostCount(userId);
        if (result > 0) {
            log.debug("增加发帖数: userId={}", userId);
        }
    }
    
    /**
     * 减少发帖数
     */
    @Transactional
    public void decrementPostCount(Long userId) {
        int result = mapper.decrementPostCount(userId);
        if (result > 0) {
            log.debug("减少发帖数: userId={}", userId);
        }
    }
    
    /**
     * 增加评论数
     */
    @Transactional
    public void incrementCommentCount(Long userId) {
        getOrCreate(userId);
        int result = mapper.incrementCommentCount(userId);
        if (result > 0) {
            log.debug("增加评论数: userId={}", userId);
        }
    }
    
    /**
     * 减少评论数
     */
    @Transactional
    public void decrementCommentCount(Long userId) {
        int result = mapper.decrementCommentCount(userId);
        if (result > 0) {
            log.debug("减少评论数: userId={}", userId);
        }
    }
    
    /**
     * 增加关注数
     */
    @Transactional
    public void incrementFollowCount(Long userId) {
        getOrCreate(userId);
        int result = mapper.incrementFollowCount(userId);
        if (result > 0) {
            log.debug("增加关注数: userId={}", userId);
        }
    }
    
    /**
     * 减少关注数
     */
    @Transactional
    public void decrementFollowCount(Long userId) {
        int result = mapper.decrementFollowCount(userId);
        if (result > 0) {
            log.debug("减少关注数: userId={}", userId);
        }
    }
    
    /**
     * 增加粉丝数
     */
    @Transactional
    public void incrementFansCount(Long userId) {
        getOrCreate(userId);
        int result = mapper.incrementFansCount(userId);
        if (result > 0) {
            log.debug("增加粉丝数: userId={}", userId);
        }
    }
    
    /**
     * 减少粉丝数
     */
    @Transactional
    public void decrementFansCount(Long userId) {
        int result = mapper.decrementFansCount(userId);
        if (result > 0) {
            log.debug("减少粉丝数: userId={}", userId);
        }
    }
    
    /**
     * 删除统计
     */
    @Transactional
    public void deleteByUserId(Long userId) {
        mapper.deleteByUserId(userId);
        log.info("删除用户统计: userId={}", userId);
    }
}
