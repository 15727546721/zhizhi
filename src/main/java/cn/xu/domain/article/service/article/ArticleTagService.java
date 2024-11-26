package cn.xu.domain.article.service.article;

import cn.xu.domain.article.repository.IArticleTagRepository;
import cn.xu.domain.article.service.IArticleTagService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ArticleTagService implements IArticleTagService {

    @Resource
    private IArticleTagRepository articleTagRepository;
    @Override
    public void saveArticleTag(Long articleId, List<Long> tagIds) {
        articleTagRepository.saveArticleTag(articleId, tagIds);
    }

    @Override
    public void updateArticleTag(Long articleId, List<Long> tagIds) {
        articleTagRepository.deleteByArticleId(articleId);
        articleTagRepository.saveArticleTag(articleId, tagIds);
    }
}
