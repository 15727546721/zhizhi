package cn.xu.service.user;

import cn.xu.cache.UserRankingCacheRepository;
import cn.xu.model.entity.User;
import cn.xu.model.enums.UserRankingSortType;
import cn.xu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户排行榜服务
 * <p>负责用户排行榜查询和缓存管理</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRankingService {

    private final UserRepository userRepository;
    private final UserRankingCacheRepository userRankingCacheRepository;

    /**
     * 查询用户排行榜
     */
    public List<User> findUserRanking(String sortType, int page, int size) {
        try {
            // 先从缓存获取
            int start = (page - 1) * size;
            int end = start + size - 1;
            List<Long> cachedUserIds = userRankingCacheRepository.getUserRankingIds(sortType, start, end);
            
            if (cachedUserIds != null && !cachedUserIds.isEmpty()) {
                return userRepository.findByIds(new ArrayList<>(cachedUserIds));
            }

            // 缓存未命中，从数据库查询
            int offset = Math.max(0, (page - 1) * size);
            List<User> users = userRepository.findUserRanking(sortType, offset, size);

            // 写入缓存
            if (users != null && !users.isEmpty()) {
                Map<Long, Double> userScores = new HashMap<>();
                for (User user : users) {
                    double score = calculateRankingScore(user, sortType);
                    userScores.put(user.getId(), score);
                }
                userRankingCacheRepository.cacheUserRanking(sortType, userScores);
            }

            return users != null ? users : new ArrayList<>();
        } catch (Exception e) {
            log.error("查询用户排行榜失败, sortType: {}, page: {}, size: {}", sortType, page, size, e);
            return new ArrayList<>();
        }
    }

    /**
     * 计算用户排行榜分数
     */
    private double calculateRankingScore(User user, String sortType) {
        if (user == null) return 0.0;

        UserRankingSortType rankingSortType = UserRankingSortType.fromCode(sortType);

        switch (rankingSortType) {
            case FANS:
                return user.getFansCount() != null ? user.getFansCount().doubleValue() : 0.0;
            case LIKES:
                return user.getLikeCount() != null ? user.getLikeCount().doubleValue() : 0.0;
            case POSTS:
                return user.getPostCount() != null ? user.getPostCount().doubleValue() : 0.0;
            case COMPREHENSIVE:
            default:
                // 综合分数 = 粉丝数*1 + 获赞数*0.5 + 帖子数*2
                double fans = user.getFansCount() != null ? user.getFansCount() : 0.0;
                double likes = user.getLikeCount() != null ? user.getLikeCount() : 0.0;
                double posts = user.getPostCount() != null ? user.getPostCount() : 0.0;
                return fans + likes * 0.5 + posts * 2;
        }
    }
}
