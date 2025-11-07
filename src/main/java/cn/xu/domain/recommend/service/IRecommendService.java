package cn.xu.domain.recommend.service;

import cn.xu.domain.post.model.entity.PostEntity;

import java.util.List;

/**
 * 推荐服务接口
 * 基于用户行为的内容推荐
 */
public interface IRecommendService {
    
    /**
     * 获取推荐帖子列表
     * 采用混合推荐策略：
     * 1. 基于用户行为的协同过滤（点赞、收藏）
     * 2. 基于内容的推荐（标签、话题）
     * 3. 基于关注用户的推荐
     * 4. 基于热度的推荐（兜底）
     *
     * @param userId 用户ID，如果为null则返回热门推荐
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @param categoryId 分类ID，可选，如果为null则返回所有分类的推荐
     * @return 推荐帖子列表
     */
    List<PostEntity> getRecommendedPosts(Long userId, Integer pageNo, Integer pageSize, Long categoryId);
    
    /**
     * 基于用户行为的协同过滤推荐
     * 找到与当前用户行为相似的用户，推荐他们喜欢的帖子
     *
     * @param userId 用户ID
     * @param limit 推荐数量
     * @return 推荐帖子列表
     */
    List<PostEntity> getCollaborativeFilteringRecommendations(Long userId, int limit);
    
    /**
     * 基于内容的推荐
     * 根据用户喜欢的标签和话题推荐相似内容
     *
     * @param userId 用户ID
     * @param limit 推荐数量
     * @return 推荐帖子列表
     */
    List<PostEntity> getContentBasedRecommendations(Long userId, int limit);
    
    /**
     * 基于关注用户的推荐
     * 推荐用户关注的人发布的帖子
     *
     * @param userId 用户ID
     * @param limit 推荐数量
     * @return 推荐帖子列表
     */
    List<PostEntity> getFollowingBasedRecommendations(Long userId, int limit);
    
    /**
     * 基于热度的推荐（兜底策略）
     * 推荐热门帖子
     *
     * @param limit 推荐数量
     * @return 推荐帖子列表
     */
    List<PostEntity> getHotRecommendations(int limit);
}

