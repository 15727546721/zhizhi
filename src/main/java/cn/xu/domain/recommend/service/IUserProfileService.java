package cn.xu.domain.recommend.service;

import java.util.List;
import java.util.Map;

/**
 * 用户画像服务接口
 * 负责用户行为画像数据的计算、存储和更新
 */
public interface IUserProfileService {
    
    /**
     * 更新用户画像
     * 当用户有新的行为时（点赞、收藏、评论等），更新用户画像
     *
     * @param userId 用户ID
     */
    void updateUserProfile(Long userId);
    
    /**
     * 批量更新用户画像
     *
     * @param userIds 用户ID列表
     */
    void batchUpdateUserProfile(List<Long> userIds);
    
    /**
     * 获取用户偏好的标签ID列表
     *
     * @param userId 用户ID
     * @param limit 返回数量限制
     * @return 标签ID列表（按偏好分数降序）
     */
    List<Long> getUserPreferredTags(Long userId, int limit);
    
    /**
     * 获取用户偏好的话题ID列表
     *
     * @param userId 用户ID
     * @param limit 返回数量限制
     * @return 话题ID列表（按偏好分数降序）
     */
    List<Long> getUserPreferredTopics(Long userId, int limit);
    
    /**
     * 获取用户标签偏好分数
     *
     * @param userId 用户ID
     * @return 标签ID -> 偏好分数的映射
     */
    Map<Long, Double> getUserTagPreferences(Long userId);
    
    /**
     * 获取用户话题偏好分数
     *
     * @param userId 用户ID
     * @return 话题ID -> 偏好分数的映射
     */
    Map<Long, Double> getUserTopicPreferences(Long userId);
    
    /**
     * 计算用户相似度
     * 用于协同过滤推荐
     *
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @return 相似度分数（0-1）
     */
    double calculateUserSimilarity(Long userId1, Long userId2);
    
    /**
     * 获取与用户相似的用户列表
     *
     * @param userId 用户ID
     * @param limit 返回数量限制
     * @return 相似用户ID列表（按相似度降序）
     */
    List<Long> getSimilarUsers(Long userId, int limit);
    
    /**
     * 增量更新用户画像
     * 当用户有新的行为时，只更新相关的画像数据
     *
     * @param userId 用户ID
     * @param postId 帖子ID（触发行为的目标）
     * @param behaviorType 行为类型：like, favorite, comment, view
     */
    void incrementUpdateProfile(Long userId, Long postId, String behaviorType);
    
    /**
     * 定时任务：批量计算用户相似度
     * 可以定期运行，更新用户相似度表
     */
    void batchCalculateUserSimilarity();
}

