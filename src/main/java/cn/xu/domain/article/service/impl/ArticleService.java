package cn.xu.domain.article.service.impl;

import cn.xu.api.system.model.dto.article.ArticleRequest;
import cn.xu.api.web.model.vo.article.ArticleListVO;
import cn.xu.api.web.model.vo.article.ArticlePageVO;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.repository.IArticleRepository;
import cn.xu.domain.article.repository.IArticleTagRepository;
import cn.xu.domain.article.repository.ITagRepository;
import cn.xu.domain.article.service.IArticleService;
import cn.xu.domain.file.service.MinioService;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.common.response.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

/**
 * 文章服务实现类
 * 负责文章的创建、修改、删除、查询等核心业务逻辑
 *
 * @author xu
 */
@Service
public class ArticleService implements IArticleService {

    private static final Logger log = LoggerFactory.getLogger(ArticleService.class);

    @Resource
    private IArticleRepository articleRepository; // 文章仓储
    @Resource
    private IArticleTagRepository articleTagRepository; // 文章标签仓储

    @Resource
    private ITagRepository tagRepository; // 标签仓储

    @Resource
    private MinioService minioService; // minio客户端

    @Override
    public Long createArticle(ArticleEntity articleEntity) {
        try {
            log.info("[文章服务] 开始创建文章 - title: {}, userId: {}", 
                    articleEntity.getTitle(), articleEntity.getUserId());
            
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
    public String uploadCover(MultipartFile imageFile) {
        try {
            log.info("[文章服务] 开始上传文章封面 - fileName: {}, size: {}", 
                    imageFile.getOriginalFilename(), imageFile.getSize());
            
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
            log.info("[文章服务] 开始分页查询文章列表 - pageNo: {}, pageSize: {}", 
                    articleRequest.getPageNo(), articleRequest.getPageSize());
            
            List<ArticlePageVO> articles = articleRepository.queryArticle(articleRequest);
            
            log.debug("[文章服务] 分页查询文章列表成功 - 获取数量: {}", articles.size());
            return PageResponse.of(
                    articleRequest.getPageNo(),
                    articleRequest.getPageSize(),
                    (long) articles.size(),
                    articles
            );
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
    public ArticleEntity getArticleById(Long articleId) {
        if (articleId == null) {
            log.error("[文章服务] 获取文章详情失败：文章ID为空");
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文章ID不能为空");
        }

        try {
            log.info("[文章服务] 开始获取文章详情 - articleId: {}", articleId);
            ArticleEntity article = articleRepository.findById(articleId);
            
            if (article == null) {
                log.warn("[文章服务] 文章不存在 - articleId: {}", articleId);
                return null;
            }
            
            log.debug("[文章服务] 获取文章详情成功 - articleId: {}, title: {}", 
                    articleId, article.getTitle());
            return article;
        } catch (Exception e) {
            log.error("[文章服务] 获取文章详情失败 - articleId: {}", articleId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取文章详情失败：" + e.getMessage());
        }
    }

    @Override
    public List<ArticleListVO> getArticleByCategory(Long categoryId) {
        try {
            log.info("[文章服务] 开始获取分类文章列表 - categoryId: {}", categoryId);
            List<ArticleListVO> articles = articleRepository.queryArticleByCategory(categoryId);
            log.debug("[文章服务] 获取分类文章列表成功 - categoryId: {}, size: {}", 
                    categoryId, articles.size());
            return articles;
        } catch (Exception e) {
            log.error("[文章服务] 获取分类文章列表失败 - categoryId: {}", categoryId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取分类文章列表失败：" + e.getMessage());
        }
    }

    @Override
    public List<ArticleListVO> getHotArticles(int limit) {
        return null;
    }

    @Override
    public List<ArticleListVO> getUserLikedArticles(Long userId) {
        return null;
    }

    @Override
    public List<Long> getArticleLikedUsers(Long articleId) {
        return null;
    }

    @Override
    public boolean isArticleLiked(Long articleId, Long userId) {
        return false;
    }

    @Override
    public List<ArticleEntity> getAllArticles() {
        return articleRepository.findAll();
    }

    @Override
    public Long getArticleAuthorId(Long articleId) {
        if (articleId == null) {
            log.error("[文章服务] 获取文章作者ID失败：文章ID为空");
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文章ID不能为空");
        }

        try {
            log.info("[文章服务] 开始获取文章作者ID - articleId: {}", articleId);
            ArticleEntity article = articleRepository.findById(articleId);
            
            if (article == null) {
                log.warn("[文章服务] 文章不存在 - articleId: {}", articleId);
                return null;
            }
            
            if (article.getUserId() == null) {
                log.error("[文章服务] 文章数据异常，作者ID为空 - articleId: {}", articleId);
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文章数据异常");
            }
            
            log.debug("[文章服务] 获取文章作者ID成功 - articleId: {}, authorId: {}", 
                    articleId, article.getUserId());
            return article.getUserId();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[文章服务] 获取文章作者ID失败 - articleId: {}", articleId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取文章作者信息失败：" + e.getMessage());
        }
    }

}

