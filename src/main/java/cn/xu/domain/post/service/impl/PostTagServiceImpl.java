package cn.xu.domain.post.service.impl;

import cn.xu.domain.post.model.entity.TagEntity;
import cn.xu.domain.post.repository.IPostTagRepository;
import cn.xu.domain.post.repository.ITagRepository;
import cn.xu.domain.post.service.IPostTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 帖子标签服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostTagServiceImpl implements IPostTagService {
    
    private final IPostTagRepository postTagRepository;
    
    @Resource
    private ITagRepository tagRepository;
    
    @Override
    public List<PostTagRelation> batchGetTagIdsByPostIds(List<Long> postIds) {
        try {
            return postTagRepository.batchGetTagIdsByPostIds(postIds);
        } catch (Exception e) {
            log.error("批量获取帖子标签ID列表失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<TagEntity> getTagList() {
        // 实现获取标签列表逻辑
        try {
            return tagRepository.getAllTags();
        } catch (Exception e) {
            log.error("获取标签列表失败", e);
            return null;
        }
    }
    
    @Override
    public TagEntity getTagById(Long id) {
        // 实现根据ID获取标签逻辑
        try {
            List<TagEntity> tags = tagRepository.getAllTags();
            if (tags != null) {
                return tags.stream()
                        .filter(tag -> tag.getId().equals(id))
                        .findFirst()
                        .orElse(null);
            }
            return null;
        } catch (Exception e) {
            log.error("根据ID获取标签失败, id: {}", id, e);
            return null;
        }
    }
    
    @Override
    public TagEntity createTag(String name) {
        // 实现创建标签逻辑
        try {
            tagRepository.addTag(name);
            // 重新获取标签列表以找到新创建的标签
            List<TagEntity> tags = tagRepository.getAllTags();
            if (tags != null) {
                return tags.stream()
                        .filter(tag -> tag.getName().equals(name))
                        .findFirst()
                        .orElse(null);
            }
            return null;
        } catch (Exception e) {
            log.error("创建标签失败, name: {}", name, e);
            return null;
        }
    }
    
    @Override
    public TagEntity updateTag(Long id, String name) {
        // 实现更新标签逻辑
        log.warn("更新标签功能暂未实现, id: {}, name: {}", id, name);
        // 这里需要调用TagRepository更新标签，但目前没有注入TagRepository
        // 暂时返回null，后续需要完善
        return null;
    }
    
    @Override
    public void deleteTag(Long id) {
        // 实现删除标签逻辑
        log.warn("删除标签功能暂未实现, id: {}", id);
        // 这里需要调用TagRepository删除标签，但目前没有注入TagRepository
        // 暂时不做任何操作，后续需要完善
    }
    
    @Override
    public List<TagEntity> getTagsByPostId(Long postId) {
        // 实现根据帖子ID获取标签列表逻辑
        try {
            // 通过PostTagRepository获取标签ID列表
            List<Long> tagIds = postTagRepository.getTagIdsByPostId(postId);
            if (tagIds != null && !tagIds.isEmpty()) {
                // 通过TagRepository获取标签详情
                List<TagEntity> allTags = tagRepository.getAllTags();
                if (allTags != null) {
                    return allTags.stream()
                            .filter(tag -> tagIds.contains(tag.getId()))
                            .collect(java.util.stream.Collectors.toList());
                }
            }
            return java.util.Collections.emptyList();
        } catch (Exception e) {
            log.error("根据帖子ID获取标签列表失败, postId: {}", postId, e);
            return java.util.Collections.emptyList();
        }
    }
    
    @Override
    public void savePostTags(Long postId, List<Long> tagIds) {
        // 实现保存帖子标签关联关系逻辑
        postTagRepository.savePostTag(postId, tagIds);
    }
    
    @Override
    public List<TagEntity> searchTags(String keyword) {
        // 实现搜索标签逻辑
        try {
            return tagRepository.searchTags(keyword);
        } catch (Exception e) {
            log.error("搜索标签失败, keyword: {}", keyword, e);
            return null;
        }
    }
    
    @Override
    public List<TagEntity> getHotTags(Integer limit) {
        // 实现获取热门标签逻辑
        try {
            int actualLimit = limit != null ? limit : 10;
            return tagRepository.getHotTags(actualLimit);
        } catch (Exception e) {
            log.error("获取热门标签失败, limit: {}", limit, e);
            return null;
        }
    }
}