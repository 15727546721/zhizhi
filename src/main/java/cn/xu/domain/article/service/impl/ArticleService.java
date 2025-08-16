package cn.xu.domain.article.service.impl;

import cn.xu.api.system.model.dto.article.ArticleRequest;
import cn.xu.api.web.model.vo.article.ArticleDetailVO;
import cn.xu.api.web.model.vo.article.ArticleListVO;
import cn.xu.api.web.model.vo.article.ArticlePageVO;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.article.model.aggregate.ArticleAndAuthorAggregate;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.entity.TagEntity;
import cn.xu.domain.article.model.valobj.ArticleHotScorePolicy;
import cn.xu.domain.article.model.valobj.ArticleStatus;
import cn.xu.domain.article.repository.IArticleRepository;
import cn.xu.domain.article.repository.IArticleTagRepository;
import cn.xu.domain.article.repository.ITagRepository;
import cn.xu.domain.article.service.IArticleCollectService;
import cn.xu.domain.article.service.IArticleService;
import cn.xu.domain.file.service.MinioService;
import cn.xu.domain.follow.service.IFollowService;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.service.ILikeService;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import cn.xu.infrastructure.cache.RedisKeyManager;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.common.response.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * 文章服务实现类
 * 负责文章的创建、修改、删除、查询等核心业务逻辑
 */
@Slf4j
@Service
public class ArticleService implements IArticleService {

    @Resource
    private IArticleRepository articleRepository; // 文章仓储
    @Resource
    private IArticleTagRepository articleTagRepository; // 文章标签仓储
    @Resource
    private ITagRepository tagRepository; // 标签仓储
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private MinioService minioService; // minio客户端
    @Resource
    private IUserService userService;
    @Resource
    private IArticleCollectService articleCollectService; // 文章收藏服务
    @Resource
    private ILikeService likeService;
    @Resource
    private IFollowService followService;

    @Override
    public Long createArticle(ArticleEntity articleEntity) {
        try {
            log.info("[文章服务] 开始创建文章 - title: {}, userId: {}", articleEntity.getTitle(), articleEntity.getUserId());

            // 保存文章
            Long articleId = articleRepository.save(articleEntity);
            log.info("[文章服务] 文章创建成功 - articleId: {}", articleId);

            return articleId;
        } catch (Exception e) {
            log.error("[文章服务] 创建文章失败 - title: {}", articleEntity.getTitle(), e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "创建文章失败：" + e.getMessage());
        }
    }

    @Override
    public Long createOrUpdateArticleDraft(ArticleEntity articleEntity) {
        try {
            log.info("[文章服务] 开始创建文章草稿 - title: {}, userId: {}", articleEntity.getTitle(), articleEntity.getUserId());
            if (articleEntity.getId() == null) {
                // 保存文章
                Long articleId = articleRepository.save(articleEntity);
                log.info("[文章服务] 文章草稿创建成功 - articleId: {}", articleId);
                return articleId;
            } else {
                // 更新文章
                articleRepository.update(articleEntity);
                log.info("[文章服务] 文章草稿更新成功 - articleId: {}", articleEntity.getId());
                return articleEntity.getId();
            }
        } catch (Exception e) {
            log.error("[文章服务] 创建文章草稿失败 - title: {}", articleEntity.getTitle(), e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "创建文章草稿失败：" + e.getMessage());
        }
    }

    @Override
    public String uploadCover(MultipartFile imageFile) {
        try {
            log.info("[文章服务] 开始上传文章封面 - fileName: {}, size: {}", imageFile.getOriginalFilename(), imageFile.getSize());

            String uploadFileUrl = minioService.uploadFile(imageFile, null);
            if (uploadFileUrl == null) {
                log.error("[文章服务] 上传文章封面失败 - 上传服务返回空URL");
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "上传封面失败");
            }

            log.info("[文章服务] 文章封面上传成功 - url: {}", uploadFileUrl);
            return uploadFileUrl;
        } catch (Exception e) {
            log.error("[文章服务] 上传文章封面失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "上传封面失败：" + e.getMessage());
        }
    }

    @Override
    public PageResponse<List<ArticlePageVO>> listArticle(ArticleRequest articleRequest) {
        try {
            log.info("[文章服务] 开始分页查询文章列表 - pageNo: {}, pageSize: {}", articleRequest.getPageNo(), articleRequest.getPageSize());

            List<ArticlePageVO> articles = articleRepository.queryArticle(articleRequest);

            log.info("[文章服务] 分页查询文章列表成功 - 获取数量: {}", articles.size());
            return PageResponse.of(articleRequest.getPageNo(), articleRequest.getPageSize(), (long) articles.size(), articles);
        } catch (Exception e) {
            log.error("[文章服务] 分页查询文章列表失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询文章列表失败：" + e.getMessage());
        }
    }

    @Override
    public void deleteArticles(List<Long> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            log.warn("[文章服务] 删除文章：文章ID列表为空");
            return;
        }

        try {
            log.info("[文章服务] 开始批量删除文章 - articleIds: {}", articleIds);
            articleRepository.deleteByIds(articleIds);
            log.info("[文章服务] 批量删除文章成功 - 删除数量: {}", articleIds.size());
        } catch (Exception e) {
            log.error("[文章服务] 批量删除文章失败 - articleIds: {}", articleIds, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除文章失败：" + e.getMessage());
        }
    }

    @Override
    public void updateArticle(ArticleEntity articleEntity) {
        try {
            log.info("[文章服务] 开始更新文章 - articleId: {}", articleEntity.getId());
            articleRepository.update(articleEntity);
            log.info("[文章服务] 更新文章成功 - articleId: {}", articleEntity.getId());
        } catch (Exception e) {
            log.error("[文章服务] 更新文章失败 - articleId: {}", articleEntity.getId(), e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新文章失败：" + e.getMessage());
        }
    }

    @Override
    public ArticleAndAuthorAggregate getArticleDetailById(Long articleId) {
//        if (articleId == null) {
//            log.error("[文章服务] 获取文章详情失败：文章ID为空");
//            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文章ID不能为空");
//        }
//
//        try {
//            log.info("[文章服务] 开始获取文章详情 - articleId: {}", articleId);
//            ArticleEntity article = articleRepository.findById(articleId);
//
//            if (article == null) {
//                log.warn("[文章服务] 文章不存在 - articleId: {}", articleId);
//                throw new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "文章不存在");
//            }
//            UserEntity userEntity = userRepository.findById(article.getUserId());
//            if (userEntity == null) {
//                log.error("[文章服务] 作者不存在 - userId: {}", article.getUserId());
//                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "作者不存在");
//            }
//            return new ArticleAndAuthorAggregate(article, userEntity);
//        } catch (Exception e) {
//            log.error("[文章服务] 获取文章详情失败 - articleId: {}", articleId, e);
//            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取文章详情失败：" + e.getMessage());
//        }
        return null;
    }

    @Override
    public List<ArticleListVO> getArticlesByUserId(Long userId) {
        log.info("[文章服务] 开始获取用户文章列表 - userId: {}", userId);
        return articleRepository.queryArticleByUserId(userId);
    }

    @Override
    public void publishArticle(ArticleEntity articleEntity, Long userId) {
        Long id = articleEntity.getId();
        try {
            log.info("[文章服务] 开始发布文章 - articleId: {}, userId: {}", id, userId);
            ArticleEntity article = articleRepository.findById(articleEntity.getId());
            if (article == null) {
                log.error("[文章服务] 文章不存在 - articleId: {}", id);
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文章不存在");
            }
            if (article.getUserId() == null || !article.getUserId().equals(userId)) {
                log.error("[文章服务] 文章作者ID不匹配 - articleId: {}, userId: {}", id, userId);
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文章作者ID不匹配");
            }
            if (article.getStatus().equals(ArticleStatus.PUBLISHED)) {
                log.warn("[文章服务] 文章已发布 - articleId: {}", id);
                return;
            }

            articleRepository.update(articleEntity);
            log.info("[文章服务] 文章发布成功 - articleId: {}", id);
        } catch (Exception e) {
            log.error("[文章服务] 发布文章失败 - articleId: {}, userId: {}", id, userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "发布文章失败：" + e.getMessage());
        }
    }

    @Override
    public List<ArticleListVO> getDraftArticleList(Long userId) {
        try {
            log.info("[文章服务] 开始获取草稿文章列表 - userId: {}", userId);
            List<ArticleListVO> articles = articleRepository.queryDraftArticleListByUserId(userId);
            log.info("[文章服务] 获取草稿文章列表成功 - userId: {}, size: {}", userId, articles.size());
            return articles;
        } catch (Exception e) {
            log.error("[文章服务] 获取草稿文章列表失败 - userId: {}", userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取草稿文章列表失败：" + e.getMessage());
        }
    }

    @Override
    public void deleteArticle(Long id, Long userId) {
        try {
            log.info("[文章服务] 开始删除文章 - articleId: {}, userId: {}", id, userId);
            ArticleEntity article = articleRepository.findById(id);
            if (article == null) {
                log.warn("[文章服务] 文章不存在 - articleId: {}", id);
                return;
            }
            if (article.getUserId() == null || !article.getUserId().equals(userId)) {
                log.error("[文章服务] 文章作者ID不匹配 - articleId: {}, userId: {}", id, userId);
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文章作者ID不匹配");
            }
            articleRepository.deleteById(id);
            log.info("[文章服务] 删除文章成功 - articleId: {}", id);
        } catch (Exception e) {
            log.error("[文章服务] 删除文章失败 - articleId: {}, userId: {}", id, userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除文章失败：" + e.getMessage());
        }
    }

    @Override
    public void viewArticle(Long articleId) {
        try {
            log.info("[文章服务] 开始浏览文章 - articleId: {}", articleId);
            ArticleEntity article = articleRepository.findById(articleId);
            if (article == null) {
                log.warn("[文章服务] 文章不存在 - articleId: {}", articleId);
                return;
            }
            article.setViewCount(article.getViewCount() + 1);
            articleRepository.update(article);
            log.info("[文章服务] 文章浏览成功 - articleId: {}, viewCount: {}", articleId, article.getViewCount());
        } catch (Exception e) {
            log.error("[文章服务] 文章浏览失败 - articleId: {}", articleId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文章浏览失败：" + e.getMessage());
        }
    }

    /**
     * 更新文章热度到 Redis
     *
     * @param articleId 文章ID
     */
    public void updateArticleHotScore(Long articleId) {
        try {
            // 获取文章数据
            ArticleEntity article = articleRepository.findById(articleId);
            if (article == null) {
                log.warn("[文章服务] 文章不存在 - articleId: {}", articleId);
                throw new BusinessException("文章不存在");
            }

            // 计算热度分数
            double hotScore = ArticleHotScorePolicy.calculate(article.getLikeCount(), article.getCommentCount(), article.getViewCount(), article.getCreateTime());

            // 使用 RedisTemplate 更新文章热度（ZSet）
            ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
            zSetOperations.add(RedisKeyManager.articleHotRankKey(), articleId.toString(), hotScore);

            log.info("[文章服务] 文章热度更新成功 - articleId: {}, hotScore: {}", articleId, hotScore);

        } catch (Exception e) {
            log.error("[文章服务] 更新文章热度失败 - articleId: {}", articleId, e);
            throw new BusinessException("更新文章热度失败：" + e.getMessage());
        }
    }

    /**
     * 获取前N篇热度最高的文章
     *
     * @param topN 获取的文章数量
     * @return 热度前N篇文章的ID
     */
    public List<Long> getTopNHotArticles(int topN) {
        try {
            // 使用 RedisTemplate 获取热度前 N 篇文章的 ID
            ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
            Set<Object> topArticles = zSetOperations.reverseRange(RedisKeyManager.articleHotRankKey(), 0, topN - 1);

            // 如果没有获取到热度前 N 篇文章，直接返回空列表
            if (CollectionUtils.isEmpty(topArticles)) {
                return new java.util.ArrayList<>();  // Java 8 中返回一个空的 List
            }

            // 使用传统的方式将 Set 转换为 List<Long>（Java 8 兼容）
            List<Long> articleIds = new java.util.ArrayList<>();
            for (Object articleId : topArticles) {
                articleIds.add(Long.parseLong((String) articleId));  // 将字符串转为 Long
            }

            return articleIds;
        } catch (Exception e) {
            log.error("[文章服务] 获取热度前 N 篇文章失败", e);
            throw new BusinessException("获取热度前 N 篇文章失败：" + e.getMessage());
        }
    }

    @Override
    public List<ArticleEntity> getArticlePageListByCategoryId(Long categoryId, Integer pageNo, Integer pageSize) {
        return articleRepository.getArticlePageListByCategoryId(categoryId, pageNo, pageSize);
    }

    @Override
    public List<ArticleEntity> getArticlePageList(Integer pageNo, Integer pageSize) {
        return articleRepository.getArticlePageList(pageNo, pageSize);
    }

    @Override
    public ArticleDetailVO getArticleDetail(Long articleId, Long currentUserId) {
        ArticleEntity article = articleRepository.findById(articleId);
        if (article == null) {
            log.warn("[文章服务] 文章不存在 - articleId: {}", articleId);
            throw new BusinessException("文章不存在");
        }
        if (article.getStatus() == null || article.getStatus().equals(ArticleStatus.DRAFT)) {
            log.warn("[文章服务] 文章未发布 - articleId: {}", articleId);
            throw new BusinessException("文章未发布");
        }
        UserEntity user = userService.getUserById(article.getUserId());
        List<TagEntity> tags = tagRepository.getTagsByArticleId(articleId);

        boolean isLiked = likeService.checkStatus(currentUserId, LikeType.ARTICLE.getCode(), articleId);
        boolean isCollected = articleCollectService.checkStatus(currentUserId, articleId);
        boolean isFollowed = followService.checkStatus(currentUserId, article.getUserId());
        boolean isAuthor = user.getId().equals(currentUserId);

        return ArticleDetailVO.builder()
                .article(article)
                .user(user)
                .tags(tags)
                .isLiked(isLiked)
                .isCollected(isCollected)
                .isFollowed(isFollowed)
                .isAuthor(isAuthor)
                .build();
    }

    @Override
    public List<ArticleEntity> getAllPublishedArticles() {
        return articleRepository.findAllPublishedArticles();
    }

    @Override
    public List<ArticleEntity> getAllArticles() {
        return articleRepository.findAll();
    }

}

