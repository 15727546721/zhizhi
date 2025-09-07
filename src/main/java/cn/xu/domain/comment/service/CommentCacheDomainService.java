package cn.xu.domain.comment.service;

import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.model.valueobject.CommentType;
import cn.xu.infrastructure.cache.CommentCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 评论缓存领域服务
 * 专门处理评论相关的缓存逻辑，遵循DDD原则
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentCacheDomainService {

    private final CommentCacheRepository commentCacheRepository;

    /**
     * 从缓存获取热门评论ID列表
     */
    public List<Long> getHotCommentIdsFromCache(CommentType commentType, Long targetId, 
                                                int start, int end) {
        return commentCacheRepository.getHotCommentIds(commentType, targetId, start, end);
    }

    /**
     * 缓存热门评论排行
     */
    public void cacheHotCommentRank(CommentType commentType, Long targetId, 
                                    List<CommentEntity> comments) {
        if (comments == null || comments.isEmpty()) {
            commentCacheRepository.cacheEmptyResult(commentType, targetId);
            return;
        }

        Map<Long, Double> commentScores = comments.stream()
                .filter(comment -> comment.getId() != null && comment.getHotScore() != 0)
                .collect(Collectors.toMap(
                        CommentEntity::getId, 
                        CommentEntity::getHotScore));
        
        commentCacheRepository.cacheHotCommentRank(commentType, targetId, commentScores);
    }

    /**
     * 从热门评论排行中移除评论
     */
    public void removeCommentFromHotRank(CommentType commentType, Long targetId, Long commentId) {
        commentCacheRepository.removeHotRank(commentType, targetId, commentId);
    }

    /**
     * 缓存空结果，防止缓存穿透
     */
    public void cacheEmptyResult(CommentType commentType, Long targetId) {
        commentCacheRepository.cacheEmptyResult(commentType, targetId);
    }

    /**
     * 检查是否为空结果缓存
     */
    public boolean isEmptyResultCached(CommentType commentType, Long targetId) {
        return commentCacheRepository.isEmptyResultCached(commentType, targetId);
    }

    /**
     * 按顺序排列评论
     */
    public List<CommentEntity> sortCommentsByIds(List<Long> orderedIds, 
                                                  List<CommentEntity> comments) {
        if (orderedIds.isEmpty() || comments.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, CommentEntity> commentMap = comments.stream()
                .collect(Collectors.toMap(CommentEntity::getId, Function.identity()));

        return orderedIds.stream()
                .map(commentMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 清理无效的缓存数据
     */
    public void cleanupInvalidCacheData(CommentType commentType, Long targetId, 
                                        List<Long> allIds, List<CommentEntity> validComments) {
        Set<Long> validIds = validComments.stream()
                .map(CommentEntity::getId)
                .collect(Collectors.toSet());
        
        List<Long> invalidIds = allIds.stream()
                .filter(id -> !validIds.contains(id))
                .collect(Collectors.toList());
        
        if (!invalidIds.isEmpty()) {
            commentCacheRepository.cleanupInvalidCacheData(commentType, targetId, invalidIds);
        }
    }
}