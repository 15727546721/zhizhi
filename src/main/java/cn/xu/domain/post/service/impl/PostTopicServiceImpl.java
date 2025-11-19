package cn.xu.domain.post.service.impl;

import cn.xu.domain.post.repository.IPostTopicRepository;
import cn.xu.domain.post.service.IPostTopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 帖子话题服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostTopicServiceImpl implements IPostTopicService {
    
    private final IPostTopicRepository postTopicRepository;
    
    @Override
    public void savePostTopics(Long postId, List<Long> topicIds) {
        // 实现保存帖子话题关联关系逻辑
        try {
            postTopicRepository.savePostTopic(postId, topicIds);
        } catch (Exception e) {
            log.error("保存帖子话题关联关系失败, postId: {}, topicIds: {}", postId, topicIds, e);
            throw e;
        }
    }
    
    @Override
    public List<Long> getTopicsByPostId(Long postId) {
        // 实现根据帖子ID获取话题列表逻辑
        try {
            return postTopicRepository.getTopicIdsByPostId(postId);
        } catch (Exception e) {
            log.error("根据帖子ID获取话题列表失败, postId: {}", postId, e);
            return java.util.Collections.emptyList();
        }
    }
    
    @Override
    public List<Long> getPostIdsByTopicId(Long topicId, int offset, int limit) {
        try {
            return postTopicRepository.getPostIdsByTopicId(topicId, offset, limit);
        } catch (Exception e) {
            log.error("根据话题ID获取帖子ID列表失败, topicId: {}", topicId, e);
            return java.util.Collections.emptyList();
        }
    }

    @Override
    public Long countPostsByTopicId(Long topicId) {
        try {
            return postTopicRepository.countPostsByTopicId(topicId);
        } catch (Exception e) {
            log.error("统计话题下帖子数量失败, topicId: {}", topicId, e);
            return 0L;
        }
    }

    @Override
    public List<PostTopicRelation> batchGetTopicIdsByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        List<IPostTopicRepository.PostTopicRelation> relations = postTopicRepository.batchGetTopicIdsByPostIds(postIds);
        
        return relations.stream()
                .map(r -> new PostTopicRelation(r.getPostId(), r.getTopicIds()))
                .collect(java.util.stream.Collectors.toList());
    }
}