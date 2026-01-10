package cn.xu.service.post;

import cn.xu.cache.CacheService;
import cn.xu.model.dto.post.PostTagRelation;
import cn.xu.model.entity.Post;
import cn.xu.model.entity.Tag;
import cn.xu.model.entity.User;
import cn.xu.model.vo.post.PostItemVO;
import cn.xu.model.vo.post.PostListVO;
import cn.xu.model.vo.post.PostDetailVO;
import cn.xu.model.vo.tag.TagVO;
import cn.xu.model.vo.user.UserVO;
import cn.xu.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 帖子对象转换器
 * 负责 Post 实体到各种 VO 的转换
 * 
 * 优化：
 * - 批量获取用户信息（避免 N+1）
 * - 批量获取标签信息（避免 N+1）
 * - 支持缓存降级
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostConverter {

    private final UserService userService;
    private final TagService tagService;
    private final CacheService cacheService;
    
    private static final String USER_CACHE_PREFIX = "user:info:";
    private static final long USER_CACHE_TTL = 300; // 5分钟

    /**
     * 批量转换 Post 列表为 PostListVO 列表
     * 包含用户信息和标签信息的批量获取优化
     */
    public List<PostListVO> toListVOs(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 批量获取用户信息
        Map<Long, User> userMap = batchGetUsers(posts);

        // 2. 批量获取标签信息
        Map<Long, String[]> tagMap = batchGetTags(posts);

        // 3. 转换为 VO
        return posts.stream()
                .map(post -> toListVO(post, userMap, tagMap))
                .collect(Collectors.toList());
    }

    /**
     * 单个 Post 转换为 PostListVO
     * 已优化：增加 null 安全检查，防止用户被删除时 NPE
     */
    private PostListVO toListVO(Post post, Map<Long, User> userMap, Map<Long, String[]> tagMap) {
        // 安全获取用户信息（用户可能已被删除）
        User user = post.getUserId() != null ? userMap.get(post.getUserId()) : null;
        String nickname = user != null ? user.getNickname() : "已删除用户";
        String avatar = user != null ? user.getAvatar() : null;
        
        String[] tagNames = tagMap.getOrDefault(post.getId(), new String[]{});

        PostItemVO postItem = PostItemVO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())
                .content(post.getContent())
                .coverUrl(post.getCoverUrl())
                .status(post.getStatus())
                .userId(post.getUserId())
                .nickname(nickname)
                .avatar(avatar)
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .favoriteCount(post.getFavoriteCount())
                .createTime(post.getCreateTime())
                .updateTime(post.getUpdateTime())
                .tagNameList(tagNames)
                .build();

        return PostListVO.builder()
                .postItem(postItem)
                .build();
    }

    /**
     * User 转换为 UserVO
     */
    public UserVO toUserVO(User user) {
        if (user == null) {
            return null;
        }
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .description(user.getDescription())
                .followCount(user.getFollowCount())
                .fansCount(user.getFansCount())
                .likeCount(user.getLikeCount())
                .createTime(user.getCreateTime())
                .build();
    }

    /**
     * Tag 列表转换为 TagVO 列表
     */
    public List<TagVO> toTagVOs(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        return tags.stream()
                .map(tag -> TagVO.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .build())
                .collect(Collectors.toList());
    }

    // ==================== 私有方法 ====================

    /**
     * 批量获取用户信息（带缓存和降级处理）
     */
    private Map<Long, User> batchGetUsers(List<Post> posts) {
        Set<Long> userIds = posts.stream()
                .map(Post::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            // 使用缓存服务批量获取
            return cacheService.batchGetOrLoad(
                    USER_CACHE_PREFIX,
                    userIds,
                    ids -> {
                        Map<Long, User> userMap = userService.batchGetUserInfo(ids);
                        return new ArrayList<>(userMap.values());
                    },
                    User::getId,
                    USER_CACHE_TTL
            );
        } catch (Exception e) {
            log.error("【降级】批量获取用户信息失败，帖子将不显示作者信息 - userIds: {}", userIds, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 批量获取标签信息（带降级处理和缓存）
     * 优化：直接调用 TagService 的优化方法，减少中间转换
     */
    private Map<Long, String[]> batchGetTags(List<Post> posts) {
        List<Long> postIds = posts.stream()
                .map(Post::getId)
                .collect(Collectors.toList());

        if (postIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            // 直接调用优化后的方法，一次性获取所有标签名称
            return tagService.batchGetPostTagNames(postIds);

        } catch (Exception e) {
            log.error("【降级】批量获取帖子标签失败，帖子将不显示标签 - postIds: {}", postIds, e);
            return Collections.emptyMap();
        }
    }
}
