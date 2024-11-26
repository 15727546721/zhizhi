package cn.xu.domain.article.service;

import cn.xu.domain.article.model.entity.TagEntity;

import java.util.List;

public interface ITagService {
    public void save(TagEntity tag);

    public List<TagEntity> queryTagList(int page, int size);

    public void update(TagEntity tagEntity);

    public void delete(List<Long> idList);

    List<TagEntity> getTagSelectList();

    List<TagEntity> getTagsByArticleId(Long id);
}
