package cn.xu.api.controller.web.article;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseEntity;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.service.IArticleCategoryService;
import cn.xu.domain.article.service.IArticleService;
import cn.xu.domain.article.service.IArticleTagService;
import cn.xu.domain.article.service.ITagService;
import cn.xu.exception.BusinessException;
import cn.xu.infrastructure.common.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

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
    private IArticleCategoryService articleCategoryService;
    @Resource
    private IArticleTagService articleTagService;
    @Resource
    private ITagService tagService;
    @Resource
    private TransactionTemplate transactionTemplate;

    @GetMapping("/category")
    @Operation(summary = "通过分类获取文章列表")
    public ResponseEntity<List<ArticleListDTO>> getArticleByCategory(Long categoryId) {
        log.info("获取文章列表，分类ID：{}", categoryId);
        if (categoryId == null || categoryId == 0) {
            categoryId = null;
        }
        List<ArticleListDTO> articleEntityList = articleService.getArticleByCategory(categoryId);
        return ResponseEntity.<List<ArticleListDTO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(articleEntityList)
                .build();
    }


    @GetMapping("/{id}")
    @Operation(summary = "获取文章详情")
    public ResponseEntity<ArticleEntity> getArticleDetail(@PathVariable("id") Long id) {
        log.info("获取文章详情，文章ID：{}", id);
        ArticleEntity articleById = articleService.getArticleById(id);
        log.info("文章详情：{}", articleById);
        return ResponseEntity.<ArticleEntity>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(articleById)
                .build();
    }

    @PostMapping("/publish")
    @Operation(summary = "发布文章")
    public ResponseEntity pushArticle(@RequestBody PublishArticleRequest publishArticleRequest) {
        log.info("发布文章，文章内容：{}", publishArticleRequest);
        Long userId = StpUtil.getLoginIdAsLong();
        transactionTemplate.execute(status -> {
            // 事务开始
            try {
                //1. 保存文章
                Long articleId = articleService.createArticle(ArticleEntity.builder()
                        .title(publishArticleRequest.getTitle())
                        .coverUrl(publishArticleRequest.getCoverUrl())
                        .content(publishArticleRequest.getContent())
                        .description(publishArticleRequest.getDescription())
                        .userId(userId) // 当前登录用户ID
                        .build());
                //2. 保存文章分类
                articleCategoryService.saveArticleCategory(articleId, publishArticleRequest.getCategoryId());
                //3. 保存文章标签
                if (publishArticleRequest.getTagIds() == null || publishArticleRequest.getTagIds().isEmpty()) {
                    throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "标签不能为空");
                }
                if (publishArticleRequest.getTagIds().size() > 3) {
                    throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "标签不能超过3个");
                }
                articleTagService.saveArticleTag(articleId, publishArticleRequest.getTagIds());
                // 事务提交
                return 1;
            } catch (Exception e) {
                // 事务回滚
                status.setRollbackOnly();
                log.error("文章发布失败", e);
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文章发布失败");
            }
        });
        log.info("文章发布成功");
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("文章发布成功")
                .build();
    }

    @GetMapping("/hot")
    @Operation(summary = "获取热门文章列表")
    public ResponseEntity<List<ArticleListDTO>> getHotArticles(@RequestParam(defaultValue = "10") int limit) {
        List<ArticleListDTO> hotArticles = articleService.getHotArticles(limit);
        return ResponseEntity.<List<ArticleListDTO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
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
                .code(ResponseCode.SUCCESS.getCode())
                .data(likedArticles)
                .build();
    }

    @GetMapping("/{id}/liked-users")
    @Operation(summary = "获取文章的点赞用户列表")
    public ResponseEntity<List<Long>> getArticleLikedUsers(@PathVariable("id") Long articleId) {
        List<Long> likedUsers = articleService.getArticleLikedUsers(articleId);
        return ResponseEntity.<List<Long>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(likedUsers)
                .build();
    }

}
