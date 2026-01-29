package cn.xu.repository;

import cn.xu.model.entity.Post;
import cn.xu.repository.impl.PostRepositoryImpl.PostWithTags;

import java.util.List;
import java.util.Optional;

/**
 * 帖子仓储接口
 */
public interface PostRepository {

    // ==================== 基础CRUD ====================

    Long save(Post post, List<Long> tagIds);

    void update(Post post, List<Long> tagIds);

    Optional<Post> findById(Long postId);

    PostWithTags findByIdWithTags(Long postId);

    void deleteByIds(List<Long> postIds);

    // ==================== 查询方法 ====================

    List<Post> findAllPublished();

    List<Post> findByUserId(Long userId);

    List<Post> findByUserIdAndStatus(Long userId, Integer status, int offset, int limit);

    List<Post> findByTagId(Long tagId, int offset, int limit);

    List<Post> findAll(int offset, int limit);

    List<Post> findHotPosts(int offset, int limit);

    List<Post> findByIds(List<Long> postIds);

    List<Post> findByUserIds(List<Long> userIds, int offset, int limit);

    long countByUserIds(List<Long> userIds);

    // ==================== 统计方法 ====================

    long countAll();

    long countByTagId(Long tagId);

    long countPublishedByUserId(Long userId);

    long countDraftsByUserId(Long userId);

    // ==================== 计数更新 ====================

    void incrementViewCount(Long postId);

    void increaseFavoriteCount(Long postId);

    void decreaseFavoriteCount(Long postId);

    void updateHotScore(Long postId);
}
