package cn.xu.domain.article.service;

import cn.xu.api.dto.article.CreateArticleRequest;
import cn.xu.api.dto.article.ArticleListResponse;
import org.springframework.web.multipart.MultipartFile;

public interface IArticleService {

    void createArticle(CreateArticleRequest createArticleRequest);

    String uploadCover(MultipartFile imageFile);

    ArticleListResponse listArticle(int page, int size);
}
