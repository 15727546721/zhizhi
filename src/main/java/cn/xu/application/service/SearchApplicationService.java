package cn.xu.application.service;

import cn.xu.api.web.model.vo.post.PostSearchResponse;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.search.model.valobj.SearchFilter;
import cn.xu.domain.search.service.ISearchDomainService;
import cn.xu.domain.search.service.ISearchStatisticsService;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 搜索应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchApplicationService {
    
    private final ISearchDomainService searchDomainService;
    private final ISearchStatisticsService searchStatisticsService;
    private final IUserService userService;
    
    public SearchResult executeSearch(String keyword, SearchFilter filter, int page, int size) {
        long startTime = System.currentTimeMillis();
        
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }
        
        String normalizedKeyword = cn.xu.infrastructure.search.util.SearchKeywordNormalizer.normalize(keyword);
        if (normalizedKeyword.isEmpty()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }
        
        if (normalizedKeyword.length() > 100) {
            throw new IllegalArgumentException("搜索关键词长度不能超过100个字符");
        }
        
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, Math.min(size, 100));
        
        try {
            Pageable pageable = PageRequest.of(safePage - 1, safeSize);
            Page<PostEntity> postsPage = searchDomainService.executeSearch(normalizedKeyword, filter, pageable);
            List<PostSearchResponse> searchResponses = convertToPostSearchResponses(postsPage.getContent());
            
            long responseTime = System.currentTimeMillis() - startTime;
            log.info("搜索完成: keyword={}, total={}, results={}, responseTime={}ms", 
                    normalizedKeyword, postsPage.getTotalElements(), searchResponses.size(), responseTime);
            
            try {
                searchDomainService.recordSearch(normalizedKeyword, postsPage.getTotalElements(), 
                        !searchResponses.isEmpty());
            } catch (Exception e) {
                log.warn("记录搜索统计失败: keyword={}", normalizedKeyword, e);
            }
            
            return SearchResult.builder()
                    .posts(searchResponses)
                    .total(postsPage.getTotalElements())
                    .page(safePage)
                    .size(safeSize)
                    .build();
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("搜索失败: keyword={}, responseTime={}ms", normalizedKeyword, responseTime, e);
            throw e;
        }
    }
    
    public List<String> getSearchSuggestions(String keyword, int limit) {
        return searchStatisticsService.getSearchSuggestions(keyword, limit);
    }
    
    public List<String> getHotKeywords(int limit) {
        return searchStatisticsService.getHotKeywords(limit);
    }
    
    public ISearchStatisticsService.SearchStatistics getSearchStatistics(String date) {
        return searchStatisticsService.getSearchStatistics(date);
    }
    
    public List<ISearchStatisticsService.HotKeyword> getHotKeywordsDetailed(int limit) {
        return searchStatisticsService.getHotKeywordsWithCount(limit);
    }
    
    private List<PostSearchResponse> convertToPostSearchResponses(List<PostEntity> posts) {
        if (posts == null || posts.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        Set<Long> userIds = posts.stream()
                .map(PostEntity::getUserId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        
        List<UserEntity> users = new java.util.ArrayList<>();
        try {
            users = userService.batchGetUserInfo(new java.util.ArrayList<>(userIds));
        } catch (Exception e) {
            log.warn("批量获取用户信息失败", e);
        }
        
        Map<Long, UserEntity> userMap = users.stream()
                .collect(Collectors.toMap(UserEntity::getId, user -> user, (existing, replacement) -> existing));
        
        return posts.stream()
                .map(post -> {
                    UserEntity user = post.getUserId() != null ? userMap.get(post.getUserId()) : null;
                    
                    String authorName = "匿名用户";
                    if (user != null) {
                        if (user.getNickname() != null && !user.getNickname().trim().isEmpty()) {
                            authorName = user.getNickname();
                        } else if (user.getUsername() != null) {
                            authorName = user.getUsernameValue();
                        }
                    }
                    
                    String avatar = user != null ? user.getAvatar() : null;
                    String summary = generateSummary(post.getDescription(), 
                                                    post.getContent() != null ? post.getContent().getValue() : null);

                    return PostSearchResponse.builder()
                            .id(post.getId())
                            .type(post.getType() != null ? post.getType().getCode() : null)
                            .title(post.getTitle() != null ? post.getTitle().getValue() : null)
                            .description(post.getDescription())
                            .content(summary)
                            .coverUrl(post.getCoverUrl())
                            .userId(post.getUserId())
                            .authorName(authorName)
                            .avatar(avatar)
                            .categoryId(post.getCategoryId())
                            .viewCount(post.getViewCount() != null ? post.getViewCount() : 0L)
                            .likeCount(post.getLikeCount() != null ? post.getLikeCount() : 0L)
                            .commentCount(post.getCommentCount() != null ? post.getCommentCount() : 0L)
                            .favoriteCount(post.getFavoriteCount() != null ? post.getFavoriteCount() : 0L)
                            .shareCount(post.getShareCount() != null ? post.getShareCount() : 0L)
                            .isFeatured(post.getIsFeatured() != null ? post.getIsFeatured() : false)
                            .createTime(post.getCreateTime())
                            .updateTime(post.getUpdateTime())
                            .publishTime(post.getPublishTime())
                            .build();
                })
                .collect(Collectors.toList());
    }
    
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
     * 搜索结果
     */
    @lombok.Data
    @lombok.Builder
    public static class SearchResult {
        private List<PostSearchResponse> posts;
        private long total;
        private int page;
        private int size;
    }
}

