package cn.xu.infrastructure.persistent.repository;


import cn.xu.domain.article.model.entity.ArticleCollectEntity;
import cn.xu.domain.article.repository.IArticleCollectRepository;
import cn.xu.infrastructure.persistent.dao.ArticleCollectMapper;
import cn.xu.infrastructure.persistent.po.ArticleCollect;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class ArticleCollectRepository implements IArticleCollectRepository {

    @Resource
    private ArticleCollectMapper articleCollectMapper;

    @Override
    public ArticleCollectEntity findByUserIdAndArticleId(Long userId, Long articleId) {
        ArticleCollect articleCollect = articleCollectMapper.findByUserIdAndArticleId(userId, articleId);
        return convertToEntity(articleCollect);
    }

    private ArticleCollectEntity convertToEntity(ArticleCollect articleCollect) {
        if (articleCollect == null) {
            return null;
        }
        return ArticleCollectEntity.builder()
                .id(articleCollect.getId())
                .userId(articleCollect.getUserId())
                .articleId(articleCollect.getArticleId())
                .status(articleCollect.getStatus())
                .createTime(articleCollect.getCreateTime())
                .build();
    }
}
