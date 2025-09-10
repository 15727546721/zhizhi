package cn.xu.infrastructure.persistent.repository;


import cn.xu.domain.article.model.entity.ArticleCollectEntity;
import cn.xu.domain.article.repository.IArticleCollectRepository;
import cn.xu.infrastructure.persistent.dao.ArticleCollectMapper;
import cn.xu.infrastructure.persistent.po.ArticleCollect;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class ArticleCollectRepository implements IArticleCollectRepository {

    @Resource
    private ArticleCollectMapper articleCollectMapper;

    @Override
    public ArticleCollectEntity findByUserIdAndArticleId(Long userId, Long articleId) {
        ArticleCollect articleCollect = articleCollectMapper.findByUserIdAndArticleId(userId, articleId);
        return convertToEntity(articleCollect);
    }

    @Override
    public Long save(ArticleCollectEntity articleCollectEntity) {
        ArticleCollect articleCollect = convertToPO(articleCollectEntity);
        articleCollectMapper.insert(articleCollect);
        return articleCollect.getId();
    }

    @Override
    public void update(ArticleCollectEntity articleCollectEntity) {
        ArticleCollect articleCollect = convertToPO(articleCollectEntity);
        articleCollectMapper.updateById(articleCollect);
    }

    @Override
    public void deleteByUserIdAndArticleId(Long userId, Long articleId) {
        articleCollectMapper.deleteByUserIdAndArticleId(userId, articleId);
    }

    @Override
    public List<Long> findArticleIdsByUserId(Long userId) {
        return articleCollectMapper.findArticleIdsByUserId(userId);
    }

    @Override
    public int countByUserId(Long userId) {
        return articleCollectMapper.countByUserId(userId);
    }

    @Override
    public int batchSave(List<ArticleCollectEntity> articleCollectEntities) {
        if (articleCollectEntities == null || articleCollectEntities.isEmpty()) {
            return 0;
        }
        List<ArticleCollect> articleCollects = articleCollectEntities.stream()
                .map(this::convertToPO)
                .collect(java.util.stream.Collectors.toList());
        return articleCollectMapper.batchInsert(articleCollects);
    }

    @Override
    public int batchDeleteByUserIdAndArticleIds(Long userId, List<Long> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return 0;
        }
        return articleCollectMapper.batchDeleteByUserIdAndArticleIds(userId, articleIds);
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

    private ArticleCollect convertToPO(ArticleCollectEntity articleCollectEntity) {
        if (articleCollectEntity == null) {
            return null;
        }
        ArticleCollect articleCollect = new ArticleCollect();
        articleCollect.setId(articleCollectEntity.getId());
        articleCollect.setUserId(articleCollectEntity.getUserId());
        articleCollect.setArticleId(articleCollectEntity.getArticleId());
        articleCollect.setStatus(articleCollectEntity.getStatus());
        articleCollect.setCreateTime(articleCollectEntity.getCreateTime());
        return articleCollect;
    }
}