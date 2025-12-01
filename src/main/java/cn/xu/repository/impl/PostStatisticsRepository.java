package cn.xu.repository.impl;

import cn.xu.model.entity.PostStatistics;
import cn.xu.repository.mapper.PostStatisticsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 帖子统计Repository
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PostStatisticsRepository {
    
    private final PostStatisticsMapper mapper;
    
    /**
     * 获取统计数据
     */
    public PostStatistics getByPostId(Long postId) {
        return mapper.selectByPostId(postId);
    }
    
    /**
     * 获取或创建统计记录
     */
    @Transactional
    public PostStatistics getOrCreate(Long postId) {
        PostStatistics stats = mapper.selectByPostId(postId);
        if (stats == null) {
            stats = PostStatistics.createNew(postId);
            try {
                mapper.insert(stats);
                log.info("创建帖子统计记录: postId={}", postId);
            } catch (Exception e) {
                // 并发情况下可能已经创建，重新查询
                stats = mapper.selectByPostId(postId);
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
    public void save(PostStatistics statistics) {
        mapper.insert(statistics);
    }
    
    /**
     * 更新统计
     */
    @Transactional
    public void update(PostStatistics statistics) {
        mapper.update(statistics);
    }
    
    /**
     * 增加浏览量
     */
    @Transactional
    public void incrementViewCount(Long postId) {
        getOrCreate(postId); // 确保记录存在
        int result = mapper.incrementViewCount(postId);
        if (result > 0) {
            log.debug("增加浏览量: postId={}", postId);
        }
    }
    
    /**
     * 增加评论数
     */
    @Transactional
    public void incrementCommentCount(Long postId) {
        getOrCreate(postId);
        int result = mapper.incrementCommentCount(postId);
        if (result > 0) {
            log.debug("增加评论数: postId={}", postId);
        }
    }
    
    /**
     * 减少评论数
     */
    @Transactional
    public void decrementCommentCount(Long postId) {
        int result = mapper.decrementCommentCount(postId);
        if (result > 0) {
            log.debug("减少评论数: postId={}", postId);
        }
    }
    
    /**
     * 增加点赞数
     */
    @Transactional
    public void incrementLikeCount(Long postId) {
        getOrCreate(postId);
        int result = mapper.incrementLikeCount(postId);
        if (result > 0) {
            log.debug("增加点赞数: postId={}", postId);
        }
    }
    
    /**
     * 减少点赞数
     */
    @Transactional
    public void decrementLikeCount(Long postId) {
        int result = mapper.decrementLikeCount(postId);
        if (result > 0) {
            log.debug("减少点赞数: postId={}", postId);
        }
    }
    
    /**
     * 增加收藏数
     */
    @Transactional
    public void incrementFavoriteCount(Long postId) {
        getOrCreate(postId);
        int result = mapper.incrementFavoriteCount(postId);
        if (result > 0) {
            log.debug("增加收藏数: postId={}", postId);
        }
    }
    
    /**
     * 减少收藏数
     */
    @Transactional
    public void decrementFavoriteCount(Long postId) {
        int result = mapper.decrementFavoriteCount(postId);
        if (result > 0) {
            log.debug("减少收藏数: postId={}", postId);
        }
    }
    
    /**
     * 删除统计
     */
    @Transactional
    public void deleteByPostId(Long postId) {
        mapper.deleteByPostId(postId);
        log.info("删除帖子统计: postId={}", postId);
    }
}
