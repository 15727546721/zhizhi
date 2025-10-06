package cn.xu.domain.post.service;

import cn.xu.common.exception.BusinessException;
import cn.xu.domain.post.model.aggregate.PostAggregate;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.repository.IPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 帖子查询领域服务
 * 负责帖子查询相关的核心业务逻辑，遵循DDD原则
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostQueryDomainService {

    private final IPostRepository postRepository;

    /**
     * 根据ID查询帖子
     * @param postId 帖子ID
     * @return 帖子实体
     */
    public PostEntity findPostById(Long postId) {
        if (postId == null) {
            log.warn("查询帖子失败：帖子ID为空");
            throw new IllegalArgumentException("帖子ID不能为空");
        }
        
        Optional<PostAggregate> aggregateOpt = postRepository.findById(postId);
        if (!aggregateOpt.isPresent()) {
            log.warn("帖子不存在 - postId: {}", postId);
            throw new BusinessException("帖子不存在");
        }
        
        PostAggregate aggregate = aggregateOpt.get();
        PostEntity post = aggregate.getPostEntity();
        log.debug("帖子查询成功 - postId: {}, title: {}", postId, post.getTitle());
        return post;
    }
}