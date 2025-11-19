package cn.xu.domain.post.service;

import cn.xu.api.system.model.dto.post.SysPostQueryRequest;
import cn.xu.api.web.model.converter.PostVOConverter;
import cn.xu.api.web.model.dto.post.PostPageQueryRequest;
import cn.xu.api.web.model.vo.post.PostDetailResponse;
import cn.xu.api.web.model.vo.post.PostListResponse;
import cn.xu.api.web.model.vo.post.PostPageListResponse;
import cn.xu.api.web.model.vo.post.PostPageResponse;
import cn.xu.common.ResponseCode;
import cn.xu.common.exception.BusinessException;
import cn.xu.common.response.PageResponse;
import cn.xu.domain.comment.service.ICommentService;
import cn.xu.domain.favorite.model.valobj.TargetType;
import cn.xu.domain.favorite.service.IFavoriteService;
import cn.xu.domain.file.service.IFileStorageService;
import cn.xu.domain.follow.service.IFollowService;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.service.ILikeService;
import cn.xu.domain.post.model.aggregate.PostAggregate;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.entity.TagEntity;
import cn.xu.domain.post.model.entity.TopicEntity;
import cn.xu.domain.post.model.valobj.PostStatus;
import cn.xu.domain.post.model.valobj.PostType;
import cn.xu.domain.post.repository.IPostRepository;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import cn.xu.infrastructure.cache.RedisService;
import cn.xu.infrastructure.transaction.TransactionParticipant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings("unused")
public class PostService implements IPostService, TransactionParticipant {

    @Resource(name = "postAggregateRepository")
    private IPostRepository postRepository;
    @Resource
    private IUserService userService;
    @Resource
    private IPostTagService tagService;
    @Resource
    private ITopicService topicService;
    @Resource
    private IPostTopicService postTopicService;
    @Resource
    private ICommentService commentService;
    @Resource
    private IFileStorageService fileStorageService;
    @Resource
    private PostHotScoreDomainService postHotScoreDomainService;
    @Resource
    private PostQueryDomainService postQueryDomainService;
    @Resource
    private ILikeService likeService;
    @Resource
    private IFollowService followService;
    @Resource
    private IFavoriteService favoriteService;
    @Resource
    private RedisService redisService;
    @Resource
    private PostVOConverter postVOConverter;
    
    // 用于存储临时浏览量数据
    private final Map<Long, Long> viewCountBuffer = new HashMap<>();
    private final Object bufferLock = new Object();

    // 用于事务管理的临时存储
    private final ThreadLocal<PostOperation> tempOperation = new ThreadLocal<>();

    @Override
    public Long createPost(PostEntity postEntity) {
        // 创建帖子聚合根
        PostAggregate postAggregate = PostAggregate.builder()
                .postEntity(postEntity)
                .build();
        
        // 验证聚合根
        try {
            postAggregate.validateForCreation();
        } catch (Exception e) {
            log.error("帖子创建验证失败: {}", e.getMessage());
            throw e;
        }
        
        // 如果在事务中，暂存操作
        if (tempOperation.get() != null) {
            tempOperation.get().setOperation(PostOperationType.CREATE);
            tempOperation.get().setPostAggregate(postAggregate);
            log.debug("在事务中暂存帖子创建操作: userId={}", postEntity.getUserId());
            return null; // 在事务提交前不返回ID
        }
        
        // 保存到数据库
        return postRepository.save(postAggregate);
    }
    
    /**
     * 创建带话题的帖子
     * 
     * @param postEntity 帖子实体
     * @param topicIds 话题ID列表
     * @return 帖子ID
     */
    public Long createPostWithTopics(PostEntity postEntity, List<Long> topicIds) {
        // 创建帖子聚合根
        PostAggregate postAggregate = PostAggregate.builder()
                .postEntity(postEntity)
                .topicIds(topicIds)
                .build();
        
        // 验证聚合根
        try {
            postAggregate.validateForCreation();
        } catch (Exception e) {
            log.error("帖子创建验证失败: {}", e.getMessage());
            throw e;
        }
        
        // 如果在事务中，暂存操作
        if (tempOperation.get() != null) {
            tempOperation.get().setOperation(PostOperationType.CREATE);
            tempOperation.get().setPostAggregate(postAggregate);
            log.debug("在事务中暂存帖子创建操作: userId={}", postEntity.getUserId());
            return null; // 在事务提交前不返回ID
        }
        
        // 保存到数据库
        return postRepository.save(postAggregate);
    }

    @Override
    public Long createOrUpdatePostDraft(PostEntity postEntity) {
        // 如果ID为空，创建新草稿
        if (postEntity.getId() == null) {
            postEntity.setStatus(PostStatus.DRAFT);
            PostAggregate postAggregate = PostAggregate.builder()
                    .postEntity(postEntity)
                    .build();
            
            try {
                postAggregate.validateForCreation();
            } catch (Exception e) {
                log.error("草稿创建验证失败: {}", e.getMessage());
                throw e;
            }
            
            // 如果在事务中，暂存操作
            if (tempOperation.get() != null) {
                tempOperation.get().setOperation(PostOperationType.CREATE_DRAFT);
                tempOperation.get().setPostAggregate(postAggregate);
                log.debug("在事务中暂存草稿创建操作: userId={}", postEntity.getUserId());
                return null; // 在事务提交前不返回ID
            }
            
            return postRepository.save(postAggregate);
        } else {
            // 更新现有草稿
            Optional<PostAggregate> existingAggregateOpt = postRepository.findById(postEntity.getId());
            if (!existingAggregateOpt.isPresent()) {
                throw new IllegalArgumentException("帖子不存在");
            }
            
            PostAggregate existingAggregate = existingAggregateOpt.get();
            
            // 验证权限
            existingAggregate.validateOwnership(postEntity.getUserId());
            
            // 更新内容
            existingAggregate.updatePost(postEntity);
            
            // 如果在事务中，暂存操作
            if (tempOperation.get() != null) {
                tempOperation.get().setOperation(PostOperationType.UPDATE);
                tempOperation.get().setPostAggregate(existingAggregate);
                log.debug("在事务中暂存帖子更新操作: postId={}", postEntity.getId());
                return existingAggregate.getId();
            }
            
            postRepository.update(existingAggregate);
            return existingAggregate.getId();
        }
    }

    @Override
    public String uploadCover(MultipartFile imageFile) {
        // 实现封面上传逻辑
        try {
            // 生成文件名前缀：posts/covers/年月/文件名
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            String fileName = "posts/covers/" + datePath + "/" + System.currentTimeMillis() + "_" + 
                             Objects.requireNonNull(imageFile.getOriginalFilename());
            
            // 上传文件到MinIO
            return fileStorageService.uploadFile(imageFile, fileName);
        } catch (Exception e) {
            log.error("上传封面图片失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "上传封面图片失败: " + e.getMessage());
        }
    }

    @Override
    public PageResponse<List<PostPageResponse>> listPost(SysPostQueryRequest postRequest) {
        // 实现帖子列表查询逻辑
        try {
            // 计算分页参数
            int pageNo = postRequest.getPageNo();
            int pageSize = postRequest.getPageSize();
            postRequest.setPageNo(pageNo - 1); // 调整页码
            
            // 查询帖子数据
            List<PostPageResponse> posts = postRepository.queryByPage(postRequest);
            
            // 返回分页响应
            return PageResponse.<List<PostPageResponse>>builder()
                    .pageNo(pageNo)
                    .pageSize(pageSize)
                    .total((long) posts.size())
                    .data(posts)
                    .build();
        } catch (Exception e) {
            log.error("查询帖子列表失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询帖子列表失败: " + e.getMessage());
        }
    }

    @Override
    public void deletePosts(List<Long> postIds) {
        // 如果在事务中，暂存操作
        if (tempOperation.get() != null) {
            tempOperation.get().setOperation(PostOperationType.DELETE_BATCH);
            tempOperation.get().setPostIds(postIds);
            log.debug("在事务中暂存批量删除帖子操作: postIds={}", postIds);
            return;
        }
        
        postRepository.deleteByIds(postIds);
    }

    @Override
    public void updatePost(PostEntity postEntity) {
        // 获取现有帖子以验证权限和获取现有话题
        Optional<PostAggregate> existingAggregateOpt = postRepository.findById(postEntity.getId());
        if (!existingAggregateOpt.isPresent()) {
            throw new IllegalArgumentException("帖子不存在");
        }
        
        PostAggregate existingAggregate = existingAggregateOpt.get();
        existingAggregate.validateOwnership(postEntity.getUserId());
        
        // 更新帖子内容
        existingAggregate.updatePost(postEntity);
        
        PostAggregate postAggregate = PostAggregate.builder()
                .id(existingAggregate.getId())
                .postEntity(existingAggregate.getPostEntity())
                .tagIds(existingAggregate.getTagIds())
                .topicIds(existingAggregate.getTopicIds())
                .acceptedAnswerId(existingAggregate.getAcceptedAnswerId())
                .build();
        
        try {
            // 验证帖子实体
            if (postEntity.getId() == null) {
                throw new IllegalArgumentException("帖子ID不能为空");
            }
        } catch (Exception e) {
            log.error("帖子更新验证失败: {}", e.getMessage());
            throw e;
        }
        
        // 如果在事务中，暂存操作
        if (tempOperation.get() != null) {
            tempOperation.get().setOperation(PostOperationType.UPDATE);
            tempOperation.get().setPostAggregate(postAggregate);
            log.debug("在事务中暂存帖子更新操作: postId={}", postEntity.getId());
            return;
        }
        
        postRepository.update(postAggregate);
    }

    @Override
    public List<PostEntity> getAllPublishedPosts() {
        return postRepository.findAllPublished();
    }

    @Override
    public List<PostEntity> getAllPosts() {
        return postRepository.findAll();
    }

    @Override
    public Optional<PostAggregate> findById(Long postId) {
        return postRepository.findById(postId);
    }

    @Override
    public List<PostListResponse> getPostsByUserId(Long userId) {
        List<PostEntity> posts = postRepository.findByUserId(userId);
        List<PostListResponse> result = new ArrayList<>();
        for (PostEntity post : posts) {
            PostListResponse vo = new PostListResponse();
            vo.setPost(post);
            result.add(vo);
        }
        return result;
    }

    @Override
    public Optional<PostEntity> findPostEntityById(Long postId) {
        Optional<PostAggregate> postAggregateOpt = postRepository.findById(postId);
        if (!postAggregateOpt.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(postAggregateOpt.get().getPostEntity());
    }

    @Override
    public List<PostEntity> getUserPosts(Long userId, String postStatus, int pageNo, int pageSize) {
        // 确保 pageNo 至少为 1
        int safePageNo = Math.max(1, pageNo);
        // 确保 pageSize 至少为 1
        int safePageSize = Math.max(1, pageSize);
        // 计算偏移量
        int offset = (safePageNo - 1) * safePageSize;
        // 查询用户帖子
        List<PostEntity> posts = postRepository.findByUserIdAndStatus(userId, postStatus, offset, safePageSize);
        if (posts == null) {
            return new ArrayList<>(); // 返回空列表而不是抛出异常
        }
        return posts;
    }

    @Override
    public void publishPost(PostEntity postEntity, Long userId) {
        Optional<PostAggregate> postAggregateOpt = postRepository.findById(postEntity.getId());
        if (!postAggregateOpt.isPresent()) {
            throw new IllegalArgumentException("帖子不存在");
        }
        
        PostAggregate postAggregate = postAggregateOpt.get();
        
        // 验证权限
        postAggregate.validateOwnership(userId);
        
        // 更新内容并发布
        postAggregate.updatePost(postEntity);
        
        try {
            // 发布前验证
            postAggregate.getPostEntity().validateForCreation();
        } catch (Exception e) {
            log.error("帖子发布验证失败: {}", e.getMessage());
            throw e;
        }
        
        postAggregate.publish();
        
        // 如果在事务中，暂存操作
        if (tempOperation.get() != null) {
            tempOperation.get().setOperation(PostOperationType.PUBLISH);
            tempOperation.get().setPostAggregate(postAggregate);
            log.debug("在事务中暂存帖子发布操作: postId={}", postEntity.getId());
            return;
        }
        
        postRepository.update(postAggregate);
    }

    @Override
    public List<PostListResponse> getDraftPostList(Long userId) {
        List<PostEntity> draftPosts = postRepository.findDraftsByUserId(userId);
        List<PostListResponse> result = new ArrayList<>();
        for (PostEntity post : draftPosts) {
            PostListResponse vo = new PostListResponse();
            vo.setPost(post);
            result.add(vo);
        }
        return result;
    }

    @Override
    public void deletePost(Long id, Long userId) {
        Optional<PostAggregate> postAggregateOpt = postRepository.findById(id);
        if (!postAggregateOpt.isPresent()) {
            throw new IllegalArgumentException("帖子不存在");
        }
        
        PostAggregate postAggregate = postAggregateOpt.get();
        
        // 验证权限
        postAggregate.validateOwnership(userId);
        
        // 删除帖子
        postAggregate.delete();
        
        // 如果在事务中，暂存操作
        if (tempOperation.get() != null) {
            tempOperation.get().setOperation(PostOperationType.DELETE);
            tempOperation.get().setPostAggregate(postAggregate);
            log.debug("在事务中暂存帖子删除操作: postId={}", id);
            return;
        }
        
        postRepository.update(postAggregate);
    }
    
    public void viewPost(Long postId) {
        // 使用异步方式增加浏览量，避免阻塞主线程
        // 实际的浏览量更新可以通过定时任务批量处理
        postRepository.incrementViewCount(postId);
    }
    
    @Override
    public void viewPost(Long postId, String clientIp, Long userId) {
        boolean shouldIncrement = false;
        
        // 对于未登录用户，基于IP限制
        if (userId == null) {
            String ipKey = "post:view:ip:" + postId + ":" + clientIp;
            if (!redisService.hasKey(ipKey)) {
                shouldIncrement = true;
                // 设置IP访问记录，10分钟过期
                redisService.set(ipKey, "1", 600);
            }
        } else {
            // 对于登录用户，基于用户ID限制
            String userKey = "post:view:user:" + postId + ":" + userId;
            if (!redisService.hasKey(userKey)) {
                shouldIncrement = true;
                // 设置用户访问记录，10分钟过期
                redisService.set(userKey, "1", 600);
            }
        }
        
        if (shouldIncrement) {
            // 如果在事务中，暂存操作
            if (tempOperation.get() != null) {
                tempOperation.get().setOperation(PostOperationType.VIEW);
                tempOperation.get().setPostId(postId);
                log.debug("在事务中暂存帖子浏览操作: postId={}", postId);
                return;
            }
            
            postRepository.incrementViewCount(postId);
        }
    }

    @Override
    public void updatePostHotScore(Long postId) {
        if (postId == null) {
            log.warn("更新帖子热度失败：postId为空");
            return;
        }

        try {
            postRepository.findById(postId).ifPresent(postAggregate -> {
                PostEntity postEntity = postAggregate.getPostEntity();
                if (postEntity == null) {
                    log.warn("更新帖子热度失败：postId={} 对应实体为空", postId);
                    return;
                }
                postHotScoreDomainService.updateHotScore(postId, postEntity);
            });
        } catch (Exception e) {
            log.error("更新帖子热度分数异常，postId={}", postId, e);
        }
    }

    @Override
    public PostDetailResponse getPostDetail(Long id, Long currentUserId) {
        Optional<PostAggregate> postAggregateOpt = postRepository.findById(id);
        if (!postAggregateOpt.isPresent()) {
            return null;
        }
        
        PostEntity post = postAggregateOpt.get().getPostEntity();
        
        // 查询作者信息
        UserEntity author = userService.getUserById(post.getUserId());
        
        // 查询标签信息
        List<TagEntity> tags = null;
        if (tagService != null) {
            tags = tagService.getTagsByPostId(id);
        }
        
        // 查询话题信息
        List<Long> topicIds = null;
        List<TopicEntity> topics = null;
        if (postTopicService != null) {
            topicIds = postTopicService.getTopicsByPostId(id);
            if (topicService != null && topicIds != null && !topicIds.isEmpty()) {
                topics = topicService.batchGetTopics(topicIds);
            }
        }
        
        // 设置用户相关状态
        boolean isLiked = false;
        boolean isFavorited = false;
        boolean isAuthor = false;
        boolean isFollowed = false;
        
        if (currentUserId != null) {
            // 是否已点赞
            try {
                isLiked = likeService != null ? likeService.checkStatus(currentUserId, LikeType.POST, id) : false;
            } catch (Exception e) {
                log.warn("检查点赞状态失败: userId={}, postId={}", currentUserId, id, e);
            }
            
            // 是否已收藏 - 根据post的type字段映射到对应的targetType
            try {
                if (favoriteService != null && post.getType() != null) {
                    // 将PostType映射到TargetType
                    String targetTypeCode = mapPostTypeToTargetType(post.getType());
                    isFavorited = favoriteService.isFavorited(currentUserId, id, targetTypeCode);
                    log.debug("查询收藏状态: userId={}, postId={}, postType={}, targetType={}, isFavorited={}", 
                            currentUserId, id, post.getType(), targetTypeCode, isFavorited);
                } else {
                    isFavorited = false;
                }
            } catch (Exception e) {
                log.warn("检查收藏状态失败: userId={}, postId={}", currentUserId, id, e);
            }
            
            isAuthor = currentUserId.equals(post.getUserId());
            
            if (!isAuthor && followService != null) {
                try {
                    isFollowed = followService.isFollowing(currentUserId, post.getUserId());
                } catch (Exception e) {
                    log.warn("检查关注状态失败: userId={}, targetId={}", currentUserId, post.getUserId(), e);
                }
            }
        }
        
        return postVOConverter.convertToPostDetailResponse(
                post,
                author,
                null,
                tags,
                topicIds,
                topics,
                post.getAcceptedAnswerId(),
                isLiked,
                isFavorited,
                isAuthor,
                isFollowed
        );
    }

    /**
     * 所有类型的帖子都使用POST作为TargetType，因为都是post表的记录
     * @param postType PostType枚举（此参数保留以便将来扩展）
     * @return TargetType的API代码
     */
    private String mapPostTypeToTargetType(PostType postType) {
        // 所有类型的帖子都统一使用POST类型，因为都是post表的记录
        // post表的type字段（POST、ARTICLE、DISCUSSION等）不影响收藏逻辑
        return TargetType.POST.getApiCode();
    }
    
    @Override
    public long countAllPosts() {
        return postRepository.countAll();
    }
    
    /**
     * 增加帖子分享数
     * @param postId 帖子ID
     */
    public void increasePostShareCount(Long postId) {
        Optional<PostAggregate> postAggregateOpt = postRepository.findById(postId);
        if (postAggregateOpt.isPresent()) {
            PostAggregate postAggregate = postAggregateOpt.get();
            PostEntity postEntity = postAggregate.getPostEntity();
            postEntity.increaseShareCount();
            postRepository.update(postAggregate);
        }
    }

    @Override
    public List<PostEntity> getPostPageListByCategoryId(Long categoryId, Integer pageNo, Integer pageSize) {
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        return postRepository.findByCategoryId(categoryId, offset, pageSize);
    }

    @Override
    public List<PostEntity> getPostPageList(Integer pageNo, Integer pageSize) {
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        return postRepository.findAll(offset, pageSize);
    }

    @Override
    public List<PostEntity> getPostPageList(PostPageQueryRequest request) {
        return postQueryDomainService.queryPosts(request);
    }

    @Override
    public List<PostPageListResponse> getPostPageListWithSort(PostPageQueryRequest request) {
        // 查询帖子列表
        List<PostEntity> postList = getPostPageList(request);
        
        // 获取用户信息
        List<Long> userIds = postList.stream()
                .map(PostEntity::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        
        List<UserEntity> userList = userService.batchGetUserInfo(userIds);
        Map<Long, UserEntity> userMap = userList.stream()
                .collect(Collectors.toMap(UserEntity::getId, user -> user));
        
        // 批量获取帖子标签
        List<Long> postIds = postList.stream().map(PostEntity::getId).collect(Collectors.toList());
        Map<Long, String[]> postTagsMap = new HashMap<>();
        if (tagService != null && !postIds.isEmpty()) {
            try {
                List<IPostTagService.PostTagRelation> postTagRelations = tagService.batchGetTagIdsByPostIds(postIds);
                for (IPostTagService.PostTagRelation relation : postTagRelations) {
                    Long postId = relation.getPostId();
                    String[] tags;
                    if (relation.getTagIds() != null) {
                        tags = relation.getTagIds().stream()
                                .map(String::valueOf)
                                .toArray(String[]::new);
                    } else {
                        tags = new String[0];
                    }
                    postTagsMap.put(postId, tags);
                }
            } catch (Exception e) {
                log.warn("批量获取帖子标签信息失败", e);
            }
        }

        // 批量获取帖子话题
        Map<Long, List<TopicEntity>> postTopicsMap = new HashMap<>();
        if (postTopicService != null && !postIds.isEmpty()) {
            try {
                List<IPostTopicService.PostTopicRelation> relations = postTopicService.batchGetTopicIdsByPostIds(postIds);
                // 获取所有涉及的话题ID
                Set<Long> allTopicIds = relations.stream()
                        .flatMap(r -> r.getTopicIds().stream())
                        .collect(Collectors.toSet());
                
                if (!allTopicIds.isEmpty()) {
                    List<TopicEntity> topics = topicService.batchGetTopics(new ArrayList<>(allTopicIds));
                    Map<Long, TopicEntity> topicMap = topics.stream()
                            .collect(Collectors.toMap(TopicEntity::getId, t -> t));
                    
                    for (IPostTopicService.PostTopicRelation r : relations) {
                        List<TopicEntity> postTopics = r.getTopicIds().stream()
                                .map(topicMap::get)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        postTopicsMap.put(r.getPostId(), postTopics);
                    }
                }
            } catch (Exception e) {
                log.warn("批量获取帖子话题信息失败", e);
            }
        }
    
        // 转换为VO对象
        return postList.stream()
                .map(post -> PostPageListResponse.builder()
                        .post(post)
                        .user(userMap.get(post.getUserId()))
                        .tags(postTagsMap.getOrDefault(post.getId(), new String[0]))
                        .topics(postTopicsMap.getOrDefault(post.getId(), new ArrayList<>()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void acceptAnswer(Long postId, Long answerId, Long userId) {
        Optional<PostAggregate> postAggregateOpt = postRepository.findById(postId);
        if (!postAggregateOpt.isPresent()) {
            throw new IllegalArgumentException("帖子不存在");
        }
        
        PostAggregate postAggregate = postAggregateOpt.get();
        
        // 验证是否为问答帖
        if (!postAggregate.isQuestion()) {
            throw new IllegalStateException("只有问答帖才能采纳回答");
        }
        
        // 验证权限（只有提问者才能采纳回答）
        postAggregate.validateOwnership(userId);
        
        // 验证回答是否存在且属于当前问题
        if (answerId == null) {
            throw new IllegalArgumentException("回答ID不能为空");
        }
        
        // 设置已采纳的回答
        postAggregate.setAcceptedAnswer(answerId);
        
        // 如果在事务中，暂存操作
        if (tempOperation.get() != null) {
            tempOperation.get().setOperation(PostOperationType.ACCEPT_ANSWER);
            tempOperation.get().setPostAggregate(postAggregate);
            tempOperation.get().setAnswerId(answerId);
            log.debug("在事务中暂存采纳回答操作: postId={}, answerId={}", postId, answerId);
            return;
        }
        
        postRepository.update(postAggregate);
    }
    
    @Override
    public List<PostEntity> findPostsByType(PostType type, Integer pageNo, Integer pageSize) {
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        return postRepository.findByType(type, offset, pageSize);
    }
    
    @Override
    public List<PostEntity> findRelatedPostsByType(PostType postType, Long excludePostId, int limit) {
        // 实现根据帖子类型查找相关帖子的逻辑
        if (postType == null || limit <= 0) {
            return new ArrayList<>();
        }
        
        // 调用仓储层方法获取相关帖子
        return postRepository.findRelatedPostsByType(postType, excludePostId, limit);
    }

    @Override
    public long countPostsByType(PostType postType) {
        // 实现统计指定类型的帖子数量
        return postRepository.countByType(postType);
    }
    
    @Override
    public List<PostEntity> findHotPosts(Integer pageNo, Integer pageSize) {
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        
        // 先尝试从缓存获取热门帖子ID
        List<String> hotPostIds = postHotScoreDomainService.getTopNHotPosts(pageSize);
        if (!hotPostIds.isEmpty()) {
            // 根据ID获取帖子详情
            List<Long> postIds = hotPostIds.stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            return postRepository.findPostsByIds(postIds);
        }
        
        // 缓存未命中，从数据库查询
        return postRepository.findHotPosts(offset, pageSize);
    }
    
    @Override
    public long countHotPosts() {
        return postRepository.countHotPosts();
    }
    
    @Override
    public List<PostEntity> findPostsByTagId(Long tagId, Integer pageNo, Integer pageSize) {
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        return postRepository.findByTagId(tagId, offset, pageSize);
    }
    
    @Override
    public long countPostsByTagId(Long tagId) {
        return postRepository.countByTagId(tagId);
    }
    
    @Override
    public List<PostEntity> getPostsByUserIds(List<Long> userIds, Integer pageNo, Integer pageSize) {
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        return postRepository.findByUserIds(userIds, offset, pageSize);
    }
    
    @Override
    public long countPostsByUserIds(List<Long> userIds) {
        return postRepository.countByUserIds(userIds);
    }
    
    /**
     * 查询精选帖子列表
     * 
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 帖子列表
     */
    @Override
    public List<PostEntity> findFeaturedPosts(Integer pageNo, Integer pageSize) {
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        return postRepository.findFeaturedPosts(offset, pageSize);
    }
    
    @Override
    public long countFeaturedPosts() {
        return postRepository.countFeaturedPosts();
    }
    
    @Override
    public List<PostEntity> findAnswersByQuestionId(Long questionId, Integer pageNo, Integer pageSize) {
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        return postRepository.findAnswersByQuestionId(questionId, offset, pageSize);
    }
    
    @Override
    public long countPublishedByUserId(Long userId) {
        if (userId == null) {
            return 0;
        }
        return postRepository.countPublishedByUserId(userId);
    }

    @Override
    public List<PostEntity> findPostsByIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return new ArrayList<>();
        }
        return postRepository.findPostsByIds(postIds);
    }
    
    /**
     * 获取前N篇热度最高的帖子
     * @param topN 获取的帖子数量
     * @return 热度前N篇帖子的ID
     */
    public List<Long> findTopNHotPosts(int topN) {
        List<String> hotPostIds = postHotScoreDomainService.getTopNHotPosts(topN);
        return hotPostIds.stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PostEntity> findPostsByTopicId(Long topicId, Integer pageNo, Integer pageSize) {
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        return postRepository.findPostsByTopicId(topicId, offset, pageSize);
    }

    @Override
    public long countPostsByTopicId(Long topicId) {
        return postRepository.countPostsByTopicId(topicId);
    }

    @Override
    public void commit() throws Exception {
        PostOperation operation = tempOperation.get();
        if (operation != null) {
            try {
                switch (operation.getOperation()) {
                    case CREATE:
                        postRepository.save(operation.getPostAggregate());
                        log.info("提交帖子创建操作");
                        break;
                    case CREATE_DRAFT:
                        postRepository.save(operation.getPostAggregate());
                        log.info("提交草稿创建操作");
                        break;
                    case UPDATE:
                        postRepository.update(operation.getPostAggregate());
                        log.info("提交帖子更新操作");
                        break;
                    case DELETE_BATCH:
                        postRepository.deleteByIds(operation.getPostIds());
                        log.info("提交批量删除帖子操作");
                        break;
                    case PUBLISH:
                        postRepository.update(operation.getPostAggregate());
                        log.info("提交帖子发布操作");
                        break;
                    case DELETE:
                        postRepository.update(operation.getPostAggregate());
                        log.info("提交帖子删除操作");
                        break;
                    case VIEW:
                        postRepository.incrementViewCount(operation.getPostId());
                        log.info("提交帖子浏览操作");
                        break;
                    case ACCEPT_ANSWER:
                        postRepository.update(operation.getPostAggregate());
                        log.info("提交采纳回答操作");
                        break;
                    default:
                        log.warn("未知的帖子操作类型: {}", operation.getOperation());
                }
            } catch (Exception e) {
                log.error("提交帖子操作失败: {}", operation.getOperation(), e);
                throw e; // 重新抛出异常以触发回滚
            } finally {
                tempOperation.remove();
            }
        }
    }
    
    @Override
    public void rollback() throws Exception {
        PostOperation operation = tempOperation.get();
        if (operation != null) {
            log.info("回滚帖子操作: {}", operation.getOperation());
            tempOperation.remove();
        }
    }
    
    /**
     * 开始事务
     */
    public void beginTransaction() {
        tempOperation.set(new PostOperation());
        log.debug("开始帖子事务");
    }
    
    /**
     * 提交事务
     */
    public void commitTransaction() throws Exception {
        commit();
    }
    
    /**
     * 回滚事务
     */
    public void rollbackTransaction() throws Exception {
        rollback();
    }
    
    /**
     * 帖子操作类型枚举
     */
    private enum PostOperationType {
        CREATE, CREATE_DRAFT, UPDATE, DELETE_BATCH, PUBLISH, DELETE, VIEW, ACCEPT_ANSWER
    }
    
    /**
     * 帖子操作封装类
     */
    private static class PostOperation {
        private PostOperationType operation;
        private PostAggregate postAggregate;
        private List<Long> postIds;
        private Long postId;
        private Long answerId;
        
        public PostOperationType getOperation() {
            return operation;
        }
        
        public void setOperation(PostOperationType operation) {
            this.operation = operation;
        }
        
        public PostAggregate getPostAggregate() {
            return postAggregate;
        }
        
        public void setPostAggregate(PostAggregate postAggregate) {
            this.postAggregate = postAggregate;
        }
        
        public List<Long> getPostIds() {
            return postIds;
        }
        
        public void setPostIds(List<Long> postIds) {
            this.postIds = postIds;
        }
        
        public Long getPostId() {
            return postId;
        }
        
        public void setPostId(Long postId) {
            this.postId = postId;
        }
        
        public Long getAnswerId() {
            return answerId;
        }
        
        public void setAnswerId(Long answerId) {
            this.answerId = answerId;
        }
    }
}
