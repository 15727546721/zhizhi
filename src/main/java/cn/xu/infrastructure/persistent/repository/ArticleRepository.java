package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.repository.IArticleRepository;
import cn.xu.infrastructure.persistent.dao.IArticleDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Slf4j
@Repository
public class ArticleRepository implements IArticleRepository {

    @Resource
    private IArticleDao articleDao;

    @Override
    public void save(ArticleEntity articleEntity) {
        log.info("save articleEntity: " + articleEntity);
        articleDao.insert(articleEntity);
    }
}
