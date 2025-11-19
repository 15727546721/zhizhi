package cn.xu.domain.post.service;

import cn.xu.api.web.model.dto.post.PostPageQueryRequest;
import cn.xu.common.exception.BusinessException;
import cn.xu.domain.post.model.aggregate.PostAggregate;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.valobj.PostType;
import cn.xu.domain.post.repository.IPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
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

    /**
     * 综合查询帖子
     * @param request 查询请求
     * @return 帖子列表
     */
    public List<PostEntity> queryPosts(PostPageQueryRequest request) {
        if (request == null) {
            return Collections.emptyList();
        }
        
        int pageNo = Math.max(0, request.getPageNo());
        int pageSize = Math.max(1, request.getPageSize());
        int offset = pageNo * pageSize;
        
        // 1. 如果有分类ID，优先按分类查询
        if (request.getCategoryId() != null) {
            return postRepository.findByCategoryId(request.getCategoryId(), offset, pageSize);
        }
        
        // 2. 如果有话题ID，按话题查询
        if (request.getTopicId() != null) {
            return postRepository.findPostsByTopicId(request.getTopicId(), offset, pageSize);
        }
        
        // 3. 如果有类型，按类型查询
        if (request.getType() != null && !request.getType().isEmpty()) {
            try {
                PostType postType = PostType.valueOf(request.getType());
                return postRepository.findByType(postType, offset, pageSize);
            } catch (IllegalArgumentException e) {
                log.warn("无效的帖子类型: {}", request.getType());
                return Collections.emptyList();
            }
        }
        
        // 4. 默认查询（支持排序）
        return postRepository.findAllWithSort(offset, pageSize, request.getSortBy());
    }
}