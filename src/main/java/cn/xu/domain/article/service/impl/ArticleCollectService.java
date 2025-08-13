package cn.xu.domain.article.service.impl;

import cn.xu.domain.article.model.entity.ArticleCollectEntity;
import cn.xu.domain.article.repository.IArticleCollectRepository;
import cn.xu.domain.article.service.IArticleCollectService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ArticleCollectService implements IArticleCollectService {

    @Resource
    private IArticleCollectRepository articleCollectRepository;

    @Override
    public boolean checkStatus(Long currentUserId, Long articleId) {
        // 查询用户是否收藏了该文章
        ArticleCollectEntity articleCollectEntity = articleCollectRepository.findByUserIdAndArticleId(currentUserId, articleId);

        // 如果查询结果不为空且 status == 1 (已收藏)，则返回 true
        return articleCollectEntity != null && articleCollectEntity.getStatus() == 1;
    }
}
