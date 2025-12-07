package cn.xu.service.post;

import cn.xu.model.dto.post.PostTagRelation;
import cn.xu.model.entity.Tag;
import cn.xu.repository.IPostTagRepository;
import cn.xu.repository.ITagRepository;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签服务
 */
@Slf4j
@Service("tagService")
@RequiredArgsConstructor
public class TagService {

    private final ITagRepository tagRepository;
    private final IPostTagRepository postTagRepository;

    // ==================== 标签CRUD ====================

    /**
     * 获取所有标签
     */
    public List<Tag> getTagList() {
        try {
            List<Tag> tags = tagRepository.getAllTags();
            return tags != null ? tags : new ArrayList<>();
        } catch (Exception e) {
            log.error("获取标签列表出错", e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据ID获取标签
     */
    public Tag getTagById(Long id) {
        if (id == null) {
            throw new BusinessException("标签ID不能为空");
        }
        try {
            Tag tag = tagRepository.getTagById(id);
            if (tag == null) {
                throw new BusinessException("标签不存在");
            }
            return tag;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("根据ID获取标签出错, id: {}", id, e);
            throw new BusinessException("获取标签出错: " + e.getMessage());
        }
    }

    /**
     * 创建标签
     */
    @Transactional(rollbackFor = Exception.class)
    public Tag createTag(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("标签名称不能为空");
        }

        try {
            String trimmedName = name.trim();

            // 判断标签是否已存在
            List<Tag> existingTags = searchTags(trimmedName);
            if (existingTags != null && existingTags.stream().anyMatch(tag -> trimmedName.equals(tag.getName()))) {
                throw new BusinessException("标签已存在: " + trimmedName);
            }

            // 创建新标签并返回ID
            Long newId = tagRepository.addTag(trimmedName);
            if (newId == null) {
                throw new BusinessException("创建标签出错");
            }

            // 根据ID获取新创建的标签
            Tag newTag = getTagById(newId);
            if (newTag != null) {
                log.info("创建标签成功, name: {}, id: {}", trimmedName, newTag.getId());
                return newTag;
            }

            throw new BusinessException("创建标签失败");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建标签出错, name: {}", name, e);
            throw new BusinessException("创建标签出错: " + e.getMessage());
        }
    }

    /**
     * 更新标签
     */
    @Transactional(rollbackFor = Exception.class)
    public Tag updateTag(Long id, String name) {
        if (id == null || name == null || name.trim().isEmpty()) {
            throw new BusinessException("标签ID和名称不能为空");
        }

        try {
            String trimmedName = name.trim();

            // 获取标签并检查是否存在
            Tag existingTag = getTagById(id);
            if (existingTag == null) {
                throw new BusinessException("标签不存在, ID: " + id);
            }

            // 检查是否有重复的标签
            List<Tag> duplicateTags = searchTags(trimmedName);
            if (duplicateTags != null) {
                boolean hasDuplicate = duplicateTags.stream()
                        .anyMatch(tag -> !tag.getId().equals(id) && trimmedName.equals(tag.getName()));
                if (hasDuplicate) {
                    throw new BusinessException("标签名称已存在: " + trimmedName);
                }
            }

            // 更新标签
            tagRepository.updateTag(id, trimmedName);

            // 返回更新后的标签
            Tag updatedTag = getTagById(id);
            if (updatedTag == null) {
                throw new BusinessException("更新标签失败");
            }

            log.info("更新标签成功, id: {}, name: {} -> {}", id, existingTag.getName(), trimmedName);
            return updatedTag;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新标签出错, id: {}, name: {}", id, name, e);
            throw new BusinessException("更新标签出错: " + e.getMessage());
        }
    }

    /**
     * 更新标签
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTag(Tag tag) {
        if (tag == null || tag.getId() == null) {
            throw new BusinessException("标签对象和ID不能为空");
        }

        try {
            // 获取标签并检查是否存在
            Tag existingTag = getTagById(tag.getId());
            if (existingTag == null) {
                throw new BusinessException("标签不存在, ID: " + tag.getId());
            }

            // 如果更新了标签名称，则检查是否重复
            if (tag.getName() != null && !tag.getName().trim().isEmpty()) {
                String trimmedName = tag.getName().trim();
                if (!trimmedName.equals(existingTag.getName())) {
                    List<Tag> duplicateTags = searchTags(trimmedName);
                    if (duplicateTags != null) {
                        boolean hasDuplicate = duplicateTags.stream()
                                .anyMatch(t -> !t.getId().equals(tag.getId()) && trimmedName.equals(t.getName()));
                        if (hasDuplicate) {
                            throw new BusinessException("标签名称已存在: " + trimmedName);
                        }
                    }
                }
                tag.setName(trimmedName);
            } else {
                // 如果没有更新名称，则使用原名称
                tag.setName(existingTag.getName());
            }

            // 更新标签
            tagRepository.updateTag(tag);
            log.info("更新标签成功, id: {}", tag.getId());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新标签出错, id: {}", tag.getId(), e);
            throw new BusinessException("更新标签出错: " + e.getMessage());
        }
    }

    /**
     * 删除标签
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long id) {
        if (id == null) {
            throw new BusinessException("标签ID不能为空");
        }

        try {
            // 获取标签并检查是否存在
            Tag existingTag = getTagById(id);
            if (existingTag == null) {
                throw new BusinessException("标签不存在, ID: " + id);
            }

            // 删除标签
            tagRepository.deleteTag(id);

            log.info("删除标签成功, id: {}, name: {}", id, existingTag.getName());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除标签出错, id: {}", id, e);
            throw new BusinessException("删除标签出错: " + e.getMessage());
        }
    }

    /**
     * 切换标签的推荐状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleRecommended(Long id) {
        if (id == null) {
            throw new BusinessException("标签ID不能为空");
        }

        try {
            Tag tag = getTagById(id);
            if (tag == null) {
                throw new BusinessException("标签不存在, ID: " + id);
            }

            // 切换推荐状态
            int newStatus = (tag.getIsRecommended() == null || tag.getIsRecommended() == 0) ? 1 : 0;
            tag.setIsRecommended(newStatus);
            tag.setUpdateTime(java.time.LocalDateTime.now());
            tagRepository.updateTag(tag);

            log.info("切换标签推荐状态成功, id: {}, isRecommended: {}", id, newStatus);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("切换标签推荐状态出错, id: {}", id, e);
            throw new BusinessException("操作出错: " + e.getMessage());
        }
    }

    // ==================== 标签与帖子关系 ====================

    /**
     * 根据帖子ID获取标签列表
     */
    public List<Tag> getTagsByPostId(Long postId) {
        if (postId == null) {
            return Collections.emptyList();
        }

        try {
            // 获取标签ID列表
            List<Long> tagIds = postTagRepository.getTagIdsByPostId(postId);
            if (tagIds == null || tagIds.isEmpty()) {
                return Collections.emptyList();
            }

            // 获取标签信息
            List<Tag> allTags = getTagList();
            return allTags.stream()
                    .filter(tag -> tagIds.contains(tag.getId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据帖子ID获取标签列表出错, postId: {}", postId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 保存帖子标签关系
     */
    @Transactional(rollbackFor = Exception.class)
    public void savePostTags(Long postId, List<Long> tagIds) {
        if (postId == null) {
            throw new BusinessException("帖子ID不能为空");
        }

        try {
            // 校验标签ID是否有效
            if (tagIds != null && !tagIds.isEmpty()) {
                List<Tag> allTags = getTagList();
                List<Long> validTagIds = allTags.stream()
                        .map(Tag::getId)
                        .collect(Collectors.toList());

                List<Long> invalidIds = tagIds.stream()
                        .filter(id -> !validTagIds.contains(id))
                        .collect(Collectors.toList());

                if (!invalidIds.isEmpty()) {
                    throw new BusinessException("无效的标签ID: " + invalidIds);
                }
            }

            // 保存帖子与标签的关系
            postTagRepository.savePostTag(postId, tagIds);
            log.info("保存帖子标签关系成功, postId: {}, tagIds: {}", postId, tagIds);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("保存帖子标签关系出错, postId: {}, tagIds: {}", postId, tagIds, e);
            throw new BusinessException("保存帖子标签关系出错: " + e.getMessage());
        }
    }

    /**
     * 批量获取帖子标签ID列表
     */
    public List<PostTagRelation> batchGetTagIdsByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            return postTagRepository.batchGetTagIdsByPostIds(postIds);
        } catch (Exception e) {
            log.error("批量获取帖子标签ID出错, postIds: {}", postIds, e);
            return Collections.emptyList();
        }
    }

    /**
     * 删除帖子标签关系
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePostTags(Long postId) {
        if (postId == null) {
            throw new BusinessException("帖子ID不能为空");
        }

        try {
            postTagRepository.deletePostTags(postId);
            log.info("删除帖子标签关系成功, postId: {}", postId);
        } catch (Exception e) {
            log.error("删除帖子标签关系出错, postId: {}", postId, e);
            throw new BusinessException("删除帖子标签关系出错: " + e.getMessage());
        }
    }

    /**
     * 根据关键字搜索标签
     */
    public List<Tag> searchTags(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getTagList();
        }

        try {
            List<Tag> result = tagRepository.searchTags(keyword.trim());
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            log.error("搜索标签出错, keyword: {}", keyword, e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取热门标签
     */
    public List<Tag> getHotTags(Integer limit) {
        try {
            int actualLimit = limit != null && limit > 0 ? limit : 10;
            List<Tag> result = tagRepository.getHotTags(actualLimit);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            log.error("获取热门标签出错, limit: {}", limit, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 根据时间范围获取热门标签
     *
     * @param timeRange 时间范围：today、week、month、all
     * @param limit 返回数量
     * @return 热门标签列表
     */
    public List<Tag> getHotTagsByTimeRange(String timeRange, Integer limit) {
        // 简化实现：暂不根据时间范围过滤，直接返回热门标签
        return getHotTags(limit);
    }

    /**
     * 获取推荐标签
     */
    public List<Tag> getRecommendedTags() {
        try {
            // 默认返回热门标签
            return getHotTags(20);
        } catch (Exception e) {
            log.error("获取推荐标签出错", e);
            return new ArrayList<>();
        }
    }

    /**
     * 批量获取标签信息
     */
    public java.util.Map<Long, Tag> batchGetTags(java.util.Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new java.util.HashMap<>();
        }

        try {
            List<Tag> tags = tagRepository.findByIds(new ArrayList<>(tagIds));
            return tags.stream()
                    .collect(Collectors.toMap(Tag::getId, tag -> tag));
        } catch (Exception e) {
            log.error("批量获取标签出错, tagIds: {}", tagIds, e);
            return new java.util.HashMap<>();
        }
    }
}
