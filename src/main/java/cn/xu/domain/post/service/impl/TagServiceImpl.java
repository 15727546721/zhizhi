package cn.xu.domain.post.service.impl;

import cn.xu.domain.post.model.entity.TagEntity;
import cn.xu.domain.post.repository.ITagRepository;
import cn.xu.domain.post.service.ITagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 标签服务实现类
 * 负责标签相关的业务逻辑处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements ITagService {
    
    private final ITagRepository tagRepository;
    
    @Override
    public List<TagEntity> getTagList() {
        try {
            return tagRepository.getAllTags();
        } catch (Exception e) {
            log.error("获取标签列表失败", e);
            return null;
        }
    }
    
    @Override
    public TagEntity getTagById(Long id) {
        try {
            return tagRepository.getTagById(id);
        } catch (Exception e) {
            log.error("根据ID获取标签失败, id: {}", id, e);
            return null;
        }
    }
    
    @Override
    public TagEntity createTag(String name) {
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
        try {
            if (id == null || name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("标签ID和名称不能为空");
            }
            
            // 调用TagRepository更新标签
            tagRepository.updateTag(id, name.trim());
            
            // 重新获取更新后的标签
            TagEntity updatedTag = tagRepository.getTagById(id);
            if (updatedTag == null) {
                log.error("更新标签后无法获取标签信息, id: {}", id);
                throw new RuntimeException("更新标签后无法获取标签信息");
            }
            
            log.info("更新标签成功, id: {}, name: {}", id, name);
            return updatedTag;
        } catch (IllegalArgumentException e) {
            log.error("更新标签参数错误, id: {}, name: {}", id, name, e);
            throw e;
        } catch (Exception e) {
            log.error("更新标签失败, id: {}, name: {}", id, name, e);
            throw new RuntimeException("更新标签失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteTag(Long id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("标签ID不能为空");
            }
            
            // 调用TagRepository删除标签（内部会处理关联关系）
            tagRepository.deleteTag(id);
            
            log.info("删除标签成功, id: {}", id);
        } catch (IllegalArgumentException e) {
            log.error("删除标签参数错误, id: {}", id, e);
            throw e;
        } catch (Exception e) {
            log.error("删除标签失败, id: {}", id, e);
            throw new RuntimeException("删除标签失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<TagEntity> getTagsByPostId(Long postId) {
        // 实现根据帖子ID获取标签列表逻辑
        // 当前ITagRepository接口没有提供根据帖子ID获取标签列表的方法
        // 暂时返回null，需要完善仓储接口
        return null;
    }
    
    @Override
    public void savePostTags(Long postId, List<Long> tagIds) {
        // 实现保存帖子标签关联关系逻辑
        // 这个功能应该由PostTagRepository来处理
        // 暂时不执行任何操作，需要完善仓储接口
    }
}