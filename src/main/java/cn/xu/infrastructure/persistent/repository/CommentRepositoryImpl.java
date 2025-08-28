package cn.xu.infrastructure.persistent.repository;

import cn.xu.api.web.model.dto.comment.FindChildCommentItemVO;
import cn.xu.api.web.model.vo.comment.FindCommentItemVO;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.repository.ICommentRepository;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.persistent.dao.CommentImageMapper;
import cn.xu.infrastructure.persistent.dao.CommentMapper;
import cn.xu.infrastructure.persistent.po.Comment;
import cn.xu.infrastructure.persistent.po.CommentImage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class CommentRepositoryImpl implements ICommentRepository {

    @Resource
    private CommentMapper commentMapper;
    @Resource
    private CommentImageMapper commentImageMapper;

    @Override
    public List<CommentEntity> findCommentBatch(int offset, int batchSize) {

        List<CommentEntity> allComments = new ArrayList<>();

        while (true) {
            List<CommentEntity> batch = commentMapper.findCommentsBatch(offset, batchSize);
            if (batch.isEmpty()) {
                break;
            }
            allComments.addAll(batch);
            offset += batchSize;
        }

        return allComments;
    }

    @Override
    public CommentEntity findById(Long id) {
        try {
//            // 先查询基础评论
//            List<Comment> comments = commentMapper.selectCommentsByIds(ids);
//
//            // 批量查询图片并关联
//            Map<Long, List<CommentImage>> imageMap = commentMapper
//                    .selectImagesByCommentIds(ids)
//                    .stream()
//                    .collect(Collectors.groupingBy(CommentImage::getCommentId));
//
//            comments.forEach(c -> c.setImages(imageMap.get(c.getId())));
//            return comments;
            log.info("查询评论 - id: {}", id);
            Comment comment = commentMapper.selectById(id);
            return convertToEntity(comment);
        } catch (Exception e) {
            log.error("查询评论失败 - id: {}", id, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询评论失败：" + e.getMessage());
        }
    }

    @Override
    public List<CommentEntity> findRootCommentsByHot(int targetType, long targetId, int offset, int pageSize) {
        List<Comment> comments = commentMapper.findRootCommentsByHot(targetType, targetId, offset, pageSize);
        return convertAndAttachImages(comments);
    }

    @Override
    public List<CommentEntity> findRootCommentsByTime(int targetType, long targetId, int offset, int pageSize) {
        List<Comment> comments = commentMapper.findRootCommentsByTime(targetType, targetId, offset, pageSize);
        return convertAndAttachImages(comments);
    }

    @Override
    public void deleteById(Long id) {
        try {
            log.info("删除评论 - id: {}", id);
            commentMapper.deleteById(id);
            log.info("删除评论成功 - id: {}", id);
        } catch (Exception e) {
            log.error("删除评论失败 - id: {}", id, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除评论失败：" + e.getMessage());
        }
    }

    @Override
    public List<CommentEntity> findByParentId(Long parentId) {
        try {
            log.info("查询子评论列表 - parentId: {}", parentId);

            List<Comment> comments = commentMapper.findByParentId(parentId);
            if (comments == null || comments.isEmpty()) {
                return new ArrayList<>();
            }

            List<CommentEntity> commentEntities = comments.stream()
                    .map(this::convertToEntity)
                    .collect(Collectors.toList());

            log.info("查询到子评论数量: {}", commentEntities.size());
            return commentEntities;

        } catch (Exception e) {
            log.error("查询子评论列表失败 - parentId: {}", parentId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询子评论列表失败：" + e.getMessage());
        }
    }

    @Override
    public void batchDelete(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return;
        }
        try {
            log.info("开始批量删除评论 - commentIds: {}", commentIds);
            int deletedCount = commentMapper.batchDelete(commentIds);
            log.info("批量删除评论完成 - 删除数量: {}", deletedCount);

            // 如果删除数量与预期不符，抛出异常
            if (deletedCount != commentIds.size()) {
                log.error("批量删除评论数量不匹配 - 预期: {}, 实际: {}", commentIds.size(), deletedCount);
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除评论数量不匹配");
            }
        } catch (Exception e) {
            log.error("批量删除评论失败 - commentIds: {}", commentIds, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "批量删除评论失败：" + e.getMessage());
        }
    }

    public void deleteByTopicId(Long topicId) {
        try {
            log.info("删除与话题相关的评论 - topicId: {}", topicId);
            commentMapper.deleteByTypeAndTargetId(2, topicId);
        } catch (Exception e) {
            log.error("删除与话题相关的评论失败 - topicId: {}", topicId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除与话题相关的评论失败");
        }
    }

    @Override
    public List<CommentEntity> findRootComments(Integer type, Long targetId, int offset, int limit) {
        try {
            log.info("分页查询一级评论 - type: {}, targetId: {}, offset: {}, limit: {}", type, targetId, offset, limit);
            List<Comment> comments = commentMapper.findRootCommentsByPage(type, targetId, offset, limit);
            if (comments == null || comments.isEmpty()) {
                return Collections.emptyList();
            }
            return comments.stream()
                    .map(this::convertToEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("分页查询一级评论失败 - type: {}, targetId: {}", type, targetId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询评论列表失败：" + e.getMessage());
        }
    }

    @Override
    public List<CommentEntity> findRepliesByPage(Long parentId, int offset, int limit) {
        try {
            log.info("分页查询二级评论列表 - parentId: {}, offset: {}, limit: {}", parentId, offset, limit);
            List<Comment> replies = commentMapper.findRepliesByPage(parentId, offset, limit);
            if (replies == null || replies.isEmpty()) {
                return new ArrayList<>();
            }
            return replies.stream()
                    .map(this::convertToEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("分页查询二级评论列表失败 - parentId: {}", parentId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询回复列表失败：" + e.getMessage());
        }
    }

    /**
     * 将评论实体转换为Comment
     */
    private Comment convertToComment(CommentEntity entity) {
        if (entity == null) {
            return null;
        }
        return Comment.builder()
                .id(entity.getId())
                .targetType(entity.getTargetType())
                .targetId(entity.getTargetId())
                .parentId(entity.getParentId())
                .userId(entity.getUserId())
                .replyUserId(entity.getReplyUserId())
                .content(entity.getContent())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    @Override
    public int deleteByParentId(Long parentId) {
        return commentMapper.deleteByParentId(parentId);
    }

    @Override
    public Long save(CommentEntity commentEntity) {
        try {
//            // 1. 保存评论主体
//            commentMapper.insertComment(comment);
//
//            // 2. 保存关联图片
//            if (comment.getImages() != null && !comment.getImages().isEmpty()) {
//                // 设置commentId关联
//                comment.getImages().forEach(img -> img.setCommentId(comment.getId()));
//                commentMapper.batchInsertImages(comment.getImages());
//            }
//
//            return comment;
            log.info("保存评论 - commentEntity: {}", commentEntity);
            Comment comment = convertToComment(commentEntity);
            comment.setCreateTime(LocalDateTime.now());
            comment.setUpdateTime(LocalDateTime.now());
            commentMapper.insert(comment);
            log.info("保存评论成功 - id: {}", comment.getId());
            return comment.getId();
        } catch (Exception e) {
            log.error("保存评论失败 - commentEntity: {}", commentEntity, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "保存评论失败：" + e.getMessage());
        }
    }

    @Override
    public List<CommentEntity> findRepliesByParentIds(List<Long> parentIds) {
        try {
            log.info("批量查询子评论 - parentIds: {}", parentIds);
            if (parentIds == null || parentIds.isEmpty()) {
                return new ArrayList<>();
            }
            List<Comment> replies = commentMapper.findByParentIds(parentIds);
            List<CommentEntity> replyEntities = replies.stream()
                    .map(this::convertToEntity)
                    .collect(Collectors.toList());
            log.info("批量查询子评论成功 - parentIds: {}, count: {}", parentIds, replyEntities.size());
            return replyEntities;
        } catch (Exception e) {
            log.error("批量查询子评论失败 - parentIds: {}", parentIds, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询回复列表失败：" + e.getMessage());
        }
    }

    @Override
    public List<FindCommentItemVO> findRootCommentWithUser(Integer targetType, Long targetId, Integer pageNo, Integer pageSize) {
        int offset = (pageNo - 1) * pageSize;
        int size = pageSize;
        List<FindCommentItemVO> vo = commentMapper.findRootCommentWithUser(targetType, targetId, offset, size);
        return vo;
    }

    @Override
    public List<FindChildCommentItemVO> findReplyPageWithUser(Long parentId, Integer pageNo, Integer pageSize) {
        int offset = (pageNo - 1) * pageSize;
        int size = pageSize;
        List<FindChildCommentItemVO> childComment = commentMapper.findReplyPageWithUserByParentId(parentId, offset, size);
        return childComment;
    }

    @Override
    public void saveCommentImages(Long commentId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        List<CommentImage> images = imageUrls.stream()
                .map(url -> CommentImage.builder()
                        .commentId(commentId)
                        .imageUrl(url)
                        .build())
                .collect(Collectors.toList());
        commentMapper.batchSaveImages(images);
    }

    @Override
    public List<CommentEntity> findByParentIds(List<Long> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Comment> comments = commentMapper.findByParentIds(parentIds);
        return convertCommentList(comments);
    }

    @Override
    public List<CommentEntity> findRepliesByParentIdsByHot(List<Long> parentIds, int size) {
        List<Comment> repliesByParentIdsByHot = commentMapper.findRepliesByParentIdsByHot(parentIds, size);
        return convertCommentList(repliesByParentIdsByHot);
    }

    @Override
    public List<CommentEntity> findRepliesByParentIdsByTime(List<Long> parentIds, int size) {
        List<Comment> repliesByParentIdsByTime = commentMapper.findRepliesByParentIdsByTime(parentIds, size);
        return convertCommentList(repliesByParentIdsByTime);
    }

    @Override
    public List<CommentEntity> findRepliesByParentIdByHot(Long parentId, int page, int size) {
        List<Comment> repliesByParentIdByHot = commentMapper.findRepliesByParentIdByHot(parentId, (page - 1) * size, size);
        return convertCommentList(repliesByParentIdByHot);
    }

    @Override
    public List<CommentEntity> findRepliesByParentIdByTime(Long parentId, int page, int size) {
        List<Comment> repliesByParentIdByTime = commentMapper.findRepliesByParentIdByTime(parentId, (page - 1) * size, size);
        return convertCommentList(repliesByParentIdByTime);
    }

    private List<CommentEntity> convertCommentList(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }
        List<CommentEntity> result = new ArrayList<>();
        for (Comment comment : comments) {
            CommentEntity commentEntity = convertToEntity(comment);
            result.add(commentEntity);
        }
        return result;
    }

    /**
     * 将PO对象转换为领域实体对象
     */
    private CommentEntity convertToEntity(Comment comment) {
        if (comment == null) {
            return null;
        }
        return CommentEntity.builder()
                .id(comment.getId())
                .userId(comment.getUserId())
                .targetType(comment.getTargetType())
                .targetId(comment.getTargetId())
                .content(comment.getContent())
                .parentId(comment.getParentId())
                .replyUserId(comment.getReplyUserId())
                .likeCount(comment.getLikeCount() != null ? comment.getLikeCount() : 0L)
                .replyCount(comment.getReplyCount() != null ? comment.getReplyCount() : 0L)
                .createTime(comment.getCreateTime())
                .updateTime(comment.getUpdateTime())
                .build();
    }

    private List<CommentEntity> convertAndAttachImages(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return Collections.emptyList();
        }

        return comments.stream()
                .map(comment -> {
                    CommentEntity entity = convertToEntity(comment);
                    List<CommentImage> images = commentImageMapper.selectImagesByCommentId(comment.getId());
                    List<String> imageUrls = images.stream()
                            .map(CommentImage::getImageUrl)
                            .collect(Collectors.toList());
                    entity.setImageUrls(imageUrls);
                    return entity;
                })
                .collect(Collectors.toList());
    }
}
