package cn.xu.domain.article.service.article;

import cn.xu.api.web.controller.article.ArticleListDTO;
import cn.xu.api.web.model.dto.article.ArticlePageResponse;
import cn.xu.api.web.model.dto.article.ArticleRequest;
import cn.xu.api.web.model.dto.common.PageResponse;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.repository.IArticleRepository;
import cn.xu.domain.article.repository.IArticleTagRepository;
import cn.xu.domain.article.repository.ITagRepository;
import cn.xu.domain.article.service.IArticleService;
import cn.xu.domain.file.service.MinioService;
import cn.xu.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ArticleService implements IArticleService {

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
        // 保存逻辑
        return articleRepository.save(articleEntity);
    }

    @Override
    public String uploadCover(MultipartFile imageFile) {
        String uploadFileUrl = null;
        try {
            uploadFileUrl = minioService.uploadFile(imageFile, null);
        } catch (Exception e) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "上传封面失败");
        }
        if (uploadFileUrl == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "上传封面失败");
        }
        return uploadFileUrl;
    }

    @Override
    public PageResponse<List<ArticlePageResponse>> listArticle(ArticleRequest articleRequest) {
        List<ArticlePageResponse> articles = articleRepository.queryArticle(articleRequest);
        return PageResponse.of(
                articleRequest.getPageNo(),
                articleRequest.getPageSize(),
                (long) articles.size(),
                articles
        );
    }

    @Override
    public void deleteArticles(List<Long> articleIds) {
        articleRepository.deleteByIds(articleIds);
    }

    @Override
    public void updateArticle(ArticleEntity articleEntity) {
        articleRepository.update(articleEntity);
    }

    @Override
    public ArticleEntity getArticleById(Long id) {
        ArticleEntity article = articleRepository.findById(id);
        if (article == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "未查询到文章");
        }

        return article;
    }

    @Override
    public List<ArticleListDTO> getArticleByCategory(Long categoryId) {
        return articleRepository.queryArticleByCategory(categoryId);
    }

    @Override
    public List<ArticleListDTO> getHotArticles(int limit) {
        return null;
    }

    @Override
    public List<ArticleListDTO> getUserLikedArticles(Long userId) {
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

}

