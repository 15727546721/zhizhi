package cn.xu.task;

import cn.xu.model.entity.Post;
import cn.xu.repository.mapper.CommentMapper;
import cn.xu.repository.mapper.FavoriteMapper;
import cn.xu.repository.mapper.LikeMapper;
import cn.xu.repository.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 帖子统计字段定时校验任务
 * 
 * 功能：
 * 1. 每天凌晨4点校验帖子统计字段
 * 2. 对比数据库实际统计和post表中的冗余字段
 * 3. 发现不一致时自动修复
 * 4. 记录修复日志供排查
 * 
 * 
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostCountVerifyTask {
    
    private final PostMapper postMapper;
    private final LikeMapper likeMapper;
    private final CommentMapper commentMapper;
    private final FavoriteMapper favoriteMapper;
    
    // 帖子点赞类型
    private static final int LIKE_TYPE_POST = 1;
    
    /**
     * 定时校验任务
     * 每天凌晨4点执行（与用户校验错开）
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void verifyAndFixPostCounts() {
        log.info("[定时任务] 开始校验帖子统计字段...");
        
        long startTime = System.currentTimeMillis();
        int fixedCount = 0;
        int totalCount = 0;
        
        try {
            // 1. 分批查询所有已发布帖子
            int pageSize = 500;
            int offset = 0;
            
            while (true) {
                List<Post> posts = postMapper.getPublishedPostPageList(offset, pageSize);
                if (posts == null || posts.isEmpty()) {
                    break;
                }
                
                totalCount += posts.size();
                
                // 2. 逐个校验并修复
                for (Post post : posts) {
                    boolean fixed = verifyAndFixSinglePost(post);
                    if (fixed) {
                        fixedCount++;
                    }
                }
                
                offset += pageSize;
                
                // 防止一次处理太多，使用分批处理避免阻塞
                if (offset % 2000 == 0) {
                    log.debug("已处理 {} 条记录，继续下一批", offset);
                }
            }
            
            long costTime = System.currentTimeMillis() - startTime;
            log.info("[定时任务] 帖子校验完成！总数={}, 修复数={}, 耗时={}ms", 
                totalCount, fixedCount, costTime);
            
        } catch (Exception e) {
            log.error("[定时任务] 帖子校验失败", e);
        }
    }
    
    /**
     * 校验并修复单个帖子的统计字段
     * 
     * @param post 帖子对象
     * @return true-需要修复 false-数据一致
     */
    private boolean verifyAndFixSinglePost(Post post) {
        try {
            Long postId = post.getId();
            
            // 1. 统计实际的点赞数
            long actualLikeCount = likeMapper.countByTargetIdAndType(postId, LIKE_TYPE_POST);
            
            // 2. 统计实际的评论数
            Long actualCommentCount = commentMapper.countByTargetTypeAndTargetId(1, postId); // 1=帖子评论
            if (actualCommentCount == null) actualCommentCount = 0L;
            
            // 3. 统计实际的收藏数
            int favoriteCount = favoriteMapper.countFavoritedItemsByTarget(postId, "post");
            Long actualFavoriteCount = (long) favoriteCount;
            
            // 4. 获取帖子当前值，处理null
            Long dbLikeCount = post.getLikeCount() != null ? post.getLikeCount() : 0L;
            Long dbCommentCount = post.getCommentCount() != null ? post.getCommentCount() : 0L;
            Long dbFavoriteCount = post.getFavoriteCount() != null ? post.getFavoriteCount() : 0L;
            
            // 5. 对比并判断是否需要修复（浏览量不校验，因为可能有缓存延迟）
            boolean needFix = !dbLikeCount.equals(actualLikeCount)
                || !dbCommentCount.equals(actualCommentCount)
                || !dbFavoriteCount.equals(actualFavoriteCount);
            
            if (needFix) {
                log.warn("[帖子计数不一致] postId={}, title={}, " +
                        "点赞: {}→{}, 评论: {}→{}, 收藏: {}→{}",
                    postId, post.getTitle() != null ? post.getTitle().substring(0, Math.min(20, post.getTitle().length())) : "",
                    dbLikeCount, actualLikeCount,
                    dbCommentCount, actualCommentCount,
                    dbFavoriteCount, actualFavoriteCount
                );
                
                // 6. 修复数据（不修改viewCount，保持原值）
                Long viewCount = post.getViewCount() != null ? post.getViewCount() : 0L;
                postMapper.updateCounts(
                    postId,
                    viewCount,
                    actualLikeCount,
                    actualCommentCount,
                    actualFavoriteCount
                );
                
                log.info("[修复成功] postId={}", postId);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("[校验失败] postId={}", post.getId(), e);
            return false;
        }
    }
    
    /**
     * 手动触发校验（可以在Controller中调用）
     */
    public void manualVerify() {
        log.info("[手动触发] 开始校验帖子统计字段...");
        verifyAndFixPostCounts();
    }
}