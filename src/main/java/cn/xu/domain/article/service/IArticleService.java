package cn.xu.domain.article.service;

import cn.xu.domain.article.model.entity.ArticleEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IArticleService {

    Long createArticle(ArticleEntity articleEntity);

    String uploadCover(MultipartFile imageFile);

    List<ArticleEntity> listArticle(int page, int size);

    void deleteArticles(List<Long> articleIds);

    void updateArticle(ArticleEntity articleEntity);

    ArticleEntity getArticleById(Long id);
}
