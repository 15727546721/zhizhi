package cn.xu.domain.article.service;

import cn.xu.api.dto.request.article.ArticleCreateDTO;

public interface IArticleService {

    void createArticle(ArticleCreateDTO articleCreateDTO);
}
