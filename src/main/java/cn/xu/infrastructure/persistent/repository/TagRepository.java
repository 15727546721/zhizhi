package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.post.model.aggregate.PostAndTagAgg;
import cn.xu.domain.post.model.entity.TagEntity;
import cn.xu.domain.post.repository.IPostTagRepository;
import cn.xu.domain.post.repository.ITagRepository;
import cn.xu.infrastructure.persistent.converter.TagConverter;
import cn.xu.infrastructure.persistent.dao.TagMapper;
import cn.xu.infrastructure.persistent.po.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TagRepository implements ITagRepository {

    private final TagMapper tagMapper;
    private final IPostTagRepository postTagRepository;
    private final TagConverter tagConverter = TagConverter.INSTANCE;

    @Override
    public void addTag(String name) {
        tagMapper.addTag(name);
    }

    @Override
    public List<String> getTagNamesByPostId(Long postId) {
        return tagMapper.getTagNamesByPostId(postId);
    }

    @Override
    public List<PostAndTagAgg> selectByPostIds(List<Long> postIds) {
        return tagMapper.selectByPostIds(postIds);
    }
    
    @Override
    public List<TagEntity> searchTags(String keyword) {
        List<Tag> tags = tagMapper.searchTags(keyword);
        return tags.stream()
                .map(tagConverter::toDomainEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TagEntity> getHotTags(int limit) {
        List<Tag> tags = tagMapper.getHotTags(limit);
        return tags.stream()
                .map(tagConverter::toDomainEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TagEntity> getAllTags() {
        List<Tag> tags = tagMapper.getAllTags();
        return tags.stream()
                .map(tagConverter::toDomainEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Long> findTagIdsByPostId(Long postId) {
        if (postId == null) {
            return new ArrayList<>();
        }
        // 通过PostTagRepository获取标签ID列表
        return postTagRepository.getTagIdsByPostId(postId);
    }
}