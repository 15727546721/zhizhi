package cn.xu.api.web.controller.article;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.model.dto.common.ResponseEntity;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.service.IArticleCategoryService;
import cn.xu.domain.article.service.IArticleService;
import cn.xu.domain.article.service.IArticleTagService;
import cn.xu.domain.article.service.ITagService;
import cn.xu.domain.article.service.article.ArticleIndexService;
import cn.xu.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("api/article")
@RestController
@Tag(name = "文章接口", description = "文章相关接口")
@Slf4j
public class ArticleController {

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
    @Resource
    private ArticleIndexService articleIndexService;

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

    @GetMapping("/search")
    @Operation(summary = "搜索文章")
    public ResponseEntity<List<ArticleEntity>> searchArticles(@RequestParam String title) {
        try {
            if (StringUtils.isBlank(title)) {
                return ResponseEntity.<List<ArticleEntity>>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("搜索关键词不能为空")
                    .build();
            }

            List<ArticleEntity> articles = articleIndexService.searchArticles(title);
            return ResponseEntity.<List<ArticleEntity>>builder()
                .data(articles)
                .code(ResponseCode.SUCCESS.getCode())
                .info("搜索成功")
                .build();
        } catch (Exception e) {
            log.error("文章搜索失败: {}", e.getMessage(), e);
            return ResponseEntity.<List<ArticleEntity>>builder()
                .code(ResponseCode.UN_ERROR.getCode())
                .info("搜索失败")
                .build();
        }
    }

    @GetMapping("/list/page")
    @Operation(summary = "获取分页文章列表")
    @Parameters({
        @Parameter(name = "pageNum", description = "页码", required = true),
        @Parameter(name = "pageSize", description = "每页数量", required = true),
        @Parameter(name = "categoryId", description = "分类ID"),
        @Parameter(name = "keyword", description = "搜索关键词")
    })
    public ResponseEntity<List<ArticleListDTO>> getArticlesPage(
            @RequestParam(value = "pageNum", required = true) int pageNum,
            @RequestParam(value = "pageSize", required = true) int pageSize,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "keyword", required = false) String keyword) {
        log.info("分页获取文章列表 - pageNum: {}, pageSize: {}, categoryId: {}, keyword: {}", 
                pageNum, pageSize, categoryId, keyword);
        List<ArticleListDTO> articles = articleService.getArticlesPage(pageNum, pageSize, categoryId, keyword);
        return ResponseEntity.<List<ArticleListDTO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(articles)
                .build();
    }

}
