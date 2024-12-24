package cn.xu.domain.article.service;

import cn.xu.api.controller.web.article.ArticleListDTO;
import cn.xu.api.dto.article.ArticlePageResponse;
import cn.xu.api.dto.article.ArticleRequest;
import cn.xu.api.dto.common.PageResponse;
import cn.xu.common.ResponseEntity;
import cn.xu.domain.article.model.entity.ArticleEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IArticleService {

    Long createArticle(ArticleEntity articleEntity);

    String uploadCover(MultipartFile imageFile);

    PageResponse<List<ArticlePageResponse>> listArticle(ArticleRequest articleRequest);

    void deleteArticles(List<Long> articleIds);

    void updateArticle(ArticleEntity articleEntity);

    ArticleEntity getArticleById(Long id);

    List<ArticleListDTO> getArticleByCategory(Long categoryId);
}
