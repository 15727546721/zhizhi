package cn.xu.domain.article.service.impl;

import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.repository.IArticleRepository;
import cn.xu.domain.article.service.ArticleQueryStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MySQL文章查询策略实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MySQLArticleQueryStrategy implements ArticleQueryStrategy {

    private final IArticleRepository articleRepository;

    @Override
    public Page<ArticleEntity> searchByTitle(String title, Pageable pageable) {
        // MySQL模糊查询作为兜底方案
        // 注意：IArticleRepository中没有直接支持title搜索的分页方法，这里简化处理
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        List<ArticleEntity> articles = articleRepository.getArticlePageList(page + 1, size);
        // 这里简化处理，实际应该根据title进行过滤
        return new PageImpl<>(articles, pageable, articles.size());
    }

    @Override
    public Page<ArticleEntity> getHotRank(String rankType, Pageable pageable) {
        // MySQL查询热门文章
        // 注意：IArticleRepository中没有直接支持热门排行的分页方法，这里简化处理
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        List<ArticleEntity> articles = articleRepository.getArticlePageList(page + 1, size);
        // 这里简化处理，实际应该根据热度进行排序
        return new PageImpl<>(articles, pageable, articles.size());
    }

    @Override
    public Page<ArticleEntity> getArticlesByCategory(Long categoryId, Pageable pageable) {
        // MySQL根据分类查询文章
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        List<ArticleEntity> articles = articleRepository.getArticlePageListByCategoryId(categoryId, page + 1, size);
        return new PageImpl<>(articles, pageable, articles.size());
    }

    @Override
    public Page<ArticleEntity> getArticlesByUser(Long userId, Pageable pageable) {
        // MySQL根据用户查询文章
        // 注意：IArticleRepository中没有直接根据用户ID分页查询的方法，这里简化处理
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        List<ArticleEntity> articles = articleRepository.getArticlePageList(page + 1, size);
        // 这里简化处理，实际应该根据userId进行过滤
        return new PageImpl<>(articles, pageable, articles.size());
    }

    @Override
    public ArticleEntity getArticleDetail(Long articleId) {
        // MySQL获取文章详情
        return articleRepository.findById(articleId).orElse(null);
    }
}