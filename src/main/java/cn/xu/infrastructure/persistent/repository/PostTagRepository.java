package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.valobj.PostType;
import cn.xu.domain.post.repository.IPostTagRepository;
import cn.xu.domain.post.service.IPostTagService;
import cn.xu.infrastructure.persistent.dao.PostTagMapper;
import cn.xu.infrastructure.persistent.po.PostTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class PostTagRepository implements IPostTagRepository {

    @Resource
    private PostTagMapper postTagDao;
    
    @Resource
    private PostRepository postRepository;

    @Override
    public void savePostTag(Long postId, List<Long> tagIds) {
        log.info("保存帖子标签 postId: {}, tagIds: {}", postId, tagIds);
        List<PostTag> postTags = new LinkedList<>();
        for (Long tagId : tagIds) {
            postTags.add(PostTag.builder().postId(postId).tagId(tagId).build());
        }
        postTagDao.insertBatchByList(postTags);
    }

    @Override
    public List<Long> getTagIdsByPostId(Long postId) {
        if (postId == null) {
            return new LinkedList<>();
        }
        return postTagDao.selectTagIdsByPostId(postId);
    }

    @Override
    public List<PostEntity> getPostsByTagId(Long tagId, int offset, int limit) {
        // 根据标签ID获取帖子列表的逻辑
        if (tagId == null) {
            return new LinkedList<>();
        }
        // 获取帖子ID列表
        List<Long> postIds = postTagDao.selectPostIdsByTagId(tagId, offset, limit);
        // 根据帖子ID列表获取帖子实体列表
        return postRepository.findPostsByIds(postIds);
    }

    @Override
    public List<IPostTagService.PostTagRelation> batchGetTagIdsByPostIds(List<Long> postIds) {
        // 批量获取帖子的标签ID列表的逻辑
        if (postIds == null || postIds.isEmpty()) {
            return new LinkedList<>();
        }
        List<PostTag> postTags = postTagDao.selectByPostIds(postIds);
        
        // 按postId分组标签ID
        Map<Long, List<Long>> postIdToTagIdsMap = postTags.stream()
                .collect(Collectors.groupingBy(
                        PostTag::getPostId,
                        Collectors.mapping(PostTag::getTagId, Collectors.toList())
                ));
        
        // 构建PostTagRelation列表
        return postIdToTagIdsMap.entrySet().stream()
                .map(entry -> new IPostTagService.PostTagRelation(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public void deletePostTags(Long postId) {
        log.info("删除帖子标签 postId: {}", postId);
        postTagDao.deleteByPostId(postId);
    }

    @Override
    public List<IPostTagService.TagStatistics> getTagStatisticsByPostType(PostType postType, int limit) {
        // 根据帖子类型获取标签统计信息的逻辑
        if (postType == null) {
            return new LinkedList<>();
        }
        List<PostTagMapper.TagStatistics> tagStatistics = postTagDao.selectTagStatisticsByPostType(postType.getCode(), limit);
        return tagStatistics.stream()
                .map(stat -> new IPostTagService.TagStatistics(stat.getTagId(), stat.getTagName(), stat.getUsageCount()))
                .collect(Collectors.toList());
    }

    @Override
    public List<IPostTagService.TagStatistics> getHotTags(int limit) {
        // 获取热门标签列表的逻辑
        List<PostTagMapper.TagStatistics> tagStatistics = postTagDao.selectHotTags(limit);
        return tagStatistics.stream()
                .map(stat -> new IPostTagService.TagStatistics(stat.getTagId(), stat.getTagName(), stat.getUsageCount()))
                .collect(Collectors.toList());
    }

    @Override
    public List<IPostTagService.TagStatistics> getRecommendedTags() {
        // 获取推荐标签列表的逻辑
        // 这里简单实现为获取前10个热门标签作为推荐
        return getHotTags(10);
    }
}