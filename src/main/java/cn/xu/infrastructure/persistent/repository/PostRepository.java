package cn.xu.infrastructure.persistent.repository;

import cn.xu.api.system.model.dto.post.SysPostQueryRequest;
import cn.xu.api.web.model.vo.post.PostListResponse;
import cn.xu.api.web.model.vo.post.PostPageResponse;
import cn.xu.domain.post.model.aggregate.PostAggregate;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.valobj.PostType;
import cn.xu.domain.post.repository.IPostRepository;
import cn.xu.infrastructure.persistent.converter.PostConverter;
import cn.xu.infrastructure.persistent.dao.PostMapper;
import cn.xu.infrastructure.persistent.dao.PostTagMapper;
import cn.xu.infrastructure.persistent.po.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 帖子仓储实现
 * 遵循DDD原则，处理帖子实体级别的操作
 * 注意：对于聚合根级别的操作，应使用PostAggregateRepositoryImpl
 */
@Slf4j
@Repository("postRepository")
@RequiredArgsConstructor
public class PostRepository implements IPostRepository {

    private final PostMapper postDao;
    private final PostTagMapper postTagDao;
    private final TransactionTemplate transactionTemplate;
    private final PostConverter postConverter;

    @Override
    public Long save(PostAggregate postAggregate) {
        if (postAggregate == null) {
            return null;
        }
        PostEntity postEntity = postAggregate.getPostEntity();
        Post post = PostConverter.toDataObject(postEntity);
        postDao.insert(post);
        return post.getId();
    }

    public List<PostPageResponse> findPost(SysPostQueryRequest postRequest) {
        postRequest.setPageNo(postRequest.getPageNo() - 1);
        return postDao.queryByPage(postRequest);
    }

    @Override
    public void deleteByIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return;
        }
        transactionTemplate.execute(status -> {
            try {
                postTagDao.deleteByPostIds(postIds);
                postDao.deleteByIds(postIds);
                return 1;
            } catch (Exception e) {
                status.setRollbackOnly();
                throw new RuntimeException("删除帖子失败", e);
            }
        });
    }

    @Override
    public Optional<PostAggregate> findById(Long id) {
        Post post = postDao.findById(id);
        if (post == null) {
            return Optional.empty();
        }
        PostEntity postEntity = PostConverter.toDomainEntity(post);
        return Optional.of(PostAggregate.builder().postEntity(postEntity).build());
    }

    public Post findPoById(Long id) {
        return postDao.findById(id);
    }

    @Override
    public void update(PostAggregate postAggregate) {
        if (postAggregate == null) {
            return;
        }
        PostEntity postEntity = postAggregate.getPostEntity();
        Post post = PostConverter.toDataObject(postEntity);
        postDao.update(post);
    }

    public List<PostEntity> findPostByPage(int page, int size) {
        List<Post> posts = postDao.findPostByPage(page, size);
        return PostConverter.toDomainEntities(posts);
    }

    public List<PostListResponse> findPostByCategoryId(Long categoryId) {
        return postDao.findByCategoryId(categoryId);
    }

    public void updatePostLikeCount(Long postId, Long likeCount) {
        postDao.updateLikeCount(postId, likeCount.intValue());
    }

    public void batchUpdatePostLikeCount(Map<Long, Long> likeCounts) {
        likeCounts.forEach((postId, likeCount) -> 
            postDao.updateLikeCount(postId, likeCount.intValue()));
    }

    @Override
    public List<PostEntity> findAllPublished() {
        List<Post> posts = postDao.findAllPublishedPosts();
        return PostConverter.toDomainEntities(posts);
    }

    @Override
    public List<PostEntity> findAll() {
        List<Post> posts = postDao.findAll();
        return PostConverter.toDomainEntities(posts);
    }

    public List<PostListResponse> findPostListByUserId(Long userId) {
        List<Post> posts = postDao.findByUserId(userId);
        // 需要将Post PO对象转换为PostListResponse对象
        return posts.stream()
            .map(post -> PostListResponse.builder().post(PostConverter.toDomainEntity(post)).build())
            .collect(Collectors.toList());
    }

    public void updatePostStatus(Integer status, Long id) {
        postDao.updateStatus(status, id);
    }

    // 重命名这个方法以避免与接口方法冲突
    public List<PostListResponse> findDraftPostListByUser(Long userId) {
        List<Post> posts = postDao.findDraftPostListByUserId(userId);
        // 需要将Post PO对象转换为PostListResponse对象
        return posts.stream()
            .map(post -> PostListResponse.builder().post(PostConverter.toDomainEntity(post)).build())
            .collect(Collectors.toList());
    }

    public void deleteById(Long id) {
        postDao.deleteById(id);
    }

    public List<PostEntity> getPostPageListByCategoryId(Long categoryId, Integer pageNo, Integer pageSize) {
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        List<Post> posts = postDao.getPostPageByCategory(categoryId, offset, pageSize);
        return PostConverter.toDomainEntities(posts);
    }

    /**
     * 分页查询帖子列表
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 帖子列表
     */
    public List<PostEntity> getPostPageList(Integer pageNo, Integer pageSize) {
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        List<Post> posts = postDao.getPostPageList(offset, pageSize);
        return PostConverter.toDomainEntities(posts);
    }

    /**
     * 分页查询帖子列表（支持排序）
     */
    public List<PostEntity> getPostPageListWithSort(Integer pageNo, Integer pageSize, String sortBy) {
        int offset = (pageNo - 1) * pageSize;
        List<Post> posts = postDao.getPostPageListWithSort(offset, pageSize, sortBy);
        return PostConverter.toDomainEntities(posts);
    }
    
    /**
     * 根据ID列表查询帖子列表
     */
    public List<PostEntity> findPostsByIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return new LinkedList<>();
        }
        List<Post> posts = postDao.findPostsByIds(postIds);
        return PostConverter.toDomainEntities(posts);
    }
    
    /**
     * 根据标题搜索帖子（分页）
     * @param title 标题关键词
     * @param offset 偏移量
     * @param limit 数量
     * @return 帖子列表
     */
    @Override
    public List<PostEntity> searchByTitle(String title, int offset, int limit) {
        int safeOffset = Math.max(0, offset);
        List<Post> posts = postDao.searchPosts(title, safeOffset, limit);
        return PostConverter.toDomainEntities(posts);
    }
    
    /**
     * 统计根据标题搜索的帖子数量
     * @param title 标题关键词
     * @return 帖子数量
     */
    @Override
    public long countSearchByTitle(String title) {
        Long count = postDao.countSearchResults(title);
        return count != null ? count : 0;
    }
    
    public List<PostEntity> getPostPageListByUserIds(List<Long> userIds, Integer pageNo, Integer pageSize) {
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Post> posts = postDao.getPostPageListByUserIds(userIds, offset, pageSize);
        return PostConverter.toDomainEntities(posts);
    }

    @Override
    public List<PostEntity> findByUserIds(List<Long> userIds, int offset, int limit) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        int safeOffset = Math.max(0, offset);
        List<Post> posts = postDao.getPostPageListByUserIds(userIds, safeOffset, limit);
        return PostConverter.toDomainEntities(posts);
    }

    @Override
    public List<PostEntity> findByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        // 从Post PO对象转换为PostEntity对象
        List<Post> posts = postDao.findByUserId(userId);
        return PostConverter.toDomainEntities(posts);
    }

    @Override
    public List<PostEntity> findDraftsByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        // 从Post PO对象转换为PostEntity对象
        List<Post> posts = postDao.findDraftPostListByUserId(userId);
        return PostConverter.toDomainEntities(posts);
    }

    @Override
    public void incrementViewCount(Long postId) {
        if (postId == null) {
            return;
        }
        Post post = postDao.findById(postId);
        if (post != null) {
            post.setViewCount(post.getViewCount() + 1);
            postDao.update(post);
        }
    }

    @Override
    public void updateHotScore(Long postId) {
        // 热度分数更新逻辑可以在这里实现
        // 目前留空，由专门的服务处理
    }

    @Override
    public List<PostEntity> findByCategoryId(Long categoryId, int offset, int limit) {
        if (categoryId == null) {
            return Collections.emptyList();
        }
        int safeOffset = Math.max(0, offset);
        List<Post> posts = postDao.getPostPageByCategory(categoryId, safeOffset, limit);
        return PostConverter.toDomainEntities(posts);
    }

    @Override
    public List<PostEntity> findAll(int offset, int limit) {
        int safeOffset = Math.max(0, offset);
        List<Post> posts = postDao.getPostPageList(safeOffset, limit);
        return PostConverter.toDomainEntities(posts);
    }

    @Override
    public List<PostEntity> findByType(PostType type, int offset, int limit) {
        if (type == null) {
            return Collections.emptyList();
        }
        int safeOffset = Math.max(0, offset);
        List<Post> posts = postDao.findPostsByType(type.name(), safeOffset, limit);
        return PostConverter.toDomainEntities(posts);
    }
    
    @Override
    public long countByType(PostType type) {
        if (type == null) {
            return 0;
        }
        Long count = postDao.countPostsByType(type.name());
        return count != null ? count : 0;
    }

    @Override
    public List<PostEntity> findHotPosts(int offset, int limit) {
        int safeOffset = Math.max(0, offset);
        List<Post> posts = postDao.findHotPosts(safeOffset, limit);
        return PostConverter.toDomainEntities(posts);
    }
    
    @Override
    public long countHotPosts() {
        Long count = postDao.countHotPosts();
        return count != null ? count : 0;
    }

    @Override
    public List<PostEntity> findByTagId(Long tagId, int offset, int limit) {
        if (tagId == null) {
            return Collections.emptyList();
        }
        int safeOffset = Math.max(0, offset);
        List<Post> posts = postDao.findPostsByTagId(tagId, safeOffset, limit);
        return PostConverter.toDomainEntities(posts);
    }
    
    @Override
    public long countByTagId(Long tagId) {
        if (tagId == null) {
            return 0;
        }
        Long count = postDao.countPostsByTagId(tagId);
        return count != null ? count : 0;
    }

    @Override
    public List<PostEntity> findByUserIdAndStatus(Long userId, String postStatus, int offset, int limit) {
        if (userId == null || postStatus == null) {
            return Collections.emptyList();
        }
        int safeOffset = Math.max(0, offset);
        List<Post> posts = postDao.findPostsByUserIdAndStatus(userId, postStatus, safeOffset, limit);
        return PostConverter.toDomainEntities(posts);
    }

    @Override
    public List<PostPageResponse> queryByPage(SysPostQueryRequest postRequest) {
        return postDao.queryByPage(postRequest);
    }
    
    @Override
    public List<PostEntity> findFeaturedPosts(int offset, int limit) {
        List<Post> posts = postDao.findFeaturedPosts(offset, limit);
        return PostConverter.toDomainEntities(posts);
    }
    
    @Override
    public long countFeaturedPosts() {
        Long count = postDao.countFeaturedPosts();
        return count != null ? count : 0;
    }
    
    @Override
    public List<PostEntity> findAnswersByQuestionId(Long questionId, int offset, int limit) {
        if (questionId == null) {
            return Collections.emptyList();
        }
        int safeOffset = Math.max(0, offset);
        List<Post> posts = postDao.findAnswersByQuestionId(questionId, safeOffset, limit);
        return PostConverter.toDomainEntities(posts);
    }
    
    @Override
    public long countByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }
        Long count = postDao.countPostsByUserIds(userIds);
        return count != null ? count : 0;
    }
    
    @Override
    public long countAll() {
        Long count = postDao.countAll();
        return count != null ? count : 0;
    }

    @Override
    public List<PostEntity> findRelatedPostsByType(PostType postType, Long excludePostId, int limit) {
        if (postType == null || limit <= 0) {
            return Collections.emptyList();
        }
        
        int safeLimit = Math.min(limit, 50); // 限制最大返回数量
        List<Post> posts = postDao.findRelatedPostsByType(postType.name(), excludePostId, safeLimit);
        return PostConverter.toDomainEntities(posts);
    }
}