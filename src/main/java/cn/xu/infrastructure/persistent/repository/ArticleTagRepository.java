package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.article.repository.IArticleTagRepository;
import cn.xu.infrastructure.persistent.dao.IArticleTagDao;
import cn.xu.infrastructure.persistent.po.ArticleTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Repository
public class ArticleTagRepository implements IArticleTagRepository {
    @Resource
    private IArticleTagDao articleTagDao;

    @Override
    public void save(ArticleTag articleTag) {
        log.info("Saving articleTag: {}", articleTag);
        articleTagDao.insert(articleTag);
    }

    @Override
    public void saveArticleTag(Long articleId, List<Long> tagIds) {
        log.info("Saving articleId: {}, tagIds: {}", articleId, tagIds);
        List<ArticleTag> articleTags = new LinkedList<>();
        for (Long tagId : tagIds) {
            articleTags.add(ArticleTag.builder().articleId(articleId).tagId(tagId).build());
        }
        articleTagDao.insertBatchByList(articleTags);
    }

    @Override
    public void deleteByArticleId(Long articleId) {
        log.info("Deleting articleId: {}", articleId);
        articleTagDao.deleteByArticleId(articleId);

    }

}
