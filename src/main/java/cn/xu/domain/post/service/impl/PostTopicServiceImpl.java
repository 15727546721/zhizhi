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
}