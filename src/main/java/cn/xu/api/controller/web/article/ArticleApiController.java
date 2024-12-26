package cn.xu.api.controller.web.article;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.Constants;
import cn.xu.common.ResponseEntity;
import cn.xu.domain.article.event.ArticleEvent;
import cn.xu.domain.article.event.ArticleEventPublisher;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.service.IArticleService;
import cn.xu.infrastructure.redis.IRedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("api/article")
@RestController
@Tag(name = "文章接口", description = "文章相关接口")
@Slf4j
public class ArticleApiController {

    @Resource
    private IArticleService articleService;
    @Resource
    private ArticleEventPublisher articleEventPublisher;
    @Resource
    private IRedisService redisService;

    @GetMapping("/category")
    @Operation(summary = "通过分类获取文章列表")
    public ResponseEntity<List<ArticleListDTO>> getArticleByCategory(Long categoryId) {
        log.info("获取文章列表，分类ID：{}", categoryId);
        if (categoryId == null || categoryId == 0) {
            categoryId = null;
        }
        List<ArticleListDTO> articleEntityList = articleService.getArticleByCategory(categoryId);
        return ResponseEntity.<List<ArticleListDTO>>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .data(articleEntityList)
                .build();
    }


    @GetMapping("/{id}")
    @Operation(summary = "获取文章详情")
    public ResponseEntity<ArticleEntity> getArticleDetail(@PathVariable("id") Long id) {
        ArticleEntity articleById = articleService.getArticleById(id);
        return ResponseEntity.<ArticleEntity>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .data(articleById)
                .build();
    }

    @GetMapping("/like")
    @Operation(summary = "点赞文章")
    @SaCheckLogin
    public ResponseEntity likeArticle(Long id, boolean isLike) {
        log.info("点赞文章，文章ID：{}, 点赞状态：{}", id, isLike);
        
        // 构建点赞记录key
        String likeKey = String.format("article:like:%d:users", id);
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 检查是否已经点赞
        boolean hasLiked = redisService.isSetMember(likeKey, String.valueOf(userId));
        
        if (isLike && hasLiked) {
            return ResponseEntity.builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info("您已经点赞过该文章")
                    .build();
        }
        
        if (!isLike && !hasLiked) {
            return ResponseEntity.builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info("您还没有点赞过该文章")
                    .build();
        }
        
        // 更新点赞集合
        if (isLike) {
            redisService.addToSet(likeKey, String.valueOf(userId));
        } else {
            redisService.removeFromSet(likeKey, String.valueOf(userId));
        }
        
        // 发布点赞事件
        articleEventPublisher.publishEvent(id, ArticleEvent.EventType.LIKE, isLike);
        
        return ResponseEntity.builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info(isLike ? "点赞成功" : "取消点赞成功")
                .build();
    }

    @GetMapping("/hot")
    @Operation(summary = "获取热门文章列表")
    public ResponseEntity<List<ArticleListDTO>> getHotArticles(@RequestParam(defaultValue = "10") int limit) {
        List<ArticleListDTO> hotArticles = articleService.getHotArticles(limit);
        return ResponseEntity.<List<ArticleListDTO>>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .data(hotArticles)
                .build();
    }

    @GetMapping("/user/likes")
    @Operation(summary = "获取用户点赞的文章列表")
    @SaCheckLogin
    public ResponseEntity<List<ArticleListDTO>> getUserLikedArticles() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<ArticleListDTO> likedArticles = articleService.getUserLikedArticles(userId);
        return ResponseEntity.<List<ArticleListDTO>>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .data(likedArticles)
                .build();
    }

    @GetMapping("/{id}/liked-users")
    @Operation(summary = "获取文章的点赞用户列表")
    public ResponseEntity<List<Long>> getArticleLikedUsers(@PathVariable("id") Long articleId) {
        List<Long> likedUsers = articleService.getArticleLikedUsers(articleId);
        return ResponseEntity.<List<Long>>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .data(likedUsers)
                .build();
    }

    @GetMapping("/{id}/like-status")
    @Operation(summary = "获取文章的点赞状态")
    @SaCheckLogin
    public ResponseEntity<Boolean> getArticleLikeStatus(@PathVariable("id") Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean isLiked = articleService.isArticleLiked(articleId, userId);
        return ResponseEntity.<Boolean>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .data(isLiked)
                .build();
    }
}
