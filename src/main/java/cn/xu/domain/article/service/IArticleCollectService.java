package cn.xu.domain.article.service;

public interface IArticleCollectService {
    /**
     * 判断当前用户是否收藏了该文章
     *
     * @param currentUserId
     * @param articleId
     * @return
     */
    boolean checkStatus(Long currentUserId, Long articleId);
}
