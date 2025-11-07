package cn.xu.infrastructure.persistent.repository;

import cn.xu.api.web.model.vo.post.PostListResponse;
import cn.xu.infrastructure.persistent.converter.PostConverter;
import cn.xu.infrastructure.persistent.dao.PostMapper;
import cn.xu.infrastructure.persistent.dao.PostTagMapper;
import cn.xu.infrastructure.persistent.po.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 帖子数据访问助手类
 * 提供辅助性的数据访问方法，不直接实现领域仓储接口
 * 主要用于内部基础设施层的数据操作支持
 */
@Slf4j
@Repository("postRepositoryHelper")
@RequiredArgsConstructor
public class PostRepository {

    private final PostMapper postDao;
    private final PostTagMapper postTagDao;
    private final TransactionTemplate transactionTemplate;
    private final PostConverter postConverter;

    // 辅助方法：更新帖子点赞数量（增量更新）
    public void updatePostLikeCount(Long postId, Long increment) {
        try {
            log.info("[PostRepository] 更新帖子点赞数 - postId: {}, increment: {}", postId, increment);
            postDao.updateLikeCount(postId, increment);
            log.info("[PostRepository] 更新帖子点赞数完成 - postId: {}, increment: {}", postId, increment);
        } catch (Exception e) {
            log.error("[PostRepository] 更新帖子点赞数失败 - postId: {}, increment: {}", postId, increment, e);
            throw e;
        }
    }
    
    // 辅助方法：更新帖子收藏数量（增量更新）
    public void updatePostFavoriteCount(Long postId, Long increment) {
        try {
            log.info("[PostRepository] 更新帖子收藏数 - postId: {}, increment: {}", postId, increment);
            postDao.updateFavoriteCount(postId, increment);
            log.info("[PostRepository] 更新帖子收藏数完成 - postId: {}, increment: {}", postId, increment);
        } catch (Exception e) {
            log.error("[PostRepository] 更新帖子收藏数失败 - postId: {}, increment: {}", postId, increment, e);
            throw e;
        }
    }

    // 辅助方法：批量更新帖子点赞数量
    public void batchUpdatePostLikeCount(Map<Long, Long> likeCounts) {
        likeCounts.forEach((postId, likeCount) -> 
            postDao.updateLikeCount(postId, likeCount));
    }
    
    // 辅助方法：批量更新帖子收藏数量
    public void batchUpdatePostFavoriteCount(Map<Long, Long> favoriteCounts) {
        favoriteCounts.forEach((postId, favoriteCount) -> 
            postDao.updateFavoriteCount(postId, favoriteCount));
    }

    // 辅助方法：根据ID查找帖子PO对象
    public Post findPoById(Long id) {
        return postDao.findById(id);
    }

    // 辅助方法：删除单个帖子
    public void deleteById(Long id) {
        postDao.deleteById(id);
    }

    // 辅助方法：根据分类ID获取帖子列表
    public List<PostListResponse> findPostByCategoryId(Long categoryId) {
        return postDao.findByCategoryId(categoryId);
    }

    // 由于这些方法在系统中未被使用，暂时保留但标记为过时
    @Deprecated
    public List<PostListResponse> findPostListByUserId(Long userId) {
        List<Post> posts = postDao.findByUserId(userId);
        return posts.stream()
            .map(post -> PostListResponse.builder().post(PostConverter.toDomainEntity(post)).build())
            .collect(Collectors.toList());
    }

    @Deprecated
    public void updatePostStatus(Integer status, Long id) {
        postDao.updateStatus(status, id);
    }

    @Deprecated
    public List<PostListResponse> findDraftPostListByUser(Long userId) {
        List<Post> posts = postDao.findDraftPostListByUserId(userId);
        return posts.stream()
            .map(post -> PostListResponse.builder().post(PostConverter.toDomainEntity(post)).build())
            .collect(Collectors.toList());
    }
}