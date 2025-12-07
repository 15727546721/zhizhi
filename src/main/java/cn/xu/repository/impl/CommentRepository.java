package cn.xu.repository.impl;

import cn.xu.model.entity.Comment;
import cn.xu.repository.ICommentRepository;
import cn.xu.repository.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论仓储实现
 * <p>负责评论的持久化操作</p>
 
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CommentRepository implements ICommentRepository {

    private final CommentMapper commentMapper;

    // ==================== 基础CRUD ====================

    @Override
    public Long save(Comment comment) {
        if (comment == null) {
            return null;
        }
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        return commentMapper.saveComment(comment);
    }

    @Override
    public void update(Comment comment) {
        if (comment == null || comment.getId() == null) {
            return;
        }
        comment.setUpdateTime(LocalDateTime.now());
        commentMapper.updateComment(comment);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            return;
        }
        commentMapper.deleteById(id);
    }

    @Override
    public void batchDelete(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return;
        }
        commentMapper.batchDelete(commentIds);
    }

    @Override
    public int deleteByParentId(Long parentId) {
        if (parentId == null) {
            return 0;
        }
        return commentMapper.deleteByParentId(parentId);
    }

    @Override
    public Comment findById(Long id) {
        if (id == null) {
            return null;
        }
        return commentMapper.selectById(id);
    }

    @Override
    public List<Comment> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return commentMapper.selectCommentsByIds(ids);
    }

    // ==================== 根评论查询 ====================

    @Override
    public List<Comment> findRootCommentsByHot(int targetType, long targetId, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        return commentMapper.findRootCommentsByHot(targetType, targetId, offset, pageSize);
    }

    @Override
    public List<Comment> findRootCommentsByTime(int targetType, long targetId, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        return commentMapper.findRootCommentsByTime(targetType, targetId, offset, pageSize);
    }

    @Override
    public List<Comment> findRootComments(Integer type, Long targetId, Long userId, int offset, int limit) {
        return commentMapper.findRootCommentsByPage(type, targetId, userId, offset, limit);
    }

    @Override
    public Long countRootComments(Integer type, Long targetId, Long userId) {
        Long count = commentMapper.countRootComments(type, targetId, userId);
        return count != null ? count : 0L;
    }

    // ==================== 子评论查询 ====================

    @Override
    public List<Comment> findByParentId(Long parentId) {
        if (parentId == null) {
            return new ArrayList<>();
        }
        return commentMapper.findByParentId(parentId);
    }

    @Override
    public List<Comment> findByParentIds(List<Long> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) {
            return new ArrayList<>();
        }
        return commentMapper.findByParentIds(parentIds);
    }

    @Override
    public List<Comment> findRepliesByPage(Long parentId, int offset, int limit) {
        return commentMapper.findRepliesByPage(parentId, offset, limit);
    }

    @Override
    public List<Comment> findRepliesByParentIdsByHot(List<Long> parentIds, int size) {
        return commentMapper.findRepliesByParentIdsByHot(parentIds, size);
    }

    @Override
    public List<Comment> findRepliesByParentIdsByTime(List<Long> parentIds, int size) {
        return commentMapper.findRepliesByParentIdsByTime(parentIds, size);
    }

    @Override
    public List<Comment> findRepliesByParentIdByHot(Long parentId, int page, int size) {
        return commentMapper.findRepliesByParentIdByHot(parentId, (page - 1) * size, size);
    }

    @Override
    public List<Comment> findRepliesByParentIdByTime(Long parentId, int page, int size) {
        return commentMapper.findRepliesByParentIdByTime(parentId, (page - 1) * size, size);
    }

    // ==================== 统计查询 ====================

    @Override
    public Long countByTargetTypeAndTargetId(Integer targetType, Long targetId) {
        if (targetType == null || targetId == null) {
            return 0L;
        }
        Long count = commentMapper.countByTargetTypeAndTargetId(targetType, targetId);
        return count != null ? count : 0L;
    }

    @Override
    public Map<Long, Long> batchCountByTargetIds(Integer targetType, List<Long> targetIds) {
        if (targetType == null || targetIds == null || targetIds.isEmpty()) {
            return new HashMap<>();
        }
        List<CommentMapper.CommentCountResult> results = commentMapper.batchCountByTargetIds(targetType, targetIds);
        Map<Long, Long> resultMap = new HashMap<>();
        for (CommentMapper.CommentCountResult result : results) {
            resultMap.put(result.getTargetId(), result.getCount());
        }
        return resultMap;
    }

    @Override
    public Long countByUserId(Long userId) {
        if (userId == null) {
            return 0L;
        }
        Long count = commentMapper.countByUserId(userId);
        return count != null ? count : 0L;
    }

    @Override
    public Long countByParentId(Long parentId) {
        if (parentId == null) {
            return 0L;
        }
        Long count = commentMapper.countByParentId(parentId);
        return count != null ? count : 0L;
    }

    // ==================== 用户评论查询 ====================

    @Override
    public List<Comment> findByUserId(Long userId, int offset, int limit) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return commentMapper.findByUserId(userId, offset, limit);
    }

    // ==================== 批量查询（ES初始化用） ====================

    @Override
    public List<Comment> findCommentBatch(int offset, int batchSize) {
        List<Comment> allComments = new ArrayList<>();
        while (true) {
            List<Comment> batch = commentMapper.findCommentsBatch(offset, batchSize);
            if (batch.isEmpty()) {
                break;
            }
            allComments.addAll(batch);
            offset += batchSize;
        }
        return allComments;
    }
    
    @Override
    public void incrementReplyCount(Long commentId) {
        if (commentId != null) {
            commentMapper.incrementReplyCount(commentId);
        }
    }
    
    @Override
    public void decrementReplyCount(Long commentId) {
        if (commentId != null) {
            commentMapper.decrementReplyCount(commentId);
        }
    }
    
    @Override
    public void saveImages(Long commentId, List<String> imageUrls) {
        if (commentId == null || imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        for (int i = 0; i < imageUrls.size(); i++) {
            String imageUrl = imageUrls.get(i);
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                commentMapper.saveImages(commentId, imageUrl, i);
            }
        }
    }
    
    // ==================== 管理后台方法 ====================
    
    @Override
    public List<Comment> findAllRootComments(Integer targetType, Long targetId, int offset, int limit) {
        return commentMapper.findAllRootComments(targetType, targetId, offset, limit);
    }
    
    @Override
    public long countAllRootComments(Integer targetType, Long targetId) {
        Long count = commentMapper.countAllRootComments(targetType, targetId);
        return count != null ? count : 0L;
    }
    
    @Override
    public List<Comment> findRepliesByParentIdByHot(Long parentId) {
        return commentMapper.findRepliesByParentIdByHot(parentId, 0, 100);
    }
    
    @Override
    public List<Comment> findRepliesByParentIdByTime(Long parentId) {
        return commentMapper.findRepliesByParentIdByTime(parentId, 0, 100);
    }
}
