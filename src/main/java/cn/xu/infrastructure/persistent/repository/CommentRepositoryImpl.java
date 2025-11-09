package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.repository.ICommentRepository;
import cn.xu.infrastructure.persistent.converter.CommentConverter;
import cn.xu.infrastructure.persistent.dao.CommentMapper;
import cn.xu.infrastructure.persistent.po.Comment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements ICommentRepository {

    private final CommentMapper commentMapper;
    private final CommentConverter commentConverter;

    @Override
    public List<CommentEntity> findCommentBatch(int offset, int batchSize) {
        List<CommentEntity> allComments = new ArrayList<>();

        while (true) {
            List<Comment> batch = commentMapper.findCommentsBatch(offset, batchSize);
            if (batch.isEmpty()) {
                break;
            }
            List<CommentEntity> convertedBatch = commentConverter.toDomainEntities(batch);
            allComments.addAll(convertedBatch);
            offset += batchSize;
        }

        return allComments;
    }

    @Override
    public CommentEntity findById(Long id) {
        if (id == null) {
            return null;
        }
        
        Comment comment = commentMapper.selectById(id);
        return commentConverter.toDomainEntity(comment);
    }

    @Override
    public List<CommentEntity> findRootCommentsByHot(int targetType, long targetId, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        List<Comment> comments = commentMapper.findRootCommentsByHot(targetType, targetId, offset, pageSize);
        return commentConverter.toDomainEntities(comments);
    }

    @Override
    public List<CommentEntity> findRootCommentsByTime(int targetType, long targetId, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        List<Comment> comments = commentMapper.findRootCommentsByTime(targetType, targetId, offset, pageSize);
        return commentConverter.toDomainEntities(comments);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            return;
        }
        commentMapper.deleteById(id);
    }

    @Override
    public List<CommentEntity> findByParentId(Long parentId) {
        if (parentId == null) {
            return new ArrayList<>();
        }
        
        List<Comment> comments = commentMapper.findByParentId(parentId);
        return commentConverter.toDomainEntities(comments);
    }

    @Override
    public void batchDelete(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return;
        }
        commentMapper.batchDelete(commentIds);
    }

    @Override
    public List<CommentEntity> findRootComments(Integer type, Long targetId, int offset, int limit) {
        List<Comment> comments = commentMapper.findRootCommentsByPage(type, targetId, offset, limit);
        return commentConverter.toDomainEntities(comments);
    }

    @Override
    public List<CommentEntity> findRepliesByPage(Long parentId, int offset, int limit) {
        List<Comment> replies = commentMapper.findRepliesByPage(parentId, offset, limit);
        return commentConverter.toDomainEntities(replies);
    }

    @Override
    public int deleteByParentId(Long parentId) {
        if (parentId == null) {
            return 0;
        }
        return commentMapper.deleteByParentId(parentId);
    }

    @Override
    public Long save(CommentEntity commentEntity) {
        if (commentEntity == null) {
            return null;
        }

        Comment comment = commentConverter.toDataObject(commentEntity);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());

        return commentMapper.saveComment(comment);
    }

    @Override
    public List<CommentEntity> findRepliesByParentIds(List<Long> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Comment> replies = commentMapper.findByParentIds(parentIds);
        return commentConverter.toDomainEntities(replies);
    }

    @Override
    public List<CommentEntity> findByParentIds(List<Long> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Comment> comments = commentMapper.findByParentIds(parentIds);
        return commentConverter.toDomainEntities(comments);
    }

    @Override
    public List<CommentEntity> findRepliesByParentIdsByHot(List<Long> parentIds, int size) {
        List<Comment> replies = commentMapper.findRepliesByParentIdsByHot(parentIds, size);
        return commentConverter.toDomainEntities(replies);
    }

    @Override
    public List<CommentEntity> findRepliesByParentIdsByTime(List<Long> parentIds, int size) {
        List<Comment> replies = commentMapper.findRepliesByParentIdsByTime(parentIds, size);
        return commentConverter.toDomainEntities(replies);
    }

    @Override
    public List<CommentEntity> findRepliesByParentIdByHot(Long parentId, int page, int size) {
        List<Comment> replies = commentMapper.findRepliesByParentIdByHot(parentId, (page - 1) * size, size);
        return commentConverter.toDomainEntities(replies);
    }

    @Override
    public List<CommentEntity> findRepliesByParentIdByTime(Long parentId, int page, int size) {
        List<Comment> replies = commentMapper.findRepliesByParentIdByTime(parentId, (page - 1) * size, size);
        return commentConverter.toDomainEntities(replies);
    }

    @Override
    public List<CommentEntity> findCommentsByIds(List<Long> commentIdList) {
        if (commentIdList == null || commentIdList.isEmpty()) {
            return new ArrayList<>();
        }
        List<Comment> comments = commentMapper.selectCommentsByIds(commentIdList);
        return commentConverter.toDomainEntities(comments);
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
    public List<CommentEntity> findByUserId(Long userId, int offset, int limit) {
        if (userId == null) {
            return new ArrayList<>();
        }
        List<Comment> comments = commentMapper.findByUserId(userId, offset, limit);
        return commentConverter.toDomainEntities(comments);
    }

    @Override
    public void update(CommentEntity commentEntity) {
        if (commentEntity == null || commentEntity.getId() == null) {
            return;
        }

        Comment comment = commentConverter.toDataObject(commentEntity);
        comment.setUpdateTime(LocalDateTime.now());
        commentMapper.updateComment(comment);
    }
}
