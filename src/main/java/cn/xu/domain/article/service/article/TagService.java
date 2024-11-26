package cn.xu.domain.article.service.article;

import cn.xu.domain.article.model.entity.TagEntity;
import cn.xu.domain.article.repository.ITagRepository;
import cn.xu.domain.article.service.ITagService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TagService implements ITagService {

    @Resource
    private ITagRepository tagRepository;

    @Override
    public void save(TagEntity tag) {
        tagRepository.save(tag);
    }

    @Override
    public List<TagEntity> queryTagList(int page, int size) {
        List<TagEntity> tagEntities = tagRepository.queryTagList(page, size);
        return tagEntities;
    }

    @Override
    public void update(TagEntity tagEntity) {
        tagRepository.update(tagEntity);
    }

    @Override
    public void delete(List<Long> idList) {
        tagRepository.delete(idList);
    }

    @Override
    public List<TagEntity> getTagSelectList() {
        return tagRepository.getTagSelectList();
    }

    @Override
    public TagEntity getTagsByArticleId(Long id) {

        return tagRepository.getTagsByArticleId(id);
    }
}
