package cn.xu.domain.comment.service.impl;

import cn.xu.api.system.model.vo.comment.CommentReplyVO;
import cn.xu.api.web.model.dto.comment.*;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.comment.event.CommentCreatedEvent;
import cn.xu.domain.comment.event.CommentDeletedEvent;
import cn.xu.domain.comment.event.CommentEventPublisher;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.model.valueobject.CommentSortType;
import cn.xu.domain.comment.model.valueobject.CommentType;
import cn.xu.domain.comment.repository.ICommentRepository;
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
    @Resource
    private IUserService userService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveComment(CommentCreatedEvent event) {
        Long currentUserId = userService.getCurrentUserId();
        UserEntity currentUser = userService.getUserById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException("用户不存在");
        }

        CommentEntity comment = CommentEntity.builder()
                .targetType(event.getTargetType())
                .targetId(event.getTargetId())
                .parentId(event.getParentId())
                .userId(currentUserId)
                .replyUserId(event.getReplyUserId())
                .content(event.getContent())
                .likeCount(0L)
                .replyCount(0L)
                .hotScore(0L)
                .isHot(false)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        Long commentId = commentRepository.save(comment);
        comment.setId(commentId);
        commentRepository.saveCommentImages(commentId, event.getImageUrls());

        // 更新 Redis 评论计数
        if (comment.getParentId() != null) {
            redisTemplate.opsForValue().increment(RedisKeyManager.commentCountKey(CommentType.COMMENT.getValue(), event.getCommentId()), 1);
        }
        redisTemplate.opsForValue().increment(RedisKeyManager.commentCountKey(event.getTargetType(), event.getTargetId()), 1);

        event.setCommentId(commentId);
        commentEventPublisher.publishCommentCreatedEvent(event);
        return commentId;
    }

    @Override
    public List<CommentEntity> findCommentListWithPreview(FindCommentReq request) {
        if (request == null) return Collections.emptyList();
        if (StringUtils.isBlank(request.getSortType())) {
            request.setSortType(CommentSortType.HOT.name());
        }

        CommentSortType sortType = CommentSortType.valueOf(request.getSortType().toUpperCase());
        return (sortType == CommentSortType.HOT) ? sortByHot(request) : sortByTime(request);
    }

    private List<CommentEntity> sortByTime(FindCommentReq request) {
        List<CommentEntity> rootComments = commentRepository.findRootCommentsByTime(
                request.getTargetType(), request.getTargetId(), request.getPageNo(), request.getPageSize());

        if (rootComments.isEmpty()) return Collections.emptyList();

        List<Long> parentIds = rootComments.stream().map(CommentEntity::getId).collect(Collectors.toList());
        List<CommentEntity> childComments = commentRepository.findRepliesByParentIdsByTime(parentIds, 2);

        mergeChildren(rootComments, childComments);
        fillUserInfo(rootComments);
        return rootComments;
    }

    private List<CommentEntity> sortByHot(FindCommentReq request) {
        String redisKey = RedisKeyManager.commentHotRankKey(CommentType.valueOf(request.getTargetType()), request.getTargetId());
        int start = (request.getPageNo() - 1) * request.getPageSize();
        int end = start + request.getPageSize() - 1;

        Set<Object> commentIds = redisTemplate.opsForZSet().reverseRange(redisKey, start, end);
        List<CommentEntity> rootComments;

        if (commentIds != null && !commentIds.isEmpty()) {
            List<Long> ids = commentIds.stream().map(id -> Long.parseLong(id.toString())).collect(Collectors.toList());
            Map<Long, CommentEntity> map = commentRepository.findCommentsByIds(ids)
                    .stream().collect(Collectors.toMap(CommentEntity::getId, Function.identity()));
            rootComments = ids.stream().map(map::get).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            rootComments = commentRepository.findRootCommentsByHot(request.getTargetType(), request.getTargetId(), request.getPageNo(), request.getPageSize());

            // 写入 Redis 缓存
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (CommentEntity comment : rootComments) {
                    connection.zAdd(redisKey.getBytes(), comment.getHotScore(), comment.getId().toString().getBytes());
                }
                connection.expire(redisKey.getBytes(), 60); // 缓存 60 秒
                return null;
            });
        }

        if (rootComments.isEmpty()) return Collections.emptyList();

        List<Long> parentIds = rootComments.stream().map(CommentEntity::getId).collect(Collectors.toList());
        List<CommentEntity> childComments = commentRepository.findRepliesByParentIdsByHot(parentIds, 2);

        mergeChildren(rootComments, childComments);
        fillUserInfo(rootComments);
        return rootComments;
    }

    @Override
    public List<CommentEntity> findChildCommentList(FindReplyReq request) {
        List<CommentEntity> replies;
        if (CommentSortType.HOT.name().equalsIgnoreCase(request.getSortType())) {
            replies = commentRepository.findRepliesByParentIdByHot(request.getParentId(), request.getPageNo(), request.getPageSize());
        } else {
            replies = commentRepository.findRepliesByParentIdByTime(request.getParentId(), request.getPageNo(), request.getPageSize());
        }

        fillUserInfo(replies);
        return replies;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        CommentEntity comment = validateCommentExists(commentId);
        validateDeletePermission(comment);
        deleteCommentInternal(comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCommentByAdmin(Long commentId) {
        CommentEntity comment = validateCommentExists(commentId);
        deleteCommentInternal(comment);
    }

    @Override
    public CommentEntity getCommentById(Long commentId) {
        return commentRepository.findById(commentId);
    }

    private void deleteCommentInternal(CommentEntity comment) {
        int deletedCount = 1;
        if (comment.getParentId() == null) {
            deletedCount += commentRepository.deleteByParentId(comment.getId());
        }
        commentRepository.deleteById(comment.getId());

        commentEventPublisher.publishCommentDeletedEvent(CommentDeletedEvent.builder()
                .commentId(comment.getId())
                .targetType(comment.getTargetType())
                .targetId(comment.getTargetId())
                .isRootComment(comment.getParentId() == null)
                .build());

        log.info("评论删除成功 - commentId: {}, 删除数量: {}", comment.getId(), deletedCount);
    }

    private void mergeChildren(List<CommentEntity> parents, List<CommentEntity> children) {
        Map<Long, List<CommentEntity>> childMap = children.stream().collect(Collectors.groupingBy(CommentEntity::getParentId));
        parents.forEach(parent -> parent.setChildren(childMap.getOrDefault(parent.getId(), Collections.emptyList())));
    }

    private void fillUserInfo(List<CommentEntity> comments) {
        Set<Long> userIds = new HashSet<>();
        comments.forEach(comment -> collectUserIdsRecursive(comment, userIds));
        Map<Long, UserEntity> userMap = userService.getBatchUserInfo(userIds);
        comments.forEach(comment -> fillUserInfoRecursive(comment, userMap));
    }

    private void collectUserIdsRecursive(CommentEntity comment, Set<Long> userIds) {
        if (comment.getUserId() != null) userIds.add(comment.getUserId());
        if (comment.getReplyUserId() != null) userIds.add(comment.getReplyUserId());
        if (comment.getChildren() != null) {
            comment.getChildren().forEach(child -> collectUserIdsRecursive(child, userIds));
        }
    }

    private void fillUserInfoRecursive(CommentEntity comment, Map<Long, UserEntity> userMap) {
        comment.setUser(userMap.get(comment.getUserId()));
        comment.setReplyUser(userMap.get(comment.getReplyUserId()));
        if (comment.getChildren() != null) {
            comment.getChildren().forEach(child -> fillUserInfoRecursive(child, userMap));
        }
    }

    private CommentEntity validateCommentExists(Long commentId) {
        CommentEntity comment = commentRepository.findById(commentId);
        if (comment == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "评论不存在");
        }
        return comment;
    }

    private void validateDeletePermission(CommentEntity comment) {
        Long currentUserId = userService.getCurrentUserId();
        if (!comment.getUserId().equals(currentUserId)) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "只能删除自己发布的评论");
        }
    }
}
