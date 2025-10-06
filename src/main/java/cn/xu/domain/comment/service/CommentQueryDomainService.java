package cn.xu.domain.comment.service;

import cn.xu.common.ResponseCode;
import cn.xu.common.exception.BusinessException;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.model.valueobject.CommentSortType;
import cn.xu.domain.comment.repository.ICommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 评论查询领域服务
 * 负责评论查询相关的核心业务逻辑，遵循DDD原则
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentQueryDomainService {

    private final ICommentRepository commentRepository;

    /**
     * 根据热度查询根评论
     */
    public List<CommentEntity> findRootCommentsByHot(Integer targetType, Long targetId, 
                                                     Integer pageNo, Integer pageSize) {
        validateQueryParams(targetType, targetId, pageNo, pageSize);
        
        try {
            return commentRepository.findRootCommentsByHot(targetType, targetId, pageNo, pageSize);
        } catch (Exception e) {
            log.error("查询热门根评论失败: targetType={}, targetId={}", targetType, targetId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询热门评论失败", e);
        }
    }

    /**
     * 根据时间查询根评论
     */
    public List<CommentEntity> findRootCommentsByTime(Integer targetType, Long targetId, 
                                                      Integer pageNo, Integer pageSize) {
        validateQueryParams(targetType, targetId, pageNo, pageSize);
        
        try {
            return commentRepository.findRootCommentsByTime(targetType, targetId, pageNo, pageSize);
        } catch (Exception e) {
            log.error("查询最新根评论失败: targetType={}, targetId={}", targetType, targetId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询最新评论失败", e);
        }
    }

    /**
     * 批量查询评论
     */
    public List<CommentEntity> findCommentsByIds(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            return commentRepository.findCommentsByIds(commentIds);
        } catch (Exception e) {
            log.error("批量查询评论失败: ids={}", commentIds, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "批量查询评论失败", e);
        }
    }

    /**
     * 查询子评论
     */
    public List<CommentEntity> findChildComments(List<Long> parentIds, CommentSortType sortType, int limit) {
        if (parentIds == null || parentIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            return (sortType == CommentSortType.HOT) 
                ? commentRepository.findRepliesByParentIdsByHot(parentIds, limit)
                : commentRepository.findRepliesByParentIdsByTime(parentIds, limit);
        } catch (Exception e) {
            log.error("查询子评论失败: parentIds={}, sortType={}", parentIds, sortType, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询子评论失败", e);
        }
    }

    /**
     * 参数校验
     */
    private void validateQueryParams(Integer targetType, Long targetId, Integer pageNo, Integer pageSize) {
        if (targetType == null) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "评论目标类型不能为空");
        }
        if (targetId == null) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "评论目标ID不能为空");
        }
        if (pageNo == null || pageNo < 1) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "页码必须大于0");
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "每页数量必须在1-100之间");
        }
    }

    /**
     * 验证排序类型
     */
    public CommentSortType validateAndGetSortType(String sortType) {
        if (sortType == null || sortType.trim().isEmpty()) {
            return CommentSortType.HOT; // 默认热门排序
        }
        
        try {
            return CommentSortType.valueOf(sortType.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("不支持的排序类型: {}, 使用默认热门排序", sortType);
            return CommentSortType.HOT;
        }
    }
}