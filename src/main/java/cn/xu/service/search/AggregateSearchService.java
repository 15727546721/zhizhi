package cn.xu.service.search;

import cn.xu.model.dto.post.PostTagRelation;
import cn.xu.model.entity.Post;
import cn.xu.model.entity.Tag;
import cn.xu.model.entity.User;
import cn.xu.model.vo.search.AggregateSearchVO;
import cn.xu.model.vo.search.AggregateSearchVO.PostSearchItem;
import cn.xu.model.vo.search.AggregateSearchVO.SearchResultGroup;
import cn.xu.model.vo.search.AggregateSearchVO.TagSearchItem;
import cn.xu.model.vo.search.AggregateSearchVO.UserSearchItem;
import cn.xu.service.post.PostQueryService;
import cn.xu.service.post.PostStatisticsService;
import cn.xu.service.post.TagService;
import cn.xu.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 聚合搜索服务
 * <p>并行搜索帖子、用户、标签，返回聚合结果</p>
 
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AggregateSearchService {
    
    @Resource
    private PostQueryService postQueryService;
    
    @Resource
    private PostStatisticsService postStatisticsService;
    
    @Resource(name = "tagService")
    private TagService tagService;
    
    @Resource(name = "userService")
    private UserService userService;
    
    /**
     * 自定义线程池，用于异步搜索
     */
    private final ExecutorService searchExecutor = Executors.newFixedThreadPool(3);
    
    /**
     * 日期格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER = cn.xu.common.constants.TimeConstants.DATETIME_FORMATTER;

    /**
     * 默认搜索数量限制
     */
    private static final int DEFAULT_LIMIT = 10;

    /**
     * 搜索超时时间
     */
    private static final int SEARCH_TIMEOUT_SECONDS = 5;

    /**
     * 关闭线程池（应用关闭时调用）
     */
    @PreDestroy
    public void shutdown() {
        log.info("正在关闭聚合搜索线程池...");
        if (searchExecutor != null && !searchExecutor.isShutdown()) {
            searchExecutor.shutdown();
            try {
                if (!searchExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.warn("线程池未能在10秒内正常关闭，强制关闭");
                    searchExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.warn("等待线程池关闭时被中断，强制关闭");
                searchExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        log.info("聚合搜索线程池已关闭");
    }

    /**
     * 聚合搜索
     *
     * <p>使用CompletableFuture并行搜索帖子、用户、标签，提高搜索效率</p>
     *
     * @param keyword 搜索关键词
     * @param postLimit 帖子数量限制
     * @param userLimit 用户数量限制
     * @param tagLimit 标签数量限制
     * @return 聚合搜索结果
     */
    public AggregateSearchVO search(String keyword, Integer postLimit, Integer userLimit, Integer tagLimit) {
        long startTime = System.currentTimeMillis();

        if (keyword == null || keyword.trim().isEmpty()) {
            return buildEmptyResponse(keyword, 0L);
        }

        String normalizedKeyword = keyword.trim();
        int safePostLimit = postLimit != null && postLimit > 0 ? Math.min(postLimit, 50) : DEFAULT_LIMIT;
        int safeUserLimit = userLimit != null && userLimit > 0 ? Math.min(userLimit, 50) : DEFAULT_LIMIT;
        int safeTagLimit = tagLimit != null && tagLimit > 0 ? Math.min(tagLimit, 50) : DEFAULT_LIMIT;

        log.info("开始聚合搜索: keyword={}, postLimit={}, userLimit={}, tagLimit={}",
                normalizedKeyword, safePostLimit, safeUserLimit, safeTagLimit);

        try {
            // 使用CompletableFuture并行搜索三种类型
            CompletableFuture<SearchResultGroup<PostSearchItem>> postsFuture =
                    CompletableFuture.supplyAsync(() -> searchPosts(normalizedKeyword, safePostLimit), searchExecutor);

            CompletableFuture<SearchResultGroup<UserSearchItem>> usersFuture =
                    CompletableFuture.supplyAsync(() -> searchUsers(normalizedKeyword, safeUserLimit), searchExecutor);

            CompletableFuture<SearchResultGroup<TagSearchItem>> tagsFuture =
                    CompletableFuture.supplyAsync(() -> searchTags(normalizedKeyword, safeTagLimit), searchExecutor);

            // 等待所有搜索完成，设置超时时间
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(postsFuture, usersFuture, tagsFuture);
            allFutures.get(SEARCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // 获取搜索结果
            SearchResultGroup<PostSearchItem> postsResult = postsFuture.join();
            SearchResultGroup<UserSearchItem> usersResult = usersFuture.join();
            SearchResultGroup<TagSearchItem> tagsResult = tagsFuture.join();

            long costTime = System.currentTimeMillis() - startTime;

            log.info("聚合搜索完成: keyword={}, posts={}, users={}, tags={}, costTime={}ms",
                    normalizedKeyword,
                    postsResult.getTotal(),
                    usersResult.getTotal(),
                    tagsResult.getTotal(),
                    costTime);

            return AggregateSearchVO.builder()
                    .keyword(normalizedKeyword)
                    .posts(postsResult)
                    .users(usersResult)
                    .tags(tagsResult)
                    .costTime(costTime)
                    .build();

        } catch (Exception e) {
            log.error("聚合搜索失败: keyword={}", normalizedKeyword, e);
            long costTime = System.currentTimeMillis() - startTime;
            return buildEmptyResponse(normalizedKeyword, costTime);
        }
    }

    /**
     * 搜索帖子
     */
    private SearchResultGroup<PostSearchItem> searchPosts(String keyword, int limit) {
        try {
            // 使用PostQueryService搜索帖子
            List<Post> posts = postQueryService.search(keyword, 0, limit);
            long total = postStatisticsService.countSearch(keyword);

            if (posts == null || posts.isEmpty()) {
                return buildEmptyPostResult();
            }

            // 获取作者信息
            Set<Long> userIds = posts.stream()
                    .map(Post::getUserId)
                    .filter(id -> id != null)
                    .collect(Collectors.toSet());

            Map<Long, User> userMap = userService.getBatchUserInfo(userIds);

            // 获取帖子标签
            List<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());
            Map<Long, List<String>> postTagsMap = getPostTagsMap(postIds);

            // 转换为帖子搜索项
            List<PostSearchItem> items = posts.stream()
                    .map(post -> convertToPostSearchItem(post, userMap, postTagsMap))
                    .collect(Collectors.toList());

            return SearchResultGroup.<PostSearchItem>builder()
                    .list(items)
                    .total(total)
                    .hasMore(total > limit)
                    .build();

        } catch (Exception e) {
            log.error("搜索帖子失败: keyword={}", keyword, e);
            return buildEmptyPostResult();
        }
    }

    /**
     * 搜索用户
     */
    private SearchResultGroup<UserSearchItem> searchUsers(String keyword, int limit) {
        try {
            // 使用UserService搜索用户
            List<User> users = userService.searchUsers(keyword, limit);

            if (users == null || users.isEmpty()) {
                return buildEmptyUserResult();
            }

            // 转换为用户搜索项
            List<UserSearchItem> items = users.stream()
                    .map(this::convertToUserSearchItem)
                    .collect(Collectors.toList());

            // 获取用户总数
            long total = users.size();

            return SearchResultGroup.<UserSearchItem>builder()
                    .list(items)
                    .total(total)
                    .hasMore(users.size() >= limit)
                    .build();

        } catch (Exception e) {
            log.error("搜索用户失败: keyword={}", keyword, e);
            return buildEmptyUserResult();
        }
    }

    /**
     * 搜索标签
     */
    private SearchResultGroup<TagSearchItem> searchTags(String keyword, int limit) {
        try {
            // 使用TagService搜索标签
            List<Tag> tags = tagService.searchTags(keyword);

            if (tags == null || tags.isEmpty()) {
                return buildEmptyTagResult();
            }

            // 获取标签总数
            long total = tags.size();

            // 转换为标签搜索项
            List<TagSearchItem> items = tags.stream()
                    .map(this::convertToTagSearchItem)
                    .collect(Collectors.toList());

            return SearchResultGroup.<TagSearchItem>builder()
                    .list(items)
                    .total(total)
                    .hasMore(total > limit)
                    .build();

        } catch (Exception e) {
            log.error("搜索标签失败: keyword={}", keyword, e);
            return buildEmptyTagResult();
        }
    }

    // ==================== 转换方法 ====================

    /**
     * 转换为帖子搜索项
     */
    private PostSearchItem convertToPostSearchItem(Post post, Map<Long, User> userMap, Map<Long, List<String>> postTagsMap) {
        User author = post.getUserId() != null ? userMap.get(post.getUserId()) : null;

        String authorName = "匿名用户";
        String authorAvatar = null;
        if (author != null) {
            authorName = author.getNickname() != null ? author.getNickname() : author.getUsername();
            authorAvatar = author.getAvatar();
        }

        List<String> tags = postTagsMap.getOrDefault(post.getId(), Collections.emptyList());
        
        String createTimeStr = null;
        if (post.getCreateTime() != null) {
            createTimeStr = post.getCreateTime().format(DATE_FORMATTER);
        }
        
        return PostSearchItem.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(generateSummary(post.getDescription(), post.getContent()))
                .coverUrl(post.getCoverUrl())
                .userId(post.getUserId())
                .authorName(authorName)
                .authorAvatar(authorAvatar)
                .viewCount(post.getViewCount() != null ? post.getViewCount() : 0L)
                .likeCount(post.getLikeCount() != null ? post.getLikeCount() : 0L)
                .commentCount(post.getCommentCount() != null ? post.getCommentCount() : 0L)
                .tags(tags)
                .createTime(createTimeStr)
                .build();
    }
    
    /**
     * 转换为用户搜索项
     */
    private UserSearchItem convertToUserSearchItem(User user) {
        return UserSearchItem.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .description(user.getDescription())
                .fansCount(user.getFansCount() != null ? user.getFansCount() : 0L)
                .postCount(user.getPostCount() != null ? user.getPostCount() : 0L)
                .isOfficial(user.isOfficialAccount())
                .build();
    }
    
    /**
     * 转换为标签搜索项
     */
    private TagSearchItem convertToTagSearchItem(Tag tag) {
        return TagSearchItem.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .usageCount(tag.getUsageCount() != null ? tag.getUsageCount() : 0)
                .isRecommended(cn.xu.common.constants.BooleanConstants.isTrue(tag.getIsRecommended()))
                .build();
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 生成帖子摘要
     */
    private String generateSummary(String description, String content) {
        if (description != null && !description.trim().isEmpty()) {
            return description.length() > 200 ? description.substring(0, 200) + "..." : description;
        }
        
        if (content != null && !content.trim().isEmpty()) {
            String plainText = content.replaceAll("<[^>]+>", "").trim();
            if (plainText.length() > 200) {
                return plainText.substring(0, 200) + "...";
            }
            return plainText;
        }
        
        return "";
    }
    
    /**
     * 批量获取帖子标签名称
     */
    private Map<Long, List<String>> getPostTagsMap(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        try {
            // 获取帖子标签关联
            List<PostTagRelation> relations = 
                    tagService.batchGetTagIdsByPostIds(postIds);
            
            if (relations == null || relations.isEmpty()) {
                return Collections.emptyMap();
            }
            
            // 收集所有标签ID
            Set<Long> allTagIds = relations.stream()
                    .flatMap(r -> r.getTagIds().stream())
                    .collect(Collectors.toSet());
            
            // 获取所有标签详情
            List<Tag> allTags = tagService.getTagList();
            Map<Long, String> tagNameMap = allTags.stream()
                    .filter(t -> allTagIds.contains(t.getId()))
                    .collect(Collectors.toMap(Tag::getId, Tag::getName, (a, b) -> a));
            
            // 构建帖子ID到标签名称列表的映射
            return relations.stream()
                    .collect(Collectors.toMap(
                            PostTagRelation::getPostId,
                            r -> r.getTagIds().stream()
                                    .map(tagNameMap::get)
                                    .filter(name -> name != null)
                                    .collect(Collectors.toList()),
                            (a, b) -> a
                    ));
        } catch (Exception e) {
            log.warn("批量获取帖子标签失败", e);
            return Collections.emptyMap();
        }
    }
    
    /**
     * 构建空响应
     */
    private AggregateSearchVO buildEmptyResponse(String keyword, Long costTime) {
        return AggregateSearchVO.builder()
                .keyword(keyword)
                .posts(buildEmptyPostResult())
                .users(buildEmptyUserResult())
                .tags(buildEmptyTagResult())
                .costTime(costTime)
                .build();
    }
    
    private SearchResultGroup<PostSearchItem> buildEmptyPostResult() {
        return SearchResultGroup.<PostSearchItem>builder()
                .list(new ArrayList<>())
                .total(0L)
                .hasMore(false)
                .build();
    }
    
    private SearchResultGroup<UserSearchItem> buildEmptyUserResult() {
        return SearchResultGroup.<UserSearchItem>builder()
                .list(new ArrayList<>())
                .total(0L)
                .hasMore(false)
                .build();
    }
    
    private SearchResultGroup<TagSearchItem> buildEmptyTagResult() {
        return SearchResultGroup.<TagSearchItem>builder()
                .list(new ArrayList<>())
                .total(0L)
                .hasMore(false)
                .build();
    }
}
