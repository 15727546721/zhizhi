package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.article.repository.IArticleTagRepository;
import cn.xu.infrastructure.persistent.dao.ArticleTagMapper;
import cn.xu.infrastructure.persistent.po.ArticleTagRel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Repository
public class ArticleTagRepository implements IArticleTagRepository {
    @Resource
    private ArticleTagMapper articleTagDao;

    @Override
    public void save(ArticleTagRel articleTagRel) {
        log.info("保存文章标签 articleTag: {}", articleTagRel);
        articleTagDao.insert(articleTagRel);
    }

    @Override
    public void saveArticleTag(Long articleId, List<Long> tagIds) {
        log.info("保存文章标签 articleId: {}, tagIds: {}", articleId, tagIds);
        List<ArticleTagRel> articleTagRels = new LinkedList<>();
        for (Long tagId : tagIds) {
            articleTagRels.add(ArticleTagRel.builder().articleId(articleId).tagId(tagId).build());
        }
        articleTagDao.insertBatchByList(articleTagRels);
    }

    @Override
    public void deleteByArticleId(Long articleId) {
        log.info("删除文章标签 articleId: {}", articleId);
        articleTagDao.deleteByArticleId(articleId);

    }

}
