package cn.xu.api.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.model.vo.article.ArticleListVO;
import cn.xu.api.web.model.vo.home.HomeDataVO;
import cn.xu.api.web.model.vo.home.PersonalizedHomeDataVO;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.article.model.entity.CategoryEntity;
import cn.xu.domain.article.model.entity.TagEntity;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.service.ArticleQueryDomainService;
import cn.xu.domain.article.service.ICategoryService;
import cn.xu.domain.article.service.ITagService;
import cn.xu.domain.article.service.IArticleService;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import cn.xu.domain.follow.service.IFollowService;
import cn.xu.infrastructure.common.response.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.ArrayList;

/**
 * 主页接口
 * 提供主页数据聚合展示功能
 */
@Slf4j
@RestController
@RequestMapping("/api/home")
@Tag(name = "主页接口", description = "主页数据展示相关接口")
@RequiredArgsConstructor
public class HomeController {

    private final IArticleService articleService;
    private final ArticleQueryDomainService articleQueryDomainService;
    private final IUserService userService;
    private final ICategoryService categoryService;
    private final ITagService tagService;
    private final IFollowService followService;

    /**
     * 获取主页数据
     *
     * @return 主页数据
     */
    @GetMapping("/data")
    @Operation(summary = "获取主页数据", description = "获取主页展示的所有数据，包括热门文章、最新文章、推荐作者、热门标签等")
    public ResponseEntity<HomeDataVO> getHomeData() {
        try {
            // 获取热门文章列表（周榜前10）
            List<ArticleEntity> hotArticles = new ArrayList<>();
            try {
                hotArticles = articleQueryDomainService.getHotRank("week", 
                    PageRequest.of(0, 10)).getContent();
            } catch (Exception e) {
                log.warn("获取热门文章失败，使用默认文章列表", e);
                hotArticles = articleService.getArticlePageList(1, 10);
            }
            
            // 获取最新文章列表
            List<ArticleEntity> latestArticles = articleService.getArticlePageList(1, 10);
            
            // 获取推荐作者（这里简单获取前10个用户，实际可以根据关注数、文章数等维度计算）
            List<UserEntity> recommendedAuthors = new ArrayList<>();
            try {
                recommendedAuthors = userService.queryUserList(
                    new cn.xu.infrastructure.common.request.PageRequest());
                // 设置分页参数
                recommendedAuthors = recommendedAuthors.subList(0, Math.min(recommendedAuthors.size(), 10));
            } catch (Exception e) {
                log.warn("获取推荐作者失败", e);
                recommendedAuthors = Collections.emptyList();
            }
            
            // 获取热门标签
            List<TagEntity> hotTags = new ArrayList<>();
            try {
                hotTags = tagService.getTagList();
                // 限制标签数量
                if (hotTags.size() > 20) {
                    hotTags = hotTags.subList(0, 20);
                }
            } catch (Exception e) {
                log.warn("获取热门标签失败", e);
                hotTags = Collections.emptyList();
            }
            
            // 获取分类列表
            List<CategoryEntity> categories = new ArrayList<>();
            try {
                categories = categoryService.getCategoryList();
                // 限制分类数量
                if (categories.size() > 10) {
                    categories = categories.subList(0, 10);
                }
            } catch (Exception e) {
                log.warn("获取分类列表失败", e);
                categories = Collections.emptyList();
            }
            
            // 构建主页数据VO
            HomeDataVO homeData = HomeDataVO.builder()
                    .hotArticles(convertToArticleListVOs(hotArticles))
                    .latestArticles(convertToArticleListVOs(latestArticles))
                    .recommendedAuthors(recommendedAuthors)
                    .hotTags(hotTags)
                    .categories(categories)
                    .build();
            
            return ResponseEntity.<HomeDataVO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(homeData)
                    .build();
        } catch (Exception e) {
            log.error("获取主页数据失败", e);
            return ResponseEntity.<HomeDataVO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取主页数据失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 获取用户个性化主页数据（需要登录）
     *
     * @return 用户个性化主页数据
     */
    @GetMapping("/personalized")
    @Operation(summary = "获取个性化主页数据", description = "获取用户个性化的主页数据，包括关注用户的最新动态等")
    public ResponseEntity<PersonalizedHomeDataVO> getPersonalizedHomeData() {
        try {
            // 检查用户是否登录
            if (!StpUtil.isLogin()) {
                return ResponseEntity.<PersonalizedHomeDataVO>builder()
                        .code(ResponseCode.UN_ERROR.getCode())
                        .info("用户未登录")
                        .build();
            }
            
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 获取用户关注的作者列表
            List<Long> followingUserIds = new ArrayList<>();
            try {
                List<cn.xu.domain.follow.model.entity.UserFollowEntity> followingList = 
                    followService.getFollowingList(userId);
                followingUserIds = followingList.stream()
                    .map(cn.xu.domain.follow.model.entity.UserFollowEntity::getFollowedId)
                    .collect(Collectors.toList());
            } catch (Exception e) {
                log.warn("获取用户关注列表失败", e);
            }
            
            // 获取关注用户发布的最新文章
            List<ArticleEntity> followingAuthorsArticles = new ArrayList<>();
            if (!followingUserIds.isEmpty()) {
                try {
                    // 这里简化处理，实际应该根据关注用户ID查询文章
                    followingAuthorsArticles = articleService.getArticlePageList(1, 10);
                } catch (Exception e) {
                    log.warn("获取关注用户文章失败", e);
                }
            }
            
            // 构建个性化主页数据VO
            PersonalizedHomeDataVO personalizedData = PersonalizedHomeDataVO.builder()
                    .followingArticles(convertToArticleListVOs(followingAuthorsArticles))
                    .build();
            
            return ResponseEntity.<PersonalizedHomeDataVO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(personalizedData)
                    .build();
        } catch (Exception e) {
            log.error("获取个性化主页数据失败", e);
            return ResponseEntity.<PersonalizedHomeDataVO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取个性化主页数据失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 将文章实体列表转换为ArticleListVO列表
     */
    private List<ArticleListVO> convertToArticleListVOs(List<ArticleEntity> articles) {
        if (articles == null || articles.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 收集所有文章的作者ID
        Set<Long> userIds = new HashSet<>();
        articles.forEach(article -> {
            if (article.getUserId() != null) {
                userIds.add(article.getUserId());
            }
        });
        
        // 批量获取用户信息
        List<UserEntity> users = Collections.emptyList();
        try {
            users = userService.batchGetUserInfo(new ArrayList<>(userIds));
        } catch (Exception e) {
            log.warn("批量获取用户信息失败", e);
        }
        
        // 构建用户ID到用户实体的映射
        java.util.Map<Long, UserEntity> userMap = users.stream()
                .collect(Collectors.toMap(UserEntity::getId, user -> user, (existing, replacement) -> existing));
        
        return articles.stream()
                .map(article -> {
                    ArticleListVO vo = new ArticleListVO();
                    vo.setArticle(article);
                    // 设置作者信息
                    if (article.getUserId() != null) {
                        vo.setUser(userMap.get(article.getUserId()));
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }
}