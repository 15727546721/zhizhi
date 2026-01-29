package cn.xu.repository.impl;

import cn.xu.model.dto.post.PostTagRelation;
import cn.xu.model.dto.post.TagStatistics;
import cn.xu.model.entity.Post;
import cn.xu.model.entity.PostTag;
import cn.xu.repository.PostTagRepository;
import cn.xu.repository.mapper.PostMapper;
import cn.xu.repository.mapper.PostTagMapper;
import cn.xu.repository.mapper.TagMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import jakarta.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 帖子标签仓储实现
 * <p>负责帖子与标签关联关系的持久化操作</p>

 */
@Slf4j
@Repository
public class PostTagRepositoryImpl implements PostTagRepository {

    @Resource
    private PostTagMapper postTagMapper;
    
    @Resource
    private PostMapper postMapper;
    
    @Resource
    private TagMapper tagMapper;

    @Override
    public void savePostTag(Long postId, List<Long> tagIds) {
        log.info("[帖子标签] 保存帖子标签 - postId: {}, tagIds: {}", postId, tagIds);
        
        if (tagIds == null || tagIds.isEmpty()) {
            log.warn("[帖子标签] 标签列表为空，不保存");
            return;
        }
        
        // 1. 先删除旧的关联（如果存在）
        postTagMapper.deleteByPostId(postId);
        
        // 2. 插入新的关联
        List<PostTag> postTags = new LinkedList<>();
        for (Long tagId : tagIds) {
            postTags.add(PostTag.builder().postId(postId).tagId(tagId).build());
        }
        postTagMapper.insertBatchByList(postTags);
        
        // 3. 更新标签使用次数
        tagMapper.incrementUsageCountBatch(tagIds);
        log.info("[帖子标签] 更新标签使用次数 - tagIds: {}", tagIds);
    }

    @Override
    public List<Long> getTagIdsByPostId(Long postId) {
        if (postId == null) {
            return new LinkedList<>();
        }
        return postTagMapper.selectTagIdsByPostId(postId);
    }

    @Override
    public List<Post> getPostsByTagId(Long tagId, int offset, int limit) {
        // 根据标签ID获取帖子列表
        if (tagId == null) {
            return new LinkedList<>();
        }
        // 获取帖子ID列表
        List<Long> postIds = postTagMapper.selectPostIdsByTagId(tagId, offset, limit);
        // 如果没有关联的帖子，直接返回空列表
        if (postIds == null || postIds.isEmpty()) {
            return new LinkedList<>();
        }
        // 直接使用PostMapper查询帖子信息并返回
        return postMapper.findPostsByIds(postIds);
    }

    @Override
    public List<PostTagRelation> batchGetTagIdsByPostIds(List<Long> postIds) {
        // 批量获取帖子的标签ID列表
        if (postIds == null || postIds.isEmpty()) {
            return new LinkedList<>();
        }
        List<PostTag> postTags = postTagMapper.selectByPostIds(postIds);
        
        // 按postId分组标签ID
        Map<Long, List<Long>> postIdToTagIdsMap = postTags.stream()
                .collect(Collectors.groupingBy(
                        PostTag::getPostId,
                        Collectors.mapping(PostTag::getTagId, Collectors.toList())
                ));
        
        // 构建PostTagRelation列表
        return postIdToTagIdsMap.entrySet().stream()
                .map(entry -> new PostTagRelation(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public void deletePostTags(Long postId) {
        log.info("[帖子标签] 删除帖子标签 - postId: {}", postId);
        postTagMapper.deleteByPostId(postId);
    }

    @Override
    public List<TagStatistics> getHotTags(int limit) {
        // 获取热门标签列表
        return postTagMapper.selectHotTags(limit);
    }

    @Override
    public List<TagStatistics> getRecommendedTags() {
        // 获取推荐标签列表（简单实现：返回前10个热门标签）
        return getHotTags(10);
    }
}
