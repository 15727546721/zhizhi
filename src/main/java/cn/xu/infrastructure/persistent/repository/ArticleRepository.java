package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.article.model.aggregate.ArticleAggregate;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.entity.ArticleTagEntity;
import cn.xu.domain.article.repository.IArticleRepository;
import cn.xu.infrastructure.persistent.dao.IArticleDao;
import cn.xu.infrastructure.persistent.dao.IArticleTagDao;
import cn.xu.infrastructure.persistent.po.Article;
import cn.xu.infrastructure.persistent.po.ArticleTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Repository
public class ArticleRepository implements IArticleRepository {

    @Resource
    private IArticleDao articleDao;
    @Resource
    private IArticleTagDao articleTagDao;

    @Override
    public void save(ArticleAggregate articleAggregate) {
        log.info("save articleAggregate: " + articleAggregate);
        Article article = Article.builder()
                .id(articleAggregate.getArticleEntity().getId())
                .title(articleAggregate.getArticleEntity().getTitle())
                .description(articleAggregate.getArticleEntity().getDescription())
                .content(articleAggregate.getArticleEntity().getContent())
                .coverUrl(articleAggregate.getArticleEntity().getCoverUrl())
                .categoryId(articleAggregate.getArticleEntity().getCategoryId())
                .commentsEnabled(articleAggregate.getArticleEntity().getCommentsEnabled())
                .status(articleAggregate.getArticleEntity().getStatus())
                .isTop(articleAggregate.getArticleEntity().getIsTop())
                .build();
        articleDao.insert(article);
        List<ArticleTag> tags = new LinkedList<>();
        for (ArticleTagEntity tag : articleAggregate.getTags()) {
            tags.add(ArticleTag.builder()
                    .articleId(tag.getArticleId())
                    .tagId(tag.getTagId())
                    .build());
        }
        articleTagDao.insertTags(tags);
    }

    @Override
    public List<ArticleEntity> queryArticle(int page, int size) {
        log.info("query article page: " + page + " size: " + size);
        return articleDao.queryByPage(page - 1, size);
    }
}
