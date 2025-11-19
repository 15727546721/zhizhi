package cn.xu.domain.post.service.impl;

import cn.xu.common.exception.BusinessException;
import cn.xu.domain.post.repository.ITopicFollowRepository;
import cn.xu.domain.post.service.ITopicFollowService;
import cn.xu.infrastructure.persistent.po.TopicFollow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TopicFollowServiceImpl implements ITopicFollowService {

    @Resource
    private ITopicFollowRepository topicFollowRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(Long userId, Long topicId) {
        if (isFollowing(userId, topicId)) {
            throw new BusinessException("已关注该话题");
        }
        TopicFollow topicFollow = new TopicFollow();
        topicFollow.setUserId(userId);
        topicFollow.setTopicId(topicId);
        topicFollow.setCreateTime(LocalDateTime.now());
        topicFollowRepository.save(topicFollow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(Long userId, Long topicId) {
        if (!isFollowing(userId, topicId)) {
            throw new BusinessException("未关注该话题");
        }
        topicFollowRepository.remove(userId, topicId);
    }

    @Override
    public boolean isFollowing(Long userId, Long topicId) {
        return topicFollowRepository.findByUserIdAndTopicId(userId, topicId) != null;
    }

    @Override
    public List<Long> getFollowedTopicIds(Long userId) {
        List<TopicFollow> follows = topicFollowRepository.findByUserId(userId);
        if (follows == null || follows.isEmpty()) {
            return Collections.emptyList();
        }
        return follows.stream().map(TopicFollow::getTopicId).collect(Collectors.toList());
    }

    @Override
    public Long getTopicFollowerCount(Long topicId) {
        return topicFollowRepository.countByTopicId(topicId);
    }
}
