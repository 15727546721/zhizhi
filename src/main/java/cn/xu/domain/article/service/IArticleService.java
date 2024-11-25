package cn.xu.domain.article.service;

import cn.xu.api.dto.article.CreateArticleRequest;
import cn.xu.domain.article.model.entity.ArticleEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IArticleService {

    void createArticle(CreateArticleRequest createArticleRequest);

    String uploadCover(MultipartFile imageFile);

    List<ArticleEntity> listArticle(int page, int size);

    void deleteArticles(List<Long> articleIds);

    void updateArticle(CreateArticleRequest createArticleRequest);

    ArticleEntity getArticleById(Long id);
}
