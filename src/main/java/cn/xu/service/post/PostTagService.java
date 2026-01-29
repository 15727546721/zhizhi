package cn.xu.service.post;

import cn.xu.common.ResponseCode;
import cn.xu.model.dto.post.PostTagRelation;
import cn.xu.model.entity.Tag;
import cn.xu.repository.PostTagRepository;
import cn.xu.repository.TagRepository;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 帖子标签服务
 * <p>负责帖子与标签的关联管理</p>

 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostTagService {

    private final PostTagRepository postTagRepository;
    private final TagRepository tagRepository;

    public List<PostTagRelation> batchGetTagIdsByPostIds(List<Long> postIds) {
        try {
            return postTagRepository.batchGetTagIdsByPostIds(postIds);
        } catch (Exception e) {
            log.error("获取帖子标签ID列表时发生错误", e);
            return new ArrayList<>();
        }
    }

    public List<Tag> getTagList() {
        try {
            List<Tag> tags = tagRepository.getAllTags();
            return tags != null ? tags : Collections.emptyList();
        } catch (Exception e) {
            log.error("获取标签列表时发生错误", e);
            return Collections.emptyList();
        }
    }

    public Tag getTagById(Long id) {
        if (id == null) {
            return null;
        }
        try {
            return tagRepository.getTagById(id);
        } catch (Exception e) {
            log.error("根据ID获取标签时发生错误, id: {}", id, e);
            return null;
        }
    }

    public Tag createTag(String name) {
        if (name == null || name.trim().isEmpty()) {
            log.warn("创建标签时失败: 标签名称不能为空");
            return null;
        }
        try {
            String trimmedName = name.trim();
            tagRepository.addTag(trimmedName);
            List<Tag> tags = tagRepository.searchTags(trimmedName);
            if (tags != null && !tags.isEmpty()) {
                return tags.stream()
                        .filter(tag -> trimmedName.equals(tag.getName()))
                        .findFirst()
                        .orElse(tags.get(0));
            }
            log.warn("创建标签失败，未能找到相应的标签, name: {}", trimmedName);
            return null;
        } catch (Exception e) {
            log.error("创建标签时发生错误, name: {}", name, e);
            return null;
        }
    }

    public Tag updateTag(Long id, String name) {
        try {
            if (id == null || name == null || name.trim().isEmpty()) {
                log.warn("更新标签时参数错误, id: {}, name: {}", id, name);
                throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "标签 ID和名称不能为空");
            }

            tagRepository.updateTag(id, name.trim());

            Tag updatedTag = tagRepository.getTagById(id);
            if (updatedTag == null) {
                log.error("更新标签失败，未找到标签, id: {}", id);
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新标签失败，标签不存在");
            }

            log.info("更新标签成功, id: {}, name: {}", id, name);
            return updatedTag;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新标签时发生错误, id: {}, name: {}", id, name, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新标签失败，请稍后重试");
        }
    }

    public void deleteTag(Long id) {
        try {
            if (id == null) {
                log.warn("删除标签时参数错误, id为空");
                throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "标签ID不能为空");
            }

            tagRepository.deleteTag(id);
            log.info("删除标签成功, id: {}", id);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除标签时发生错误, id: {}", id, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除标签失败，请稍后重试");
        }
    }

    public List<Tag> getTagsByPostId(Long postId) {
        if (postId == null) {
            return Collections.emptyList();
        }
        try {
            List<Long> tagIds = postTagRepository.getTagIdsByPostId(postId);
            if (tagIds == null || tagIds.isEmpty()) {
                return Collections.emptyList();
            }
            List<Tag> result = new ArrayList<>(tagIds.size());
            for (Long tagId : tagIds) {
                Tag tag = tagRepository.getTagById(tagId);
                if (tag != null) {
                    result.add(tag);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("根据帖子ID获取标签时发生错误, postId: {}", postId, e);
            return Collections.emptyList();
        }
    }

    public void savePostTags(Long postId, List<Long> tagIds) {
        postTagRepository.savePostTag(postId, tagIds);
    }

    public List<Tag> searchTags(String keyword) {
        try {
            List<Tag> result = tagRepository.searchTags(keyword);
            return result != null ? result : Collections.emptyList();
        } catch (Exception e) {
            log.error("搜索标签时发生错误, keyword: {}", keyword, e);
            return Collections.emptyList();
        }
    }

    public List<Tag> getHotTags(Integer limit) {
        try {
            int actualLimit = limit != null ? limit : 10;
            List<Tag> result = tagRepository.getHotTags(actualLimit);
            return result != null ? result : Collections.emptyList();
        } catch (Exception e) {
            log.error("获取热门标签时发生错误, limit: {}", limit, e);
            return Collections.emptyList();
        }
    }

    public List<Tag> getHotTagsByTimeRange(String timeRange, Integer limit) {
        try {
            String actualTimeRange = timeRange != null ? timeRange : "all";
            int actualLimit = limit != null ? limit : 10;
            List<Tag> result = tagRepository.getHotTagsByTimeRange(actualTimeRange, actualLimit);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            log.error("获取热门标签时发生错误, timeRange: {}, limit: {}", timeRange, limit, e);
            return new ArrayList<>();
        }
    }
}