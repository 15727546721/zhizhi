package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.post.repository.IPostTopicRepository;
import cn.xu.infrastructure.persistent.dao.PostTopicMapper;
import cn.xu.infrastructure.persistent.po.PostTopic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class PostTopicRepository implements IPostTopicRepository {

    @Resource
    private PostTopicMapper postTopicDao;

    @Override
    public void savePostTopic(Long postId, List<Long> topicIds) {
        log.info("保存帖子话题 postId: {}, topicIds: {}", postId, topicIds);
        if (topicIds == null || topicIds.isEmpty()) {
            return;
        }
        
        List<PostTopic> postTopics = topicIds.stream()
                .map(topicId -> PostTopic.builder().postId(postId).topicId(topicId).build())
                .collect(Collectors.toList());
        postTopicDao.insertBatchByList(postTopics);
    }

    @Override
    public List<Long> getTopicIdsByPostId(Long postId) {
        if (postId == null) {
            return new LinkedList<>();
        }
        return postTopicDao.selectTopicIdsByPostId(postId);
    }

    @Override
    public List<Long> getPostIdsByTopicId(Long topicId, int offset, int limit) {
        if (topicId == null) {
            return new LinkedList<>();
        }
        return postTopicDao.selectPostIdsByTopicId(topicId, offset, limit);
    }

    @Override
    public List<PostTopicRelation> batchGetTopicIdsByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return new LinkedList<>();
        }
        List<PostTopic> postTopics = postTopicDao.selectByPostIds(postIds);
        
        // 按postId分组话题ID
        Map<Long, List<Long>> postIdToTopicIdsMap = postTopics.stream()
                .collect(Collectors.groupingBy(
                        PostTopic::getPostId,
                        Collectors.mapping(PostTopic::getTopicId, Collectors.toList())
                ));
        
        // 构建PostTopicRelation列表
        return postIdToTopicIdsMap.entrySet().stream()
                .map(entry -> new PostTopicRelation(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public void deletePostTopics(Long postId) {
        log.info("删除帖子话题 postId: {}", postId);
        postTopicDao.deleteByPostId(postId);
    }
    
    @Override
    public List<IPostTopicRepository.UserTopicStats> getTopicStatsByUserId(Long userId, int offset, int limit) {
        if (userId == null) {
            return new LinkedList<>();
        }
        List<PostTopicMapper.UserTopicStats> statsList = postTopicDao.selectTopicStatsByUserId(userId, offset, limit);
        return statsList.stream().map(stats -> {
            IPostTopicRepository.UserTopicStats result = new IPostTopicRepository.UserTopicStats();
            result.setTopicId(stats.getTopicId());
            result.setPostCount(stats.getPostCount());
            result.setLastPostTime(stats.getLastPostTime());
            return result;
        }).collect(Collectors.toList());
    }
    
    @Override
    public Long countTopicsByUserId(Long userId) {
        if (userId == null) {
            return 0L;
        }
        Long count = postTopicDao.countTopicsByUserId(userId);
        return count != null ? count : 0L;
    }
}