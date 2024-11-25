package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.article.repository.IArticleTagRepository;
import cn.xu.infrastructure.persistent.dao.IArticleTagDao;
import cn.xu.infrastructure.persistent.po.ArticleTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

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
}
