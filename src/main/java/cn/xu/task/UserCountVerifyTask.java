package cn.xu.task;

import cn.xu.model.entity.User;
import cn.xu.repository.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户统计字段定时校验任务
 *
 * 实现方案：保留冗余字段 + 原子更新 + 定时校验
 *
 * 功能：
 * <ul>
 *   <li>每天凌晨3点校验用户统计字段</li>
 *   <li>对比数据库实际统计和user表中的冗余字段</li>
 *   <li>发现不一致时自动修复</li>
 *   <li>记录修复日志供排查</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserCountVerifyTask {

    private final UserMapper userMapper;
    private final FollowMapper followMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final LikeMapper likeMapper;

    /**
     * 定时校验任务
     *
     * <p>每天凌晨3点执行一次
     *
     * <p>cron表达式：0 0 3 * * ? = 每天3:00:00执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void verifyAndFixUserCounts() {
        log.info("[定时任务] 开始校验用户统计信息...");

        long startTime = System.currentTimeMillis();
        int fixedCount = 0;
        int totalCount = 0;

        try {
            // 1. 使用分页查询，避免一次性加载所有用户
            int pageSize = 500;
            int offset = 0;

            while (true) {
                List<User> users = userMapper.selectByPage(offset, pageSize);
                if (users == null || users.isEmpty()) {
                    break;
                }

                totalCount += users.size();

                // 2. 逐个校验并修复
                for (User user : users) {
                    boolean fixed = verifyAndFixSingleUser(user);
                    if (fixed) {
                        fixedCount++;
                    }
                }

                offset += pageSize;

                // 防止一次处理太多，记录进度
                if (offset % 2000 == 0) {
                    log.debug("已处理{}条记录，继续下一批，offset={}", fixedCount, offset);
                }
            }

            long costTime = System.currentTimeMillis() - startTime;
            log.info("[定时任务] 校验完成！总数={}, 修复数={}, 耗时={}ms",
                    totalCount, fixedCount, costTime);

        } catch (Exception e) {
            log.error("[定时任务] 校验失败", e);
        }
    }

    /**
     * 校验并修复单个用户的统计信息
     *
     * <p>比较数据库冗余字段与实际统计值，发现不一致时自动修复
     *
     * @param user 用户对象
     * @return true表示进行了修复，false表示数据一致无需修复
     */
    private boolean verifyAndFixSingleUser(User user) {
        try {
            Long userId = user.getId();

            // 1. 统计实际的关注数
            Long actualFollowCount = (long) followMapper.countFollowing(userId);

            // 2. 统计实际的粉丝数
            Long actualFansCount = (long) followMapper.countFollowers(userId);

            // 3. 统计实际的获赞数（用户发的帖子和评论收到的点赞总数）
            Long actualLikeCount = calculateTotalReceivedLikes(userId);

            // 4. 统计实际的发帖数
            Long actualPostCount = postMapper.countPublishedByUserId(userId);
            if (actualPostCount == null) {
                actualPostCount = 0L;
            }

            // 5. 统计实际的评论数
            Long actualCommentCount = commentMapper.countByUserId(userId);
            if (actualCommentCount == null) {
                actualCommentCount = 0L;
            }

            // 6. 获取用户当前值，处理null情况
            Long dbFollowCount = user.getFollowCount() != null ? user.getFollowCount() : 0L;
            Long dbFansCount = user.getFansCount() != null ? user.getFansCount() : 0L;
            Long dbLikeCount = user.getLikeCount() != null ? user.getLikeCount() : 0L;
            Long dbPostCount = user.getPostCount() != null ? user.getPostCount() : 0L;
            Long dbCommentCount = user.getCommentCount() != null ? user.getCommentCount() : 0L;

            // 7. 对比并判断是否需要修复
            boolean needFix = !dbFollowCount.equals(actualFollowCount)
                    || !dbFansCount.equals(actualFansCount)
                    || !dbLikeCount.equals(actualLikeCount)
                    || !dbPostCount.equals(actualPostCount)
                    || !dbCommentCount.equals(actualCommentCount);

            if (needFix) {
                log.warn("[计数不一致] userId={}, username={}, " +
                                "关注: {}→{}, 粉丝: {}→{}, 获赞: {}→{}, 发帖: {}→{}, 评论: {}→{}",
                        userId, user.getUsername(),
                        dbFollowCount, actualFollowCount,
                        dbFansCount, actualFansCount,
                        dbLikeCount, actualLikeCount,
                        dbPostCount, actualPostCount,
                        dbCommentCount, actualCommentCount
                );

                // 8. 修复数据
                userMapper.updateUserCounts(
                        userId,
                        actualFollowCount,
                        actualFansCount,
                        actualLikeCount,
                        actualPostCount,
                        actualCommentCount
                );

                log.info("[修复成功] userId={}, username={}", userId, user.getUsername());
                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("[校验失败] userId={}, username={}", user.getId(), user.getUsername(), e);
            return false;
        }
    }

    /**
     * 计算用户收到的总点赞数
     *
     * <p>即：用户发布的帖子和评论被别人点赞的总数
     *
     * <p>计算公式：帖子获赞数 + 评论获赞数
     *
     * @param userId 用户ID
     * @return 总获赞数
     */
    private Long calculateTotalReceivedLikes(Long userId) {
        try {
            // 统计用户帖子收到的点赞数
            Long postLikes = likeMapper.countReceivedLikesByUserPosts(userId);
            if (postLikes == null) {
                postLikes = 0L;
            }

            // 统计用户评论收到的点赞数
            Long commentLikes = likeMapper.countReceivedLikesByUserComments(userId);
            if (commentLikes == null) {
                commentLikes = 0L;
            }

            return postLikes + commentLikes;

        } catch (Exception e) {
            log.error("[统计获赞数失败] userId={}", userId, e);
            return 0L;
        }
    }

    /**
     * 手动触发校验（可在Controller中调用，用于紧急修复）
     *
     * <p>示例：当发现计数不准时，可以通过API手动触发重新校验
     */
    public void manualVerify() {
        log.info("[手动触发] 开始校验用户统计字段...");
        verifyAndFixUserCounts();
    }
}
