package cn.xu.api.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.model.dto.article.DraftRequest;
import cn.xu.api.web.model.dto.article.FindArticlePageByCategoryReq;
import cn.xu.api.web.model.dto.article.PublishOrDraftArticleRequest;
import cn.xu.api.web.model.vo.article.ArticleDetailVO;
import cn.xu.api.web.model.vo.article.ArticleListVO;
import cn.xu.api.web.model.vo.article.FindArticlePageListVO;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.article.model.aggregate.ArticleAndAuthorAggregate;
import cn.xu.domain.article.model.aggregate.ArticleAndTagAgg;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.entity.TagEntity;
import cn.xu.domain.article.model.valobj.ArticleStatus;
import cn.xu.domain.article.service.IArticleService;
import cn.xu.domain.article.service.IArticleTagService;
import cn.xu.domain.article.service.ITagService;
import cn.xu.domain.follow.service.IFollowService;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.service.ILikeService;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import cn.xu.infrastructure.common.annotation.ApiOperationLog;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.common.response.ResponseEntity;
import cn.xu.infrastructure.persistent.read.elastic.service.ArticleElasticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RequestMapping("api/article")
@RestController
@Tag(name = "文章接口", description = "文章相关接口")
@Slf4j
public class ArticleController {

    @Resource
    private IArticleService articleService;
    @Resource
    private IArticleTagService articleTagService;
    @Resource
    private ITagService tagService;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private ArticleElasticService articleElasticService;
    @Resource
    private IUserService userService;
    @Resource
    private ILikeService likeService;
    @Resource
    private IFollowService followService;

    @PostMapping("/page/category")
    @ApiOperationLog(description = "通过分类获取文章列表")
    @Operation(summary = "通过分类获取文章列表")
    public ResponseEntity<List<FindArticlePageListVO>> getArticleByCategoryId(@RequestBody FindArticlePageByCategoryReq request) {
        List<FindArticlePageListVO> result = new LinkedList<>();
        List<ArticleEntity> articleList;
        if (request.getCategoryId() == null) {
            articleList = articleService.getArticlePageList(request.getPageNo(), request.getPageSize());
        } else {
            articleList = articleService.getArticlePageListByCategoryId(request.getCategoryId(), request.getPageNo(), request.getPageSize());
        }
        List<Long> articleIds = articleList.stream().map(ArticleEntity::getId).collect(Collectors.toList());
        List<Long> userIds = articleList.stream().map(ArticleEntity::getUserId).collect(Collectors.toList());
        List<UserEntity> userList = userService.batchGetUserInfo(userIds);
        List<ArticleAndTagAgg> articleAndTagAggs = tagService.batchGetTagListByArticleIds(articleIds);
        articleList.stream().forEach(article -> {
            result.add(FindArticlePageListVO.builder()
                    .article(article)
                    .user(userList.stream().filter(user -> user.getId().equals(article.getUserId())).findFirst().get())
                    .tags(articleAndTagAggs.stream().filter(agg -> agg.getArticleId().equals(article.getId())).findFirst().get().getTags().split(","))
                    .build());
        });
        return ResponseEntity.<List<FindArticlePageListVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(result)
                .build();
    }


    @GetMapping("/{id}")
    @Operation(summary = "获取文章详情")
    public ResponseEntity<ArticleDetailVO> getArticleDetail(@PathVariable("id") Long id) {
        log.info("获取文章详情，文章ID：{}", id);
        ArticleAndAuthorAggregate article = articleService.getArticleDetailById(id);
        List<TagEntity> tag = tagService.getTagsByArticleId(id);
        boolean login = StpUtil.isLogin();
        boolean likeStatus = false;
        boolean followStatus = false;
        boolean isAuthor = false;
        if (login) {
            isAuthor = article.getUser().getId().equals(StpUtil.getLoginIdAsLong());
            likeStatus = likeService.checkStatus(StpUtil.getLoginIdAsLong(),
                    LikeType.ARTICLE.getCode(),
                    article.getArticle().getId());
            followStatus = followService.checkStatus(StpUtil.getLoginIdAsLong(),
                    article.getUser().getId());
        }
        return ResponseEntity.<ArticleDetailVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("文章获取成功")
                .data(ArticleDetailVO.builder()
                        .article(article.getArticle())
                        .user(article.getUser())
                        .tagNameList(tag.stream().map(TagEntity::getName).collect(Collectors.toList()))
                        .isLiked(likeStatus)
                        .isFollowed(followStatus)
                        .isAuthor(isAuthor)
                        .build())
                .build();
    }

    @PostMapping("/create")
    @Operation(summary = "创建文章")
    @ApiOperationLog(description = "创建文章")
    public ResponseEntity<Long> createArticle(@RequestBody PublishOrDraftArticleRequest publishArticleRequest) {
        Long userId = StpUtil.getLoginIdAsLong();
        AtomicLong articleId = new AtomicLong();
        transactionTemplate.execute(status -> {
            // 事务开始
            try {
                //1. 保存文章和分类id
                long id = articleService.createArticle(ArticleEntity.builder()
                        .categoryId(publishArticleRequest.getCategoryId())
                        .title(publishArticleRequest.getTitle())
                        .coverUrl(publishArticleRequest.getCoverUrl())
                        .content(publishArticleRequest.getContent())
                        .description(publishArticleRequest.getDescription())
                        .userId(userId) // 当前登录用户ID
                        .status(ArticleStatus.PUBLISHED)
                        .build());
                articleId.set(id);
                //2. 保存文章标签
                if (publishArticleRequest.getTagIds() == null || publishArticleRequest.getTagIds().isEmpty()) {
                    throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "标签不能为空");
                }
                if (publishArticleRequest.getTagIds().size() > 3) {
                    throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "标签不能超过3个");
                }
                articleTagService.saveArticleTag(articleId.get(), publishArticleRequest.getTagIds());
                // 事务提交
                return 1;
            } catch (Exception e) {
                // 事务回滚
                status.setRollbackOnly();
                log.error("文章发布失败", e);
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文章发布失败");
            }
        });
        log.info("文章创建成功");
        return ResponseEntity.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(articleId.longValue())
                .info("文章创建成功")
                .build();
    }

    @PostMapping("/publish")
    @Operation(summary = "发布文章")
    @SaCheckLogin
    public ResponseEntity pushArticle(@RequestBody PublishOrDraftArticleRequest publishArticleRequest) {
        log.info("发布文章，文章内容：{}", publishArticleRequest);
        if (publishArticleRequest != null && publishArticleRequest.getId() != null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "文章ID不能为空");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        transactionTemplate.execute(status -> {
            // 事务开始
            try {
                Long articleId = publishArticleRequest.getId();
                //1. 保存文章和分类id
                articleService.publishArticle(ArticleEntity.builder()
                        .id(publishArticleRequest.getId())
                        .categoryId(publishArticleRequest.getCategoryId())
                        .title(publishArticleRequest.getTitle())
                        .coverUrl(publishArticleRequest.getCoverUrl())
                        .content(publishArticleRequest.getContent())
                        .description(publishArticleRequest.getDescription())
                        .userId(userId) // 当前登录用户ID
                        .status(ArticleStatus.PUBLISHED)
                        .build(), userId);
                //2. 保存文章标签
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
        return ResponseEntity.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("文章发布成功")
                .build();
    }

    @PostMapping("/saveDraft")
    @Operation(summary = "保存文章草稿")
    @SaCheckLogin
    public ResponseEntity saveArticleDraft(@RequestBody DraftRequest draftRequest) {
        log.info("保存文章草稿，文章内容：{}", draftRequest);
        Long userId = StpUtil.getLoginIdAsLong();
        //1. 保存文章，并将状态设置为草稿
        Long articleId = articleService.createOrUpdateArticleDraft(ArticleEntity.builder()
                .id(draftRequest.getId())
                .title(draftRequest.getTitle())
                .content(draftRequest.getContent())
                .userId(userId)
                .status(ArticleStatus.DRAFT)
                .build());
        log.info("文章草稿保存成功");
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("文章草稿保存成功")
                .build();
    }

    @GetMapping("/draft/list")
    @Operation(summary = "获取用户文章草稿列表")
    @SaCheckLogin
    public ResponseEntity<List<ArticleListVO>> getDraftArticles() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<ArticleListVO> draftArticles = articleService.getDraftArticleList(userId);
        return ResponseEntity.<List<ArticleListVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(draftArticles)
                .build();
    }

    @GetMapping("/draft/{id}")
    @Operation(summary = "删除文章草稿")
    @SaCheckLogin
    public ResponseEntity deleteDraftArticle(@PathVariable("id") Long id) {
        log.info("删除文章草稿，文章ID：{}", id);
        Long userId = StpUtil.getLoginIdAsLong();
        articleService.deleteArticle(id, userId);
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("文章草稿删除成功")
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

    @GetMapping("/user/articles")
    @SaCheckLogin
    @Operation(summary = "根据用户ID查询发布的文章列表")
    public ResponseEntity<List<ArticleListVO>> getArticlesByUserId() {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("根据用户ID查询发布的文章列表，用户ID：{}", userId);
        List<ArticleListVO> articles = articleService.getArticlesByUserId(userId);
        return ResponseEntity.<List<ArticleListVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(articles)
                .build();
    }

    @GetMapping("/view")
    @Operation(summary = "文章阅读数+1")
    public ResponseEntity<?> viewArticle(@RequestParam Long articleId) {
        articleService.viewArticle(articleId);
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("文章阅读数+1成功")
                .build();
    }

    @GetMapping("/like/{id}")
    @Operation(summary = "文章点赞")
    public ResponseEntity<?> likeArticle(@PathVariable("id") Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        likeService.like(userId, LikeType.ARTICLE.getCode(), id);
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("文章点赞成功")
                .build();
    }

    @GetMapping("/unlike/{id}")
    @Operation(summary = "取消文章点赞")
    public ResponseEntity<?> unlikeArticle(@PathVariable("id") Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        likeService.unlike(userId, LikeType.ARTICLE.getCode(), articleId);
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("文章取消点赞成功")
                .build();
    }
}
