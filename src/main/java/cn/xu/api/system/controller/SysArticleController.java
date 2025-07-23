package cn.xu.api.system.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.system.model.dto.article.ArticleRequest;
import cn.xu.api.system.model.dto.article.CreateArticleRequest;
import cn.xu.api.system.model.dto.article.PublishArticleRequest;
import cn.xu.api.system.model.vo.SysArticleDetailVO;
import cn.xu.api.web.model.vo.article.ArticlePageVO;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.article.event.ArticleEventPublisher;
import cn.xu.domain.article.model.aggregate.ArticleAndAuthorAggregate;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.entity.CategoryEntity;
import cn.xu.domain.article.model.entity.TagEntity;
import cn.xu.domain.article.service.*;
import cn.xu.domain.like.service.ILikeService;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.common.response.PageResponse;
import cn.xu.infrastructure.common.response.ResponseEntity;
import cn.xu.infrastructure.persistent.read.elastic.service.ArticleElasticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "文章管理", description = "文章管理相关接口")
@Slf4j
@RequestMapping("system/article")
@RestController
public class SysArticleController {

    @Resource
    private IArticleService articleService;
    @Resource
    private IArticleTagService articleTagService;
    @Resource
    private ITagService tagService;
    @Resource
    private ICategoryService categoryService;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private ArticleElasticService articleElasticService;
    @Resource
    private ArticleEventPublisher eventPublisher;
    @Resource
    private ILikeService likeService;

    @PostMapping("/uploadCover")
    public ResponseEntity<String> uploadCover(@RequestPart("file") MultipartFile file) {
        String coverUrl = articleService.uploadCover(file);
        return ResponseEntity.<String>builder()
                .data(coverUrl)
                .code(ResponseCode.SUCCESS.getCode())
                .info("上传封面成功")
                .build();
    }

    @PostMapping("/add")
    public ResponseEntity saveArticle(@RequestBody CreateArticleRequest createArticleRequest) {
        log.info("文章创建参数: {}", createArticleRequest);
        transactionTemplate.execute(status -> {
            try {
                //1. 保存文章和分类id
                ArticleEntity article = ArticleEntity.builder()
                        .categoryId(createArticleRequest.getCategoryId())
                        .title(createArticleRequest.getTitle())
                        .coverUrl(createArticleRequest.getCoverUrl())
                        .content(createArticleRequest.getContent())
                        .description(createArticleRequest.getDescription())
                        .userId(StpUtil.getLoginIdAsLong())
                        .build();
                Long articleId = articleService.createArticle(article);
                article.setId(articleId);
                
                //2. 保存文章标签
                if (createArticleRequest.getTagIds() == null || createArticleRequest.getTagIds().isEmpty()) {
                    throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "标签不能为空");
                }
                if (createArticleRequest.getTagIds().size() > 3) {
                    throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "标签不能超过3个");
                }
                articleTagService.saveArticleTag(articleId, createArticleRequest.getTagIds());

                //4. 发布文章创建事件
                eventPublisher.publishArticleCreated(article);

                return 1;
            } catch (Exception e) {
                status.setRollbackOnly();
                log.error("文章创建失败", e);
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文章创建失败");
            }
        });
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("文章创建成功")
                .build();
    }

    @PostMapping("/update")
    public ResponseEntity updateArticle(@RequestBody CreateArticleRequest createArticleRequest) {
        log.info("文章更新参数: {}", createArticleRequest);
        transactionTemplate.execute(status -> {
            try {
                //1. 更新文章和分类id
                ArticleEntity article = ArticleEntity.builder()
                        .id(createArticleRequest.getId())
                        .categoryId(createArticleRequest.getCategoryId())
                        .title(createArticleRequest.getTitle())
                        .coverUrl(createArticleRequest.getCoverUrl())
                        .content(createArticleRequest.getContent())
                        .description(createArticleRequest.getDescription())
                        .userId(StpUtil.getLoginIdAsLong())
                        .build();
                articleService.updateArticle(article);

                //2. 更新文章标签
                if (createArticleRequest.getTagIds() == null || createArticleRequest.getTagIds().isEmpty()) {
                    throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "标签不能为空");
                }
                if (createArticleRequest.getTagIds().size() > 3) {
                    throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "标签不能超过3个");
                }
                articleTagService.updateArticleTag(createArticleRequest.getId(), createArticleRequest.getTagIds());

                //4. 发布文章更新事件
                eventPublisher.publishArticleUpdated(article);

                return 1;
            } catch (Exception e) {
                status.setRollbackOnly();
                log.error("文章更新失败", e);
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文章更新失败");
            }
        });
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("文章更新成功")
                .build();
    }

    @PostMapping("/delete")
    public ResponseEntity deleteArticles(@RequestBody List<Long> articleIds) {
        try {
            for (Long articleId : articleIds) {
                articleService.deleteArticles(articleIds);
                // 发布文章删除事件
                ArticleEntity article = ArticleEntity.builder()
                        .id(articleId)
                        .build();
                eventPublisher.publishArticleDeleted(articleId);
            }
            return ResponseEntity.builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("文章批量删除成功")
                    .build();
        } catch (Exception e) {
            log.error("文章删除失败", e);
            return ResponseEntity.builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("文章删除失败")
                    .build();
        }
    }

    @GetMapping("/list")
    @Operation(summary = "文章列表")
    public ResponseEntity<PageResponse<List<ArticlePageVO>>> listArticle(ArticleRequest articleRequest) {
        log.info("文章列表获取参数: {}", articleRequest);

        // 参数校验和默认值设置
        if (articleRequest.getPageNo() == null || articleRequest.getPageNo() < 1) {
            articleRequest.setPageNo(1);
        }
        if (articleRequest.getPageSize() == null || articleRequest.getPageSize() < 1) {
            articleRequest.setPageSize(10);
        }

        // 查询文章列表
        PageResponse<List<ArticlePageVO>> articleList = articleService.listArticle(articleRequest);
        log.info("文章列表获取结果: {}", articleList);

        return ResponseEntity.<PageResponse<List<ArticlePageVO>>>builder()
                .data(articleList)
                .code(ResponseCode.SUCCESS.getCode())
                .info("文章列表获取成功")
                .build();
    }

    /**
     * 获取文章详情
     *
     * @param id
     * @return
     */
    @GetMapping("info/{id}")
    public ResponseEntity<SysArticleDetailVO> getArticle(@PathVariable("id") Long id) {
        log.info("文章详情获取参数: id={}", id);
        if (id == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "文章ID不能为空");
        }
        ArticleAndAuthorAggregate article = articleService.getArticleDetailById(id);
        CategoryEntity category = categoryService.getCategoryByArticleId(id);
        List<TagEntity> tag = tagService.getTagsByArticleId(id);

        return ResponseEntity.<SysArticleDetailVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("文章获取成功")
                .data(SysArticleDetailVO.builder()
                        .articleAndAuthorAggregate(article)
                        .categoryName(category.getName())
                        .tags(tag.stream().map(TagEntity::getName).collect(Collectors.toList()))
                        .build())
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

            List<ArticleEntity> articles = articleElasticService.searchArticles(title);
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

    @PostMapping("/publish")
    @Operation(summary = "发布文章")
    public ResponseEntity pushArticle(@RequestBody PublishArticleRequest publishArticleRequest) {
        log.info("发布文章，文章内容：{}", publishArticleRequest);
        Long userId = StpUtil.getLoginIdAsLong();
        
        final Long[] articleId = new Long[1];
        
        transactionTemplate.execute(status -> {
            try {
                //1. 保存文章和分类id
                ArticleEntity article = ArticleEntity.builder()
                        .categoryId(publishArticleRequest.getCategoryId())
                        .title(publishArticleRequest.getTitle())
                        .coverUrl(publishArticleRequest.getCoverUrl())
                        .content(publishArticleRequest.getContent())
                        .description(publishArticleRequest.getDescription())
                        .userId(userId)
                        .build();
                
                articleId[0] = articleService.createArticle(article);
                article.setId(articleId[0]);
                
                //2. 保存文章标签
                if (publishArticleRequest.getTagIds() == null || publishArticleRequest.getTagIds().isEmpty()) {
                    throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "标签不能为空");
                }
                if (publishArticleRequest.getTagIds().size() > 3) {
                    throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "标签不能超过3个");
                }
                articleTagService.saveArticleTag(articleId[0], publishArticleRequest.getTagIds());
                
                // 4. 发布文章创建事件
                eventPublisher.publishArticleCreated(article);
                
                return 1;
            } catch (Exception e) {
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
}
