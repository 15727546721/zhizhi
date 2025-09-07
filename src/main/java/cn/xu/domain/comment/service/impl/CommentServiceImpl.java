package cn.xu.domain.comment.service.impl;

import cn.xu.api.web.model.dto.comment.*;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.comment.event.CommentCreatedEvent;
import cn.xu.domain.comment.event.CommentDeletedEvent;
import cn.xu.domain.comment.event.CommentEventPublisher;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.model.policy.HotScoreStrategy;
import cn.xu.domain.comment.model.policy.HotScoreStrategyFactory;
import cn.xu.domain.comment.model.valueobject.CommentSortType;
import cn.xu.domain.comment.model.valueobject.CommentType;
import cn.xu.domain.comment.repository.ICommentRepository;
import cn.xu.domain.comment.service.CommentAggregateDomainService;
import cn.xu.domain.comment.service.CommentCacheDomainService;
import cn.xu.domain.comment.service.CommentCreationDomainService;
import cn.xu.domain.comment.service.CommentManagementDomainService;
import cn.xu.domain.comment.service.CommentQueryDomainService;
import cn.xu.domain.comment.service.ICommentService;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import cn.xu.infrastructure.cache.RedisKeyManager;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements ICommentService {

    private final ICommentRepository commentRepository;
    private final CommentEventPublisher commentEventPublisher;
    private final CommentQueryDomainService commentQueryDomainService;
    private final CommentCacheDomainService commentCacheDomainService;
    private final CommentAggregateDomainService commentAggregateDomainService;
    private final CommentManagementDomainService commentManagementDomainService;
    private final CommentCreationDomainService commentCreationDomainService;
    
    @Resource
    private IUserService userService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveComment(CommentCreatedEvent event) {
        return commentCreationDomainService.createComment(event);
    }

    @Override
    public List<CommentEntity> findCommentListWithPreview(FindCommentReq request) {
        if (request == null) {
            return Collections.emptyList();
        }
        
        // 参数校验和排序类型确定
        CommentSortType sortType = commentQueryDomainService.validateAndGetSortType(request.getSortType());
        
        try {
            List<CommentEntity> rootComments = (sortType == CommentSortType.HOT) 
                ? findCommentsByHotSort(request) 
                : findCommentsByTimeSort(request);
            
            if (rootComments.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 获取子评论并组装
            enrichCommentsWithChildren(rootComments, sortType);
            
            // 填充用户信息
            commentAggregateDomainService.fillUserInfo(rootComments);
            
            return rootComments;
        } catch (Exception e) {
            log.error("查询评论列表失败: targetType={}, targetId={}, sortType={}", 
                    request.getTargetType(), request.getTargetId(), sortType, e);
            return Collections.emptyList();
        }
    }

    /**
     * 按时间排序查询评论
     */
    private List<CommentEntity> findCommentsByTimeSort(FindCommentReq request) {
        return commentQueryDomainService.findRootCommentsByTime(
                request.getTargetType(), 
                request.getTargetId(), 
                request.getPageNo(), 
                request.getPageSize());
    }

    /**
     * 按热度排序查询评论
     */
    private List<CommentEntity> findCommentsByHotSort(FindCommentReq request) {
        CommentType commentType = CommentType.valueOf(request.getTargetType());
        int start = (request.getPageNo() - 1) * request.getPageSize();
        int end = start + request.getPageSize() - 1;

        // 检查是否为空结果缓存
        if (commentCacheDomainService.isEmptyResultCached(commentType, request.getTargetId())) {
            return Collections.emptyList();
        }

        // 尝试从缓存获取
        List<Long> cachedIds = commentCacheDomainService.getHotCommentIdsFromCache(
                commentType, request.getTargetId(), start, end);

        if (!cachedIds.isEmpty()) {
            // 缓存命中：查询评论详情并按缓存顺序返回
            List<CommentEntity> comments = commentQueryDomainService.findCommentsByIds(cachedIds);
            List<CommentEntity> sortedComments = commentCacheDomainService.sortCommentsByIds(cachedIds, comments);
            
            // 如果有无效数据，清理缓存
            if (sortedComments.size() < cachedIds.size()) {
                commentCacheDomainService.cleanupInvalidCacheData(
                        commentType, request.getTargetId(), cachedIds, sortedComments);
            }
            
            return sortedComments;
        }

        // 缓存未命中：从数据库查询并更新缓存
        List<CommentEntity> comments = commentQueryDomainService.findRootCommentsByHot(
                request.getTargetType(), request.getTargetId(), 
                request.getPageNo(), request.getPageSize());

        // 异步更新缓存
        commentCacheDomainService.cacheHotCommentRank(commentType, request.getTargetId(), comments);

        return comments;
    }

    /**
     * 为评论添加子评论
     */
    private void enrichCommentsWithChildren(List<CommentEntity> rootComments, CommentSortType sortType) {
        if (rootComments.isEmpty()) {
            return;
        }
        
        List<Long> parentIds = rootComments.stream()
                .map(CommentEntity::getId)
                .collect(Collectors.toList());
        
        List<CommentEntity> childComments = commentQueryDomainService.findChildComments(
                parentIds, sortType, 2);
        
        commentAggregateDomainService.mergeChildren(rootComments, childComments);
    }

    @Override
    public List<CommentEntity> findChildCommentList(FindReplyReq request) {
        List<CommentEntity> replies;
        if (CommentSortType.HOT.name().equalsIgnoreCase(request.getSortType())) {
            replies = commentRepository.findRepliesByParentIdByHot(request.getParentId(), request.getPageNo(), request.getPageSize());
        } else {
            replies = commentRepository.findRepliesByParentIdByTime(request.getParentId(), request.getPageNo(), request.getPageSize());
        }

        commentAggregateDomainService.fillUserInfo(replies);
        return replies;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        commentManagementDomainService.deleteUserComment(commentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCommentByAdmin(Long commentId) {
        commentManagementDomainService.deleteCommentByAdmin(commentId);
    }

    @Override
    public CommentEntity getCommentById(Long commentId) {
        return commentManagementDomainService.validateCommentExists(commentId);
    }

}