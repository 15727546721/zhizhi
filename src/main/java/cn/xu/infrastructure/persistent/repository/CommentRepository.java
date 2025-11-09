package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.repository.ICommentRepository;
import cn.xu.infrastructure.persistent.converter.CommentConverter;
import cn.xu.infrastructure.persistent.dao.CommentMapper;
import cn.xu.infrastructure.persistent.po.Comment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public  class CommentRepository implements ICommentRepository {

    private final CommentMapper commentMapper;
    private final CommentConverter commentConverter;

    @Override
    public Long save(CommentEntity commentEntity) {
        Comment comment = commentConverter.toDataObject(commentEntity);
        if (comment.getId() == null) {
            return commentMapper.saveComment(comment);
        } else {
            // 对于更新操作，我们使用saveComment方法，因为它会处理插入和更新
            return commentMapper.saveComment(comment);
        }
    }

    @Override
    public List<CommentEntity> findCommentBatch(int offset, int batchSize) {
        List<Comment> comments = commentMapper.findCommentsBatch(offset, batchSize);
        return comments.stream()
                .map(commentConverter::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public CommentEntity findById(Long id) {
        Comment comment = commentMapper.selectById(id);
        return comment != null ? commentConverter.toDomainEntity(comment) : null;
    }

    @Override
    public List<CommentEntity> findRootCommentsByHot(int targetType, long targetId, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        List<Comment> comments = commentMapper.findRootCommentsByHot(targetType, targetId, offset, pageSize);
        return comments.stream()
                .map(commentConverter::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentEntity> findRootCommentsByTime(int targetType, long targetId, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        List<Comment> comments = commentMapper.findRootCommentsByTime(targetType, targetId, offset, pageSize);
        return comments.stream()
                .map(commentConverter::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        commentMapper.deleteById(id);
    }

    @Override
    public List<CommentEntity> findByParentId(Long parentId) {
        List<Comment> comments = commentMapper.findByParentId(parentId);
        return comments.stream()
                .map(commentConverter::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void batchDelete(List<Long> commentIds) {
        if (commentIds != null && !commentIds.isEmpty()) {
            commentMapper.batchDelete(commentIds);
        }
    }

    @Override
    public List<CommentEntity> findRootComments(Integer type, Long targetId, int offset, int limit) {
        List<Comment> comments = commentMapper.findRootCommentsByPage(type, targetId, offset, limit);
        return comments.stream()
                .map(commentConverter::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentEntity> findRepliesByPage(Long parentId, int offset, int limit) {
        List<Comment> comments = commentMapper.findRepliesByPage(parentId, offset, limit);
        return comments.stream()
                .map(commentConverter::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public int deleteByParentId(Long parentId) {
        return commentMapper.deleteByParentId(parentId);
    }

    @Override
    public List<CommentEntity> findRepliesByParentIds(List<Long> parentIds) {
        List<Comment> comments = commentMapper.findByParentIds(parentIds);
        return comments.stream()
                .map(commentConverter::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentEntity> findByParentIds(List<Long> parentIds) {
        List<Comment> comments = commentMapper.findByParentIds(parentIds);
        return comments.stream()
                .map(commentConverter::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentEntity> findRepliesByParentIdsByHot(List<Long> parentIds, int size) {
        List<Comment> comments = commentMapper.findRepliesByParentIdsByHot(parentIds, size);
        return comments.stream()
                .map(commentConverter::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentEntity> findRepliesByParentIdsByTime(List<Long> parentIds, int size) {
        List<Comment> comments = commentMapper.findRepliesByParentIdsByTime(parentIds, size);
        return comments.stream()
                .map(commentConverter::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentEntity> findRepliesByParentIdByHot(Long parentId, int page, int size) {
        int offset = (page - 1) * size;
        List<Comment> comments = commentMapper.findRepliesByParentIdByHot(parentId, offset, size);
        return comments.stream()
                .map(commentConverter::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentEntity> findRepliesByParentIdByTime(Long parentId, int page, int size) {
        int offset = (page - 1) * size;
        List<Comment> comments = commentMapper.findRepliesByParentIdByTime(parentId, offset, size);
        return comments.stream()
                .map(commentConverter::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentEntity> findCommentsByIds(List<Long> commentIdList) {
        if (commentIdList == null || commentIdList.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        List<Comment> comments = commentMapper.selectCommentsByIds(commentIdList);
        return comments.stream()
                .map(commentConverter::toDomainEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public Long countByTargetTypeAndTargetId(Integer targetType, Long targetId) {
        if (targetType == null || targetId == null) {
            return 0L;
        }
        Long count = commentMapper.countByTargetTypeAndTargetId(targetType, targetId);
        return count != null ? count : 0L;
    }
    
    @Override
    public java.util.Map<Long, Long> batchCountByTargetIds(Integer targetType, List<Long> targetIds) {
        if (targetType == null || targetIds == null || targetIds.isEmpty()) {
            return new java.util.HashMap<>();
        }
        List<CommentMapper.CommentCountResult> results = commentMapper.batchCountByTargetIds(targetType, targetIds);
        java.util.Map<Long, Long> resultMap = new java.util.HashMap<>();
        for (CommentMapper.CommentCountResult result : results) {
            resultMap.put(result.getTargetId(), result.getCount());
        }
        return resultMap;
    }

    @Override
    public void update(CommentEntity commentEntity) {
        if (commentEntity == null || commentEntity.getId() == null) {
            return;
        }

        Comment comment = commentConverter.toDataObject(commentEntity);
        commentMapper.updateComment(comment);
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
    public List<CommentEntity> findByUserId(Long userId, int offset, int limit) {
        if (userId == null) {
            return java.util.Collections.emptyList();
        }
        List<Comment> comments = commentMapper.findByUserId(userId, offset, limit);
        return comments.stream()
                .map(commentConverter::toDomainEntity)
                .collect(Collectors.toList());
    }
}