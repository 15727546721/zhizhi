package cn.xu.domain.post.service;

import cn.xu.domain.post.model.entity.TagEntity;

import java.util.List;

/**
 * 标签服务接口
 * 负责标签相关的业务逻辑处理
 */
public interface ITagService {
    
    /**
     * 获取所有标签列表
     * 
     * @return 标签列表
     */
    List<TagEntity> getTagList();
    
    /**
     * 根据ID获取标签
     * 
     * @param id 标签ID
     * @return 标签实体
     */
    TagEntity getTagById(Long id);
    
    /**
     * 创建标签
     * 
     * @param name 标签名称
     * @return 标签实体
     */
    TagEntity createTag(String name);
    
    /**
     * 更新标签
     * 
     * @param id 标签ID
     * @param name 标签名称
     * @return 标签实体
     */
    TagEntity updateTag(Long id, String name);
    
    /**
     * 删除标签
     * 
     * @param id 标签ID
     */
    void deleteTag(Long id);
    
    /**
     * 根据帖子ID获取标签列表
     * 
     * @param postId 帖子ID
     * @return 标签列表
     */
    List<TagEntity> getTagsByPostId(Long postId);
    
    /**
     * 保存帖子标签关联关系
     * 
     * @param postId 帖子ID
     * @param tagIds 标签ID列表
     */
    void savePostTags(Long postId, List<Long> tagIds);
}