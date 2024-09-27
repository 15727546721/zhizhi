package cn.xu.domain.article.service;

import cn.xu.api.dto.request.article.ArticleCreateDTO;
import org.springframework.web.multipart.MultipartFile;

public interface IArticleService {

    void createArticle(ArticleCreateDTO articleCreateDTO);

    String uploadCover(MultipartFile imageFile);
}
