package cn.xu.api.controller.admin.article;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.dto.article.ArticleDetailsResponse;
import cn.xu.api.dto.article.ArticleListResponse;
import cn.xu.api.dto.article.CreateArticleRequest;
import cn.xu.api.dto.common.PageRequest;
import cn.xu.common.Constants;
import cn.xu.common.ResponseEntity;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.entity.CategoryEntity;
import cn.xu.domain.article.model.entity.TagEntity;
import cn.xu.domain.article.service.*;
import cn.xu.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("system/article")
@RestController
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
    private ICategoryService categoryService;
    @Resource
    private TransactionTemplate transactionTemplate;

    @PostMapping("/uploadCover")
    public ResponseEntity<String> uploadCover(@RequestPart("file") MultipartFile file) {
        String coverUrl = articleService.uploadCover(file);
        return ResponseEntity.<String>builder()
                .data(coverUrl)
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("上传封面成功")
                .build();
    }

    @PostMapping("/add")
    public ResponseEntity saveArticle(@RequestBody CreateArticleRequest createArticleRequest) {
        log.info("文章创建参数: {}", createArticleRequest);
        transactionTemplate.execute(status -> {
            // 事务开始
            try {
                //1. 保存文章
                Long articleId = articleService.createArticle(ArticleEntity.builder()
                        .title(createArticleRequest.getTitle())
                        .coverUrl(createArticleRequest.getCoverUrl())
                        .content(createArticleRequest.getContent())
                        .description(createArticleRequest.getDescription())
                        .commentEnabled(createArticleRequest.getCommentEnabled())
                        .authorId(StpUtil.getLoginIdAsLong())
                        .build());
                //2. 保存文章分类
                articleCategoryService.saveArticleCategory(articleId, createArticleRequest.getCategoryId());
                //3. 保存文章标签
                if (createArticleRequest.getTagIds() == null || createArticleRequest.getTagIds().isEmpty()) {
                    throw new AppException(Constants.ResponseCode.NULL_PARAMETER.getCode(), "标签不能为空");
                }
                if (createArticleRequest.getTagIds().size() > 3) {
                    throw new AppException(Constants.ResponseCode.ILLEGAL_PARAMETER.getCode(), "标签不能超过3个");
                }
                articleTagService.saveArticleTag(articleId, createArticleRequest.getTagIds());
                // 事务提交
                return 1;
            } catch (Exception e) {
                // 事务回滚
                status.setRollbackOnly();
                log.error("文章创建失败", e);
                throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "文章创建失败");
            }
        });
        return ResponseEntity.builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("文章创建成功")
                .build();
    }

    @PostMapping("/update")
    public ResponseEntity updateArticle(@RequestBody CreateArticleRequest createArticleRequest) {
        log.info("文章更新参数: {}", createArticleRequest);
        transactionTemplate.execute(status -> {
            // 事务开始
            try {
                //1. 更新文章
                articleService.updateArticle(ArticleEntity.builder()
                        .title(createArticleRequest.getTitle())
                        .coverUrl(createArticleRequest.getCoverUrl())
                        .content(createArticleRequest.getContent())
                        .description(createArticleRequest.getDescription())
                        .commentEnabled(createArticleRequest.getCommentEnabled())
                        .authorId(StpUtil.getLoginIdAsLong())
                        .build());
                //2. 更新文章分类
                articleCategoryService.updateArticleCategory(createArticleRequest.getId(), createArticleRequest.getCategoryId());
                //3. 更新文章标签
                if (createArticleRequest.getTagIds() == null || createArticleRequest.getTagIds().isEmpty()) {
                    throw new AppException(Constants.ResponseCode.NULL_PARAMETER.getCode(), "标签不能为空");
                }
                if (createArticleRequest.getTagIds().size() > 3) {
                    throw new AppException(Constants.ResponseCode.ILLEGAL_PARAMETER.getCode(), "标签不能超过3个");
                }
                articleTagService.updateArticleTag(createArticleRequest.getId(), createArticleRequest.getTagIds());
                return 1;
            } catch (Exception e) {
                // 事务回滚
                status.setRollbackOnly();
                log.error("文章更新失败", e);
                throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "文章更新失败");
            }
        });
        return ResponseEntity.builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("文章更新成功")
                .build();
    }

    @PostMapping("/delete")
    public ResponseEntity deleteArticles(@RequestBody List<Long> articleIds) {
        articleService.deleteArticles(articleIds);
        return ResponseEntity.builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("文章批量删除成功")
                .build();
    }

    @PostMapping("/list")
    public ResponseEntity<List<ArticleListResponse>> listArticle(@ModelAttribute PageRequest pageRequest) {
        log.info("文章列表获取参数: page={}, size={}", pageRequest.getPage(), pageRequest.getSize());
        List<ArticleEntity> articles = articleService.listArticle(pageRequest.getPage(), pageRequest.getSize());
        log.info("文章列表获取结果: {}", articles);

        // 将 ArticleEntity 转换为 ArticleResponse
        List<ArticleListResponse> response = articles.stream()
                .map(article -> ArticleListResponse.builder()
                        .id(article.getId())
                        .title(article.getTitle())
                        .description(article.getDescription())
                        .status(article.getStatus())
                        .coverUrl(article.getCoverUrl())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.<List<ArticleListResponse>>builder()
                .data(response)
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("文章列表获取成功")
                .build();
    }

    /**
     * 获取文章详情
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<ArticleDetailsResponse> getArticle(@PathVariable("id") Long id) {
        log.info("文章详情获取参数: id={}", id);
        ArticleEntity article = articleService.getArticleById(id);
        CategoryEntity category = categoryService.getCategoryByArticleId(id);
        List<TagEntity> tag = tagService.getTagsByArticleId(id);
        ArticleDetailsResponse response = new ArticleDetailsResponse();
        response.setId(article.getId());
        response.setTitle(article.getTitle());
        response.setContent(article.getContent());
        response.setCoverUrl(article.getCoverUrl());
        response.setDescription(article.getDescription());
        response.setStatus(article.getStatus());
        response.setCommentEnabled(article.getCommentEnabled());
        response.setCategory(category);
        response.setTags(tag);
        log.info("文章详情获取结果: {}", response);
        return ResponseEntity.<ArticleDetailsResponse>builder()
                .data(response)
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("文章获取成功")
                .build();
    }
}
