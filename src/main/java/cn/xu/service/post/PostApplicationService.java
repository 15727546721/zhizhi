package cn.xu.service.post;

import cn.xu.common.request.CursorPageRequest;
import cn.xu.common.response.CursorPageResponse;
import cn.xu.common.response.PageResponse;
import cn.xu.model.dto.search.SearchFilter;
import cn.xu.model.entity.Like;
import cn.xu.model.entity.Post;
import cn.xu.model.entity.Tag;
import cn.xu.model.entity.User;
import cn.xu.model.vo.post.PostDetailVO;
import cn.xu.model.vo.post.PostListVO;
import cn.xu.model.vo.post.PostSearchResponseVO;
import cn.xu.model.vo.tag.TagVO;
import cn.xu.model.vo.user.UserVO;
import cn.xu.service.favorite.FavoriteService;
import cn.xu.service.follow.FollowService;
import cn.xu.service.like.LikeService;
import cn.xu.service.search.PostSearchService;
import cn.xu.service.user.UserService;
import cn.xu.support.exception.BusinessException;
import cn.xu.common.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 帖子应用服务（门面服务）
 * <p>
 * 职责：协调各个领域服务，为 Controller 提供统一的业务接口
 * <ul>
 *   <li>组合多个服务的调用</li>
 *   <li>处理复杂的业务流程</li>
 *   <li>数据转换和组装</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostApplicationService {

    private final PostQueryService postQueryService;
    private final PostCommandService postCommandService;
    private final PostStatisticsService postStatisticsService;
    private final PostConverter postConverter;
    private final PostValidationService postValidationService;
    private final TagService tagService;
    private final UserService userService;
    private final LikeService likeService;
    private final FavoriteService favoriteService;
    private final FollowService followService;
    private final PostSearchService postSearchService;

    // ==================== 游标分页查询（性能优化） ====================

    /**
     * 游标分页获取帖子列表
     * <p>
     * 相比传统 OFFSET 分页的优势：
     * <ul>
     *   <li>性能稳定：无论翻到第几页，查询性能一致</li>
     *   <li>数据一致：避免翻页时数据重复或遗漏</li>
     * </ul>
     *
     * @param request 游标分页请求
     * @return 游标分页响应
     */
    public CursorPageResponse<PostListVO> getPostsByCursor(CursorPageRequest request) {
        int limit = request.getSafePageSize();
        Long tagId = request.getTagId();
        String sortBy = request.getSortBy();
        
        List<Post> posts;
        String nextCursor;
        
        if ("hot".equalsIgnoreCase(sortBy)) {
            // 热门排序：使用复合游标（分数_ID）
            CursorPageRequest.CursorPair cursorPair = request.getCursorPair();
            Double cursorScore = cursorPair != null ? cursorPair.getScore() : null;
            Long cursorId = cursorPair != null ? cursorPair.getId() : null;
            
            posts = postQueryService.getByCursorHot(cursorScore, cursorId, tagId, limit + 1);
            
            // 构建下一页游标
            if (posts.size() > limit) {
                posts = posts.subList(0, limit);
                Post lastPost = posts.get(posts.size() - 1);
                double hotScore = calculateHotScore(lastPost);
                nextCursor = hotScore + "_" + lastPost.getId();
            } else {
                nextCursor = null;
            }
        } else {
            // 最新排序：使用 ID 作为游标
            Long cursorId = request.getCursorAsLong();
            
            posts = postQueryService.getByCursorLatest(cursorId, tagId, limit + 1);
            
            // 构建下一页游标
            if (posts.size() > limit) {
                posts = posts.subList(0, limit);
                Post lastPost = posts.get(posts.size() - 1);
                nextCursor = String.valueOf(lastPost.getId());
            } else {
                nextCursor = null;
            }
        }
        
        boolean hasMore = nextCursor != null;
        List<PostListVO> result = postConverter.toListVOs(posts);
        
        // 首次请求时返回总数
        Long total = null;
        if (request.getCursor() == null || request.getCursor().isEmpty()) {
            total = postStatisticsService.countAll();
        }
        
        return CursorPageResponse.of(result, nextCursor, hasMore, limit, total);
    }

    /**
     * 计算帖子热度分数
     */
    private double calculateHotScore(Post post) {
        long likeCount = post.getLikeCount() != null ? post.getLikeCount() : 0;
        long commentCount = post.getCommentCount() != null ? post.getCommentCount() : 0;
        long viewCount = post.getViewCount() != null ? post.getViewCount() : 0;
        long favoriteCount = post.getFavoriteCount() != null ? post.getFavoriteCount() : 0;
        return likeCount * 3 + commentCount * 5 + viewCount * 0.5 + favoriteCount * 4;
    }

    // ==================== 传统分页查询 ====================

    /**
     * 分页获取帖子列表
     */
    public PageResponse<List<PostListVO>> getPostsByPage(int pageNo, int pageSize) {
        postValidationService.validatePageParams(pageNo, pageSize);
        
        List<Post> posts = postQueryService.getAll(pageNo, pageSize);
        long total = postStatisticsService.countAll();
        List<PostListVO> result = postConverter.toListVOs(posts);
        
        return PageResponse.ofList(pageNo, pageSize, total, result);
    }

    /**
     * 获取帖子详情（含权限校验和浏览量统计）
     */
    public PostDetailVO getPostDetail(Long postId, Long currentUserId, String clientIp) {
        // 1. 增加浏览量（带防刷机制）
        postCommandService.viewPost(postId, currentUserId, clientIp);

        // 2. 获取帖子基本信息
        Post post = postQueryService.getById(postId)
                .orElseThrow(() -> new BusinessException(ResponseCode.UN_ERROR.getCode(), "帖子不存在"));

        // 3. 权限校验：只有已发布的帖子或自己的草稿才可以查看
        if (post.getStatus() != Post.STATUS_PUBLISHED) {
            if (currentUserId == null || !currentUserId.equals(post.getUserId())) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "无权限查看该帖子");
            }
        }

        // 4. 构建详情响应
        return buildPostDetailVO(post, currentUserId);
    }

    /**
     * 获取我的帖子列表
     */
    public PageResponse<List<PostListVO>> getMyPosts(Long userId, Integer pageNo, Integer pageSize, 
                                                      String status, String keyword) {
        postValidationService.validatePageParams(pageNo, pageSize);
        
        Integer statusCode = parseStatus(status);
        String trimmedKeyword = keyword != null ? keyword.trim() : null;
        if (trimmedKeyword != null && trimmedKeyword.isEmpty()) {
            trimmedKeyword = null;
        }
        
        List<Post> posts = postQueryService.getByUserIdWithKeyword(userId, statusCode, trimmedKeyword, pageNo, pageSize);
        long total = postStatisticsService.countByUserIdWithKeyword(userId, statusCode, trimmedKeyword);
        
        return PageResponse.ofList(pageNo, pageSize, total, postConverter.toListVOs(posts));
    }

    /**
     * 获取指定用户的帖子列表
     */
    public PageResponse<List<PostListVO>> getUserPosts(Long userId, Integer pageNo, Integer pageSize, String status) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "用户ID不能为空");
        }
        postValidationService.validatePageParams(pageNo, pageSize);
        
        int statusCode = "PUBLISHED".equals(status) ? Post.STATUS_PUBLISHED : Post.STATUS_DRAFT;
        List<Post> posts = postQueryService.getByUserIdAndStatus(userId, statusCode, pageNo, pageSize);
        long total = statusCode == Post.STATUS_PUBLISHED 
                ? postStatisticsService.countPublishedByUserId(userId) 
                : postStatisticsService.countDraftsByUserId(userId);
        
        return PageResponse.ofList(pageNo, pageSize, total, postConverter.toListVOs(posts));
    }

    /**
     * 获取我的草稿列表
     */
    public PageResponse<List<PostListVO>> getMyDrafts(Long userId, Integer pageNo, Integer pageSize) {
        postValidationService.validatePageParams(pageNo, pageSize);
        
        List<Post> drafts = postQueryService.getByUserIdAndStatus(userId, Post.STATUS_DRAFT, pageNo, pageSize);
        long total = postStatisticsService.countDraftsByUserId(userId);
        
        return PageResponse.ofList(pageNo, pageSize, total, postConverter.toListVOs(drafts));
    }

    /**
     * 获取收藏排行榜
     */
    public List<PostListVO> getFavoriteRanking(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        List<Post> posts = postQueryService.getByFavoriteCount(safeLimit);
        return postConverter.toListVOs(posts);
    }

    // ==================== 搜索操作 ====================

    /**
     * 搜索帖子
     */
    public PageResponse<List<PostSearchResponseVO>> searchPosts(String keyword, String[] types,
                                                                  String timeRange, String sortOption,
                                                                  int page, int size) {
        SearchFilter filter = buildSearchFilter(timeRange, sortOption);
        PostSearchService.SearchResult searchResult = postSearchService.executeSearch(keyword, filter, page, size);
        
        return PageResponse.ofList(
                searchResult.getPage(),
                searchResult.getSize(),
                searchResult.getTotal(),
                searchResult.getPosts()
        );
    }

    // ==================== 写操作 ====================

    /**
     * 创建帖子（支持直接发布或保存为草稿）
     */
    public Long createPost(Long userId, String title, String content, String description,
                           String coverUrl, List<Long> tagIds, String status) {
        postValidationService.validatePostPublishParams(title, content);
        postValidationService.validateTagIds(tagIds);

        if ("DRAFT".equals(status)) {
            Long postId = postCommandService.createDraft(userId, title, content, description, coverUrl, tagIds);
            log.info("草稿创建成功，ID:{}", postId);
            return postId;
        } else {
            Long postId = postCommandService.publishPost(null, userId, title, content, description, coverUrl, tagIds);
            log.info("帖子创建并发布成功，ID:{}", postId);
            return postId;
        }
    }

    /**
     * 发布/更新帖子
     */
    public Long publishOrUpdatePost(Long postId, Long userId, String title, String content,
                                     String description, String coverUrl, List<Long> tagIds, String status) {
        postValidationService.validatePostPublishParams(title, content);
        postValidationService.validateTagIds(tagIds);

        if ("DRAFT".equals(status)) {
            if (postId == null) {
                throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "帖子ID不能为空");
            }
            postCommandService.updateDraft(postId, userId, title, content, description, coverUrl, tagIds);
            log.info("草稿更新成功，ID:{}", postId);
            return postId;
        } else {
            Long resultId = postCommandService.publishPost(postId, userId, title, content, description, coverUrl, tagIds);
            log.info("帖子发布成功，ID:{}", resultId);
            return resultId;
        }
    }

    /**
     * 保存草稿
     */
    public Long saveDraft(Long userId, Long postId, String title, String content,
                          String description, String coverUrl, List<Long> tagIds) {
        if (postId != null) {
            postCommandService.updateDraft(postId, userId, title, content, description, coverUrl, tagIds);
            return postId;
        } else {
            return postCommandService.createDraft(userId, title, content, description, coverUrl, tagIds);
        }
    }

    /**
     * 删除草稿
     */
    public void deleteDraft(Long postId, Long userId) {
        Post post = postQueryService.getById(postId)
                .orElseThrow(() -> new BusinessException(ResponseCode.UN_ERROR.getCode(), "草稿不存在"));
        
        if (!userId.equals(post.getUserId())) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "无权删除此草稿");
        }
        if (post.getStatus() != Post.STATUS_DRAFT) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "只能删除草稿状态的帖子");
        }
        
        postCommandService.deletePost(postId, userId, false);
    }

    /**
     * 删除帖子
     */
    public void deletePost(Long postId, Long userId) {
        Post post = postQueryService.getById(postId)
                .orElseThrow(() -> new BusinessException(ResponseCode.UN_ERROR.getCode(), "帖子不存在"));
        
        if (!userId.equals(post.getUserId())) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "无权删除此帖子");
        }
        
        postCommandService.deletePost(postId, userId, false);
    }

    /**
     * 分享帖子
     */
    public void sharePost(Long postId) {
        postQueryService.getById(postId)
                .orElseThrow(() -> new BusinessException(ResponseCode.UN_ERROR.getCode(), "帖子不存在"));
        postCommandService.increaseShareCount(postId);
    }

    // ==================== 私有方法 ====================

    /**
     * 构建帖子详情 VO
     */
    private PostDetailVO buildPostDetailVO(Post post, Long currentUserId) {
        // 获取作者信息
        UserVO author = null;
        if (post.getUserId() != null) {
            try {
                User user = userService.getUserById(post.getUserId());
                author = postConverter.toUserVO(user);
            } catch (Exception e) {
                log.warn("获取作者信息失败: userId={}", post.getUserId(), e);
            }
        }

        // 获取标签信息
        List<TagVO> tags = Collections.emptyList();
        try {
            List<Tag> entityTags = tagService.getTagsByPostId(post.getId());
            tags = postConverter.toTagVOs(entityTags);
        } catch (Exception e) {
            log.warn("获取帖子标签失败: postId={}", post.getId(), e);
        }

        // 获取用户交互状态
        boolean isLiked = false, isFavorited = false, isFollowed = false, isAuthor = false;
        if (currentUserId != null) {
            isAuthor = currentUserId.equals(post.getUserId());
            isLiked = checkLikeStatus(currentUserId, post.getId());
            isFavorited = checkFavoriteStatus(currentUserId, post.getId());
            if (post.getUserId() != null && !isAuthor) {
                isFollowed = checkFollowStatus(currentUserId, post.getUserId());
            }
        }

        return PostDetailVO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())
                .content(post.getContent())
                .coverUrl(post.getCoverUrl())
                .author(author)
                .tags(tags)
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .favoriteCount(post.getFavoriteCount())
                .shareCount(post.getShareCount())
                .status(post.getStatus())
                .isFeatured(post.isFeaturedPost())
                .isLiked(isLiked)
                .isFavorited(isFavorited)
                .isFollowed(isFollowed)
                .createTime(post.getCreateTime())
                .updateTime(post.getUpdateTime())
                .build();
    }

    private boolean checkLikeStatus(Long userId, Long postId) {
        try {
            return likeService.checkStatus(userId, Like.LikeType.POST.getCode(), postId);
        } catch (Exception e) {
            log.debug("检查点赞状态失败: userId={}, postId={}", userId, postId);
            return false;
        }
    }

    private boolean checkFavoriteStatus(Long userId, Long postId) {
        try {
            return favoriteService.isFavorited(userId, postId, "POST");
        } catch (Exception e) {
            log.debug("检查收藏状态失败: userId={}, postId={}", userId, postId);
            return false;
        }
    }

    private boolean checkFollowStatus(Long userId, Long authorId) {
        try {
            return followService.isFollowed(userId, authorId);
        } catch (Exception e) {
            log.debug("检查关注状态失败: userId={}, authorId={}", userId, authorId);
            return false;
        }
    }

    private Integer parseStatus(String status) {
        if ("PUBLISHED".equals(status)) {
            return Post.STATUS_PUBLISHED;
        } else if ("DRAFT".equals(status)) {
            return Post.STATUS_DRAFT;
        }
        return null;
    }

    private SearchFilter buildSearchFilter(String timeRange, String sortOption) {
        SearchFilter.SearchFilterBuilder filterBuilder = SearchFilter.builder();

        // 处理时间范围
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime startTime = null;
        switch (timeRange != null ? timeRange.toLowerCase() : "all") {
            case "day":
                startTime = now.minusDays(1);
                break;
            case "week":
                startTime = now.minusWeeks(1);
                break;
            case "month":
                startTime = now.minusMonths(1);
                break;
            case "year":
                startTime = now.minusYears(1);
                break;
            default:
                break;
        }
        if (startTime != null) {
            filterBuilder.startTime(startTime);
            filterBuilder.endTime(now);
        }

        // 处理排序方式
        SearchFilter.SortOption sort = SearchFilter.SortOption.TIME;
        if (sortOption != null) {
            switch (sortOption.toLowerCase()) {
                case "hot":
                    sort = SearchFilter.SortOption.HOT;
                    break;
                case "comment":
                    sort = SearchFilter.SortOption.COMMENT;
                    break;
                case "like":
                    sort = SearchFilter.SortOption.LIKE;
                    break;
                default:
                    sort = SearchFilter.SortOption.TIME;
                    break;
            }
        }
        filterBuilder.sortOption(sort);

        return filterBuilder.build();
    }
}
