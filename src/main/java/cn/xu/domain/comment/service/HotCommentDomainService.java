package cn.xu.domain.comment.service;

import cn.xu.application.common.ResponseCode;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.model.valueobject.CommentType;
import cn.xu.domain.comment.repository.ICommentRepository;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 热点评论领域服务
 * 负责处理热点评论相关的业务逻辑，遵循DDD原则
 * 
 * @author Lily
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HotCommentDomainService {

    private final ICommentRepository commentRepository;
    private final CommentCacheDomainService commentCacheDomainService;
    private final HotScoreService hotScoreService;

    /**
     * 获取指定目标的热门评论
     * 
     * @param targetType 评论目标类型
     * @param targetId 目标ID
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 热门评论列表
     */
    public List<CommentEntity> getHotComments(Integer targetType, Long targetId, int pageNo, int pageSize) {
        // 参数校验
        validateParams(targetType, targetId, pageNo, pageSize);
        
        try {
            CommentType commentType = CommentType.valueOf(targetType);
            
            // 先尝试从缓存获取
            List<Long> cachedIds = getCachedHotCommentIds(commentType, targetId, pageNo, pageSize);
            
            if (!cachedIds.isEmpty()) {
                // 缓存命中，直接返回缓存中的评论
                return getCommentsFromCache(cachedIds, commentType, targetId);
            } else {
                // 缓存未命中，从数据库查询并更新缓存
                return getCommentsFromDatabase(commentType, targetId, pageNo, pageSize);
            }
        } catch (Exception e) {
            log.error("获取热门评论失败: targetType={}, targetId={}, pageNo={}, pageSize={}", 
                     targetType, targetId, pageNo, pageSize, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取热门评论失败", e);
        }
    }

    /**
     * 刷新指定目标的热门评论缓存
     * 
     * @param targetType 评论目标类型
     * @param targetId 目标ID
     */
    public void refreshHotCommentCache(Integer targetType, Long targetId) {
        try {
            CommentType commentType = CommentType.valueOf(targetType);
            
            // 从数据库查询最新的热门评论
            List<CommentEntity> hotComments = commentRepository.findRootCommentsByHot(
                targetType, targetId, 1, 100); // 获取前100个热门评论
            
            // 更新缓存
            commentCacheDomainService.cacheHotCommentRank(commentType, targetId, hotComments);
            
            log.info("刷新热门评论缓存成功: targetType={}, targetId={}, count={}", 
                    targetType, targetId, hotComments.size());
        } catch (Exception e) {
            log.error("刷新热门评论缓存失败: targetType={}, targetId={}", targetType, targetId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "刷新热门评论缓存失败", e);
        }
    }

    /**
     * 获取缓存中的热门评论ID列表
     */
    private List<Long> getCachedHotCommentIds(CommentType commentType, Long targetId, int pageNo, int pageSize) {
        int start = (pageNo - 1) * pageSize;
        int end = start + pageSize - 1;
        
        // 检查是否为空结果缓存
        if (commentCacheDomainService.isEmptyResultCached(commentType, targetId)) {
            return java.util.Collections.emptyList();
        }
        
        return commentCacheDomainService.getHotCommentIdsFromCache(commentType, targetId, start, end);
    }

    /**
     * 从缓存中获取评论详情
     */
    private List<CommentEntity> getCommentsFromCache(List<Long> cachedIds, CommentType commentType, Long targetId) {
        // 查询评论详情
        List<CommentEntity> comments = commentRepository.findCommentsByIds(cachedIds);
        
        // 按缓存顺序排列
        List<CommentEntity> sortedComments = commentCacheDomainService.sortCommentsByIds(cachedIds, comments);
        
        // 如果有无效数据，清理缓存
        if (sortedComments.size() < cachedIds.size()) {
            commentCacheDomainService.cleanupInvalidCacheData(commentType, targetId, cachedIds, sortedComments);
        }
        
        return sortedComments;
    }

    /**
     * 从数据库获取评论并更新缓存
     */
    private List<CommentEntity> getCommentsFromDatabase(CommentType commentType, Long targetId, int pageNo, int pageSize) {
        // 从数据库查询热门评论
        List<CommentEntity> comments = commentRepository.findRootCommentsByHot(
            commentType.getValue(), targetId, pageNo, pageSize);
        
        // 更新缓存
        commentCacheDomainService.cacheHotCommentRank(commentType, targetId, comments);
        
        return comments;
    }

    /**
     * 参数校验
     */
    private void validateParams(Integer targetType, Long targetId, int pageNo, int pageSize) {
        if (targetType == null) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "评论目标类型不能为空");
        }
        
        if (targetId == null) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "目标ID不能为空");
        }
        
        if (pageNo < 1) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "页码必须大于0");
        }
        
        if (pageSize < 1 || pageSize > 100) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "每页数量必须在1-100之间");
        }
    }
}