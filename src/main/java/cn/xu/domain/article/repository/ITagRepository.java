package cn.xu.domain.article.repository;

import cn.xu.domain.article.model.entity.TagEntity;
import cn.xu.infrastructure.persistent.po.ArticleTag;

import java.util.List;

public interface ITagRepository {
    void save(TagEntity tag);

    List<TagEntity> queryTagList(int page, int size);

    void update(TagEntity tag);

    void delete(List<Long> idList);

    ArticleTag findById(Long tagId);

    List<TagEntity> getTagSelectList();

    List<TagEntity> getTagsByArticleId(Long id);
}
