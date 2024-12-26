package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.article.event.ArticleEvent;
import cn.xu.domain.like.repository.ILikeRepository;
import cn.xu.infrastructure.persistent.dao.ILikeDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Slf4j
@Repository
public class LikeRepository implements ILikeRepository {
    @Resource
    private ILikeDao likeDao;

    @Override
    public void insertArticleLikeRecord(ArticleEvent event) {
        log.info("文章点赞: {}", event);
        likeDao.insertArticleLikeRecord(event.getArticleId(), event.getUserId(), event.isAdd() ? 1 : 0, 1);
    }
}
