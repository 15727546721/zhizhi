package cn.xu.infrastructure.persistent.repository;

import cn.xu.api.system.model.dto.post.SysPostQueryRequest;
import cn.xu.api.web.model.vo.post.PostPageResponse;
import cn.xu.domain.post.model.aggregate.PostAggregate;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.valobj.PostType;
import cn.xu.domain.post.repository.IPostRepository;
import cn.xu.domain.post.service.PostHotScoreDomainService;
import cn.xu.infrastructure.cache.RedisService;
import cn.xu.infrastructure.persistent.converter.PostConverter;
import cn.xu.infrastructure.persistent.dao.PostMapper;
import cn.xu.infrastructure.persistent.dao.PostTagMapper;
import cn.xu.infrastructure.persistent.dao.PostTopicMapper;
import cn.xu.infrastructure.persistent.po.Post;
import cn.xu.infrastructure.persistent.po.PostTag;
import cn.xu.infrastructure.persistent.po.PostTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 帖子聚合根仓储实现
 * 遵循DDD原则，管理帖子聚合根的持久化操作
 */
@Slf4j
@Repository("postAggregateRepository")
@RequiredArgsConstructor
public class PostAggregateRepositoryImpl implements IPostRepository {
    
    private final PostMapper postMapper;
    private final PostTagMapper postTagMapper;
    private final PostTopicMapper postTopicMapper;
    private final PostConverter postConverter;
    private final RedisService redisService;
    private final PostHotScoreDomainService postHotScoreDomainService;
    
    @Override
    @Transactional
    public Long save(PostAggregate aggregate) {
        if (aggregate == null) {
            return null;
        }

        // 保存帖子实体
        PostEntity postEntity = aggregate.getPostEntity();
        Post post = postConverter.toDataObject(postEntity);
        postMapper.insert(post);
        
        // 更新聚合根ID
        Long postId = post.getId();
        aggregate.setId(postId);
        
        // 保存标签关联
        if (aggregate.getTagIds() != null && !aggregate.getTagIds().isEmpty()) {
            List<PostTag> postTags = aggregate.getTagIds().stream()
                .map(tagId -> PostTag.builder().postId(postId).tagId(tagId).build())
                .collect(Collectors.toList());
            postTagMapper.insertBatchByList(postTags);
        }
        
        // 保存话题关联
        if (aggregate.getTopicIds() != null && !aggregate.getTopicIds().isEmpty()) {
            List<PostTopic> postTopics = aggregate.getTopicIds().stream()
                .map(topicId -> PostTopic.builder().postId(postId).topicId(topicId).build())
                .collect(Collectors.toList());
            postTopicMapper.insertBatchByList(postTopics);
        }
        
        log.info("保存帖子聚合根成功, ID: {}", postId);
        return postId;
    }

    @Override
    @Transactional
    public void update(PostAggregate aggregate) {
        if (aggregate == null || aggregate.getId() == null) {
            return;
        }

        // 更新帖子实体
        PostEntity postEntity = aggregate.getPostEntity();
        Post post = postConverter.toDataObject(postEntity);
        postMapper.update(post);
        
        // 差量更新标签关联
        Long postId = aggregate.getId();
        List<Long> currentTagIds = postTagMapper.selectTagIdsByPostId(postId);
        List<Long> newTagIds = aggregate.getTagIds() != null ? aggregate.getTagIds() : new ArrayList<>();
        
        // 计算需要删除和新增的标签
        List<Long> toDelete = currentTagIds.stream()
                .filter(tagId -> !newTagIds.contains(tagId))
                .collect(Collectors.toList());
        List<Long> toAdd = newTagIds.stream()
                .filter(tagId -> !currentTagIds.contains(tagId))
                .collect(Collectors.toList());
        
        // 删除不再关联的标签
        if (!toDelete.isEmpty()) {
            postTagMapper.deleteByPostIdAndTagIds(postId, toDelete);
        }
        
        // 添加新的标签关联
        if (!toAdd.isEmpty()) {
            List<PostTag> postTags = toAdd.stream()
                .map(tagId -> PostTag.builder().postId(postId).tagId(tagId).build())
                .collect(Collectors.toList());
            postTagMapper.insertBatchByList(postTags);
        }
        
        // 差量更新话题关联
        List<Long> currentTopicIds = postTopicMapper.selectTopicIdsByPostId(postId);
        List<Long> newTopicIds = aggregate.getTopicIds() != null ? aggregate.getTopicIds() : new ArrayList<>();
        
        // 计算需要删除和新增的话题
        List<Long> topicsToDelete = currentTopicIds.stream()
                .filter(topicId -> !newTopicIds.contains(topicId))
                .collect(Collectors.toList());
        List<Long> topicsToAdd = newTopicIds.stream()
                .filter(topicId -> !currentTopicIds.contains(topicId))
                .collect(Collectors.toList());
        
        // 删除不再关联的话题
        if (!topicsToDelete.isEmpty()) {
            postTopicMapper.deleteByPostIdAndTopicIds(postId, topicsToDelete);
        }
        
        // 添加新的话题关联
        if (!topicsToAdd.isEmpty()) {
            List<PostTopic> postTopics = topicsToAdd.stream()
                .map(topicId -> PostTopic.builder().postId(postId).topicId(topicId).build())
                .collect(Collectors.toList());
            postTopicMapper.insertBatchByList(postTopics);
        }
        
        // 更新帖子热度分数
        updateHotScore(postId);
        
        log.info("更新帖子聚合根成功, ID: {}", postId);
    }

    @Override
    public Optional<PostAggregate> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        Post post = postMapper.findById(id);
        if (post == null) {
            return Optional.empty();
        }

        PostEntity postEntity = postConverter.toDomainEntity(post);
        List<Long> tagIds = postTagMapper.selectTagIdsByPostId(id);
        List<Long> topicIds = postTopicMapper.selectTopicIdsByPostId(id);
        
        PostAggregate aggregate = PostAggregate.builder()
                .id(id)
                .postEntity(postEntity)
                .tagIds(tagIds)
                .topicIds(topicIds)
                .acceptedAnswerId(post.getAcceptedAnswerId()) // 从PO中获取已采纳的回答ID
                .build();
        return Optional.of(aggregate);
    }

    @Override
    public void deleteByIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return;
        }

        // 删除标签关联
        postTagMapper.deleteByPostIds(postIds);
        
        // 删除帖子
        postMapper.deleteByIds(postIds);
        
        log.info("批量删除帖子聚合根成功, IDs: {}", postIds);
    }

    @Override
    public List<PostEntity> findAllPublished() {
        List<Post> posts = postMapper.findAllPublishedPosts();
        return postConverter.toDomainEntities(posts);
    }

    @Override
    public List<PostEntity> findAll() {
        List<Post> posts = postMapper.findAll();
        return postConverter.toDomainEntities(posts);
    }

    @Override
    public List<PostEntity> findByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        // 从Post PO对象转换为PostEntity对象
        List<Post> posts = postMapper.findByUserId(userId);
        return postConverter.toDomainEntities(posts);
    }

    @Override
    public List<PostEntity> findByUserIds(List<Long> userIds, int offset, int limit) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Post> posts = postMapper.getPostPageListByUserIds(userIds, offset, limit);
        return postConverter.toDomainEntities(posts);
    }

    @Override
    public List<PostEntity> findDraftsByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        // 从Post PO对象转换为PostEntity对象
        List<Post> posts = postMapper.findDraftPostListByUserId(userId);
        return postConverter.toDomainEntities(posts);
    }

    @Override
    public void incrementViewCount(Long postId) {
        if (postId == null) {
            return;
        }
        
        // 使用Redis缓存增加浏览量，避免频繁数据库写入
        try {
            // 使用Redis增加浏览量计数
            redisService.incrementViewCount(postId);
            
            // 直接更新数据库中的浏览量
            Long viewCount = redisService.getViewCount(postId);
            postMapper.updateViewCount(postId, viewCount);
        } catch (Exception e) {
            log.error("增加帖子浏览量失败: postId={}", postId, e);
            // 不抛出异常，避免影响主流程
        }
    }

    @Override
    public void updateHotScore(Long postId) {
        // 热度分数更新逻辑
        if (postId == null) {
            return;
        }
        
        // 从数据库获取帖子信息
        Post post = postMapper.findById(postId);
        if (post == null) {
            log.warn("更新帖子热度分数失败：帖子不存在 - postId: {}", postId);
            return;
        }
        
        // 转换为领域实体
        PostEntity postEntity = postConverter.toDomainEntity(post);
        
        // 调用专门的热度服务更新热度分数
        if (postHotScoreDomainService != null) {
            postHotScoreDomainService.updateHotScore(postId, postEntity);
        } else {
            log.warn("PostHotScoreDomainService未注入，无法更新帖子热度分数 - postId: {}", postId);
        }
        
        log.debug("帖子热度分数更新完成 - postId: {}", postId);
    }

    @Override
    public List<PostEntity> findByCategoryId(Long categoryId, int offset, int limit) {
        List<Post> posts = postMapper.getPostPageByCategory(categoryId, offset, limit);
        return postConverter.toDomainEntities(posts);
    }

    @Override
    public List<PostEntity> findAll(int offset, int limit) {
        List<Post> posts = postMapper.getPostPageList(offset, limit);
        return postConverter.toDomainEntities(posts);
    }
    
    /**
     * 分页查询帖子列表（支持排序）
     *
     * @param offset 偏移量
     * @param limit  数量
     * @param sortBy 排序方式 (newest, most_commented, most_bookmarked, most_liked, popular)
     * @return 帖子列表
     */
    @Override
    public List<PostEntity> findAllWithSort(int offset, int limit, String sortBy) {
        List<Post> posts = postMapper.getPostPageListWithSort(offset, limit, sortBy);
        return postConverter.toDomainEntities(posts);
    }

    @Override
    public List<PostEntity> searchByTitle(String title, int offset, int limit) {
        List<Post> posts = postMapper.searchPosts(title, offset, limit);
        return postConverter.toDomainEntities(posts);
    }

    @Override
    public long countSearchByTitle(String title) {
        Long count = postMapper.countSearchResults(title);
        return count != null ? count : 0;
    }

    @Override
    public List<PostEntity> searchByTitleWithFilters(String keyword, java.util.List<String> types,
                                                     java.time.LocalDateTime startTime, java.time.LocalDateTime endTime,
                                                     String sortBy, int offset, int limit) {
        List<Post> posts = postMapper.searchPostsWithFilters(keyword, types, startTime, endTime, sortBy, offset, limit);
        return postConverter.toDomainEntities(posts);
    }

    @Override
    public long countSearchByTitleWithFilters(String keyword, java.util.List<String> types,
                                              java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        Long count = postMapper.countSearchResultsWithFilters(keyword, types, startTime, endTime);
        return count != null ? count : 0;
    }

    @Override
    public List<PostEntity> findByType(PostType type, int offset, int limit) {
        List<Post> posts = postMapper.findPostsByType(type.name(), offset, limit);
        return postConverter.toDomainEntities(posts);
    }

    @Override
    public List<PostEntity> findHotPosts(int offset, int limit) {
        List<Post> posts = postMapper.findHotPosts(offset, limit);
        return postConverter.toDomainEntities(posts);
    }

    @Override
    public List<PostEntity> findByTagId(Long tagId, int offset, int limit) {
        List<Post> posts = postMapper.findPostsByTagId(tagId, offset, limit);
        return postConverter.toDomainEntities(posts);
    }

    @Override
    public List<PostEntity> findByUserIdAndStatus(Long userId, String postStatus, int offset, int limit) {
        // 确保 offset 不为负数
        int safeOffset = Math.max(0, offset);
        // 确保 limit 为正数
        int safeLimit = Math.max(1, limit);
        List<Post> posts = postMapper.findPostsByUserIdAndStatus(userId, postStatus, safeOffset, safeLimit);
        return postConverter.toDomainEntities(posts);
    }

    @Override
    public List<PostPageResponse> queryByPage(SysPostQueryRequest postRequest) {
        if (postRequest == null) {
            return Collections.emptyList();
        }

        // 计算偏移量
        int offset = (postRequest.getPageNo() - 1) * postRequest.getPageSize();
        postRequest.setOffset(offset);
        
        return postMapper.queryByPage(postRequest);
    }
    
    @Override
    public List<PostEntity> findFeaturedPosts(int offset, int limit) {
        List<Post> posts = postMapper.findFeaturedPosts(offset, limit);
        return postConverter.toDomainEntities(posts);
    }

    @Override
    public List<PostEntity> findPostsByIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Post> posts = postMapper.findPostsByIds(postIds);
        return postConverter.toDomainEntities(posts);
    }
    
    @Override
    public List<PostEntity> findAnswersByQuestionId(Long questionId, int offset, int limit) {
        List<Post> posts = postMapper.findAnswersByQuestionId(questionId, offset, limit);
        return postConverter.toDomainEntities(posts);
    }
    
    @Override
    public long countByType(PostType type) {
        if (type == null) {
            return 0;
        }
        Long count = postMapper.countPostsByType(type.name());
        return count != null ? count : 0;
    }
    
    @Override
    public long countHotPosts() {
        Long count = postMapper.countHotPosts();
        return count != null ? count : 0;
    }
    
    @Override
    public long countByTagId(Long tagId) {
        if (tagId == null) {
            return 0;
        }
        Long count = postMapper.countPostsByTagId(tagId);
        return count != null ? count : 0;
    }
    
    @Override
    public long countByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }
        Long count = postMapper.countPostsByUserIds(userIds);
        return count != null ? count : 0;
    }
    
    @Override
    public long countFeaturedPosts() {
        Long count = postMapper.countFeaturedPosts();
        return count != null ? count : 0;
    }
    
    @Override
    public long countAll() {
        Long count = postMapper.countAll();
        return count != null ? count : 0;
    }
    
    @Override
    public long countPublishedByUserId(Long userId) {
        if (userId == null) {
            return 0;
        }
        Long count = postMapper.countPublishedByUserId(userId);
        return count != null ? count : 0;
    }

    @Override
    public List<PostEntity> findRelatedPostsByType(PostType postType, Long excludePostId, int limit) {
        if (postType == null || limit <= 0) {
            return Collections.emptyList();
        }
        
        int safeLimit = Math.min(limit, 50); // 限制最大返回数量
        List<Post> posts = postMapper.findRelatedPostsByType(postType.name(), excludePostId, safeLimit);
        return postConverter.toDomainEntities(posts);
    }

    @Override
    public List<PostEntity> findPostsByTopicId(Long topicId, int offset, int limit) {
        List<Post> posts = postMapper.findPostsByTopicId(topicId, offset, limit);
        return postConverter.toDomainEntities(posts);
    }

    @Override
    public long countPostsByTopicId(Long topicId) {
        if (topicId == null) {
            return 0;
        }
        Long count = postMapper.countPostsByTopicId(topicId);
        return count != null ? count : 0;
    }
}