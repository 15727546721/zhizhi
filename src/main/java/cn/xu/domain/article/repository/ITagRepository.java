package cn.xu.domain.article.repository;

import cn.xu.domain.article.model.entity.TagEntity;

import java.util.List;

public interface ITagRepository {
    void save(TagEntity tag);

    List<TagEntity> queryTagList(int page, int size);

    void update(TagEntity tag);

    void delete(List<Long> idList);
}
