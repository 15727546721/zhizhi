package cn.xu.domain.article.service.article;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.controller.web.article.ArticleRecommendOrNewRequest;
import cn.xu.api.dto.article.ArticlePageResponse;
import cn.xu.api.dto.article.ArticleRequest;
import cn.xu.api.dto.common.PageResponse;
import cn.xu.common.Constants;
import cn.xu.common.ResponseEntity;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.entity.ArticleRecommendOrNew;
import cn.xu.domain.article.repository.IArticleRepository;
import cn.xu.domain.article.repository.IArticleTagRepository;
import cn.xu.domain.article.repository.ITagRepository;
import cn.xu.domain.article.service.IArticleService;
import cn.xu.domain.file.service.MinioService;
import cn.xu.exception.AppException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional(rollbackFor = Exception.class)
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
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "上传封面失败");
        }
        if (uploadFileUrl == null) {
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "上传封面失败");
        }
        return uploadFileUrl;
    }

    @Override
    public PageResponse<List<ArticlePageResponse>> listArticle(ArticleRequest articleRequest) {
        List<ArticlePageResponse> articles = articleRepository.queryArticle(articleRequest);
        return PageResponse.<List<ArticlePageResponse>>builder()
                .data(articles)
                .total(articles.size())
                .page(articleRequest.getPage())
                .size(articleRequest.getSize())
                .build();
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
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "未查询到文章");
        }

        return article;
    }

    @Override
    public ResponseEntity getRecommendArticleOrNewArticle(ArticleRecommendOrNewRequest articleRequest) {
        articleRequest.getType();
        Long userId = StpUtil.getLoginIdAsLong();
        if (articleRequest.getType() == Constants.ArticleType.RECOMMEND.getCode()) {
            // 推荐文章
        } else if (articleRequest.getType() == Constants.ArticleType.NEW.getCode()) {
            // 最新文章
        } else {
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "请求类型错误");
        }
        //目前只需要简单的分页查询，其他先不管后续逻辑以后实现
        List<ArticleRecommendOrNew> articles = articleRepository.queryArticleByPage();
        return ResponseEntity.builder().data(articles).build();
    }

}

