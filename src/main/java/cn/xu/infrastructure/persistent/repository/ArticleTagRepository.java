package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.article.repository.IArticleTagRepository;
import cn.xu.infrastructure.persistent.dao.IArticleTagDao;
import cn.xu.infrastructure.persistent.po.ArticleTagRelation;
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
    public void save(ArticleTagRelation articleTagRelation) {
        log.info("保存文章标签 articleTag: {}", articleTagRelation);
        articleTagDao.insert(articleTagRelation);
    }

    @Override
    public void saveArticleTag(Long articleId, List<Long> tagIds) {
        log.info("保存文章标签 articleId: {}, tagIds: {}", articleId, tagIds);
        List<ArticleTagRelation> articleTagRelations = new LinkedList<>();
        for (Long tagId : tagIds) {
            articleTagRelations.add(ArticleTagRelation.builder().articleId(articleId).tagId(tagId).build());
        }
        articleTagDao.insertBatchByList(articleTagRelations);
    }

    @Override
    public void deleteByArticleId(Long articleId) {
        log.info("删除文章标签 articleId: {}", articleId);
        articleTagDao.deleteByArticleId(articleId);

    }

}
