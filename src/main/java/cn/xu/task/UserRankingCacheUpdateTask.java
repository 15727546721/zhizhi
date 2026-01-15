package cn.xu.task;

import cn.xu.cache.repository.UserRankingCacheRepository;
import cn.xu.model.entity.User;
import cn.xu.repository.UserRepository;
import cn.xu.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户排名缓存更新任务
 * 定时更新Redis中的用户排名缓存
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserRankingCacheUpdateTask {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final UserRankingCacheRepository userRankingCacheRepository;

    /**
     * 定时更新用户排名缓存
     * 每30分钟更新一次，包括根据粉丝数、点赞数、帖子数和综合排名来更新缓存
     */
    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void updateUserRankingCache() {
        log.info("开始执行用户排名缓存更新任务");

        try {
            // 更新粉丝数排名
            updateRankingByFans();

            // 更新点赞数排名
            updateRankingByLikes();

            // 更新帖子数排名
            updateRankingByPosts();

            // 更新综合排名
            updateRankingByComprehensive();

            log.info("用户排名缓存更新任务完成");
        } catch (Exception e) {
            log.error("用户排名缓存更新任务执行失败", e);
        }
    }

    /**
     * 更新粉丝数排名
     */
    private void updateRankingByFans() {
        try {
            log.debug("开始更新粉丝数排名");

            // 查询粉丝数排名前1000的用户
            List<User> users = userRepository.findUserRanking("fans", 0, 1000);

            // 转换成ID与分数的映射
            Map<Long, Double> userScores = new HashMap<>();
            for (User user : users) {
                if (user != null && user.getId() != null) {
                    double score = user.getFansCount() != null ? user.getFansCount().doubleValue() : 0.0;
                    // 计算得分: 粉丝数 * 1000000 + 点赞数
                    score = score * 1000000 + (user.getLikeCount() != null ? user.getLikeCount().doubleValue() : 0.0);
                    userScores.put(user.getId(), score);
                }
            }

            // 更新Redis缓存
            userRankingCacheRepository.cacheUserRanking("fans", userScores);

            log.info("粉丝数排名更新完成, 用户数: {}", userScores.size());
        } catch (Exception e) {
            log.error("更新粉丝数排名失败", e);
        }
    }

    /**
     * 更新点赞数排名
     */
    private void updateRankingByLikes() {
        try {
            log.debug("开始更新点赞数排名");

            // 查询点赞数排名前1000的用户
            List<User> users = userRepository.findUserRanking("likes", 0, 1000);

            // 转换成ID与分数的映射
            Map<Long, Double> userScores = new HashMap<>();
            for (User user : users) {
                if (user != null && user.getId() != null) {
                    double score = user.getLikeCount() != null ? user.getLikeCount().doubleValue() : 0.0;
                    // 计算得分: 点赞数 * 1000000 + 粉丝数
                    score = score * 1000000 + (user.getFansCount() != null ? user.getFansCount().doubleValue() : 0.0);
                    userScores.put(user.getId(), score);
                }
            }

            // 更新Redis缓存
            userRankingCacheRepository.cacheUserRanking("likes", userScores);

            log.info("点赞数排名更新完成, 用户数: {}", userScores.size());
        } catch (Exception e) {
            log.error("更新点赞数排名失败", e);
        }
    }

    /**
     * 更新帖子数排名
     */
    private void updateRankingByPosts() {
        try {
            log.debug("开始更新帖子数排名");

            // 查询帖子数排名前1000的用户
            List<User> users = userRepository.findUserRanking("posts", 0, 1000);

            // 转换成ID与分数的映射
            Map<Long, Double> userScores = new HashMap<>();
            for (User user : users) {
                if (user != null && user.getId() != null) {
                    double score = user.getPostCount() != null ? user.getPostCount().doubleValue() : 0.0;
                    // 计算得分: 帖子数 * 1000000 + 点赞数
                    score = score * 1000000 + (user.getLikeCount() != null ? user.getLikeCount().doubleValue() : 0.0);
                    userScores.put(user.getId(), score);
                }
            }

            // 更新Redis缓存
            userRankingCacheRepository.cacheUserRanking("posts", userScores);

            log.info("帖子数排名更新完成, 用户数: {}", userScores.size());
        } catch (Exception e) {
            log.error("更新帖子数排名失败", e);
        }
    }

    /**
     * 更新综合排名
     * 综合分数 = 粉丝数 * 0.4 + 点赞数 * 0.4 + 帖子数 * 0.2
     */
    private void updateRankingByComprehensive() {
        try {
            log.debug("开始更新综合排名");

            // 查询综合排名前1000的用户
            List<User> users = userRepository.findUserRanking("comprehensive", 0, 1000);

            // 转换成ID与分数的映射
            Map<Long, Double> userScores = new HashMap<>();
            for (User user : users) {
                if (user != null && user.getId() != null) {
                    long fansCount = user.getFansCount() != null ? user.getFansCount() : 0L;
                    long likeCount = user.getLikeCount() != null ? user.getLikeCount() : 0L;
                    long postCount = user.getPostCount() != null ? user.getPostCount() : 0L;

                    // 计算综合分数
                    double score = fansCount * 0.4 + likeCount * 0.4 + postCount * 0.2;
                    userScores.put(user.getId(), score);
                }
            }

            // 更新Redis缓存
            userRankingCacheRepository.cacheUserRanking("comprehensive", userScores);

            log.info("综合排名更新完成, 用户数: {}", userScores.size());
        } catch (Exception e) {
            log.error("更新综合排名失败", e);
        }
    }
}
