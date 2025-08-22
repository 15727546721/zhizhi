package cn.xu.domain.comment.service.impl;

import cn.xu.api.system.model.vo.comment.CommentReplyVO;
import cn.xu.api.web.model.dto.comment.*;
import cn.xu.api.web.model.vo.comment.CommentSimpleVO;
import cn.xu.api.web.model.vo.comment.CommentVO;
import cn.xu.api.web.model.vo.comment.CommentWithPreviewVO;
import cn.xu.api.web.model.vo.comment.FindCommentItemVO;
import cn.xu.application.common.ResponseCode;
import cn.xu.application.query.comment.dto.CommentCountDTO;
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
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.common.request.PageRequest;
import cn.xu.infrastructure.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements ICommentService {
    private final ICommentRepository commentRepository;
    private final CommentEventPublisher commentEventPublisher;
    @Resource
    private IUserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveComment(CommentCreatedEvent event) {
        try {
            // 1. 获取当前用户
            Long currentUserId = userService.getCurrentUserId();
            UserEntity currentUser = userService.getUserById(currentUserId);
            if (currentUser == null) {
                throw new BusinessException("用户不存在");
            }

            // 2. 构建评论实体
            CommentEntity comment = CommentEntity.builder()
                    .targetType(event.getTargetType())
                    .targetId(event.getTargetId())
                    .parentId(event.getParentId())
                    .userId(currentUserId)
                    .replyUserId(event.getReplyUserId())
                    .content(event.getContent())
                    .likeCount(0L)
                    .replyCount(0L)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .hotScore(0.0)
                    .isHot(false)
                    .build();

            // 3. 保存评论
            Long commentId = commentRepository.save(comment);
            comment.setId(commentId);

            // 4. 保存评论图片
            commentRepository.saveCommentImages(commentId, event.getImageUrls());

            // 5. 发布评论创建事件
            event.setCommentId(commentId);
            commentEventPublisher.publishCommentCreatedEvent(event);

            return commentId;
        } catch (BusinessException e) {
            log.error("保存评论失败 - error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("保存评论发生未知错误", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "保存评论失败：" + e.getMessage());
        }
    }

    @Override
    public List<CommentEntity> findCommentListWithPreview(FindCommentReq request) {
        if (request == null) {
            return Collections.emptyList();
        }
        if (StringUtils.isBlank(request.getSortType())) {
            request.setSortType(CommentSortType.HOT.name());
        }

        if (CommentSortType.HOT.name().equals((request.getSortType().toUpperCase()))) {
            return sortByHot(request);
        } else if (CommentSortType.NEW.name().equals((request.getSortType().toUpperCase()))) {
            return sortByTime(request);
        } else {
            return Collections.emptyList();
        }
    }

    private List<CommentEntity> sortByTime(FindCommentReq request) {
        // 1. 查询一级评论（按时间排序）
        List<CommentEntity> rootComments = commentRepository.findRootCommentsByTime(
                request.getTargetType(), request.getTargetId(), request.getPage(), request.getPageSize());

        if (rootComments.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 获取所有一级评论 ID，批量查询子评论
        List<Long> parentIds = rootComments.stream()
                .map(CommentEntity::getId)
                .collect(Collectors.toList());

        List<CommentEntity> allChildComments = commentRepository.findByParentIds(parentIds);

        // 3. 按 parentId 分组子评论
        Map<Long, List<CommentEntity>> childCommentMap = allChildComments.stream()
                .collect(Collectors.groupingBy(CommentEntity::getParentId));

        // 4. 合并子评论到对应父评论
        rootComments.forEach(parent ->
                parent.setChildren(childCommentMap.getOrDefault(parent.getId(), Collections.emptyList()))
        );

        // 5. 收集所有用户 ID（包括父子评论的用户 & 被回复用户）
        Set<Long> userIds = new HashSet<>();
        rootComments.forEach(comment -> collectUserIdsRecursive(comment, userIds));

        // 6. 批量查询用户信息
        Map<Long, UserEntity> userMap = userService.getBatchUserInfo(userIds);

        // 7. 填充用户信息
        rootComments.forEach(comment -> fillUserInfoRecursive(comment, userMap));

        return rootComments;
    }

    /**
     * 递归收集评论及其子评论中的用户ID（评论作者和回复对象）
     */
    private void collectUserIdsRecursive(CommentEntity comment, Set<Long> userIds) {
        if (comment.getUserId() != null) {
            userIds.add(comment.getUserId());
        }
        if (comment.getReplyUserId() != null) {
            userIds.add(comment.getReplyUserId());
        }
        if (comment.getChildren() != null) {
            comment.getChildren().forEach(child -> collectUserIdsRecursive(child, userIds));
        }
    }

    /**
     * 递归填充评论及其子评论中的用户信息
     */
    private void fillUserInfoRecursive(CommentEntity comment, Map<Long, UserEntity> userMap) {
        comment.setUser(userMap.get(comment.getUserId()));
        comment.setReplyUser(userMap.get(comment.getReplyUserId()));

        if (comment.getChildren() != null) {
            comment.getChildren().forEach(child -> fillUserInfoRecursive(child, userMap));
        }
    }

    private List<CommentEntity> sortByHot(FindCommentReq request) {
        // 1. 查询一级评论（按时间排序）
        List<CommentEntity> rootComments = commentRepository.findRootCommentsByHot(
                request.getTargetType(), request.getTargetId(), request.getPage(), request.getPageSize());

        if (rootComments.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 获取所有一级评论 ID，批量查询子评论
        List<Long> parentIds = rootComments.stream()
                .map(CommentEntity::getId)
                .collect(Collectors.toList());

        List<CommentEntity> allChildComments = commentRepository.findByParentIds(parentIds);

        // 3. 按 parentId 分组子评论
        Map<Long, List<CommentEntity>> childCommentMap = allChildComments.stream()
                .collect(Collectors.groupingBy(CommentEntity::getParentId));

        // 4. 合并子评论到对应父评论
        rootComments.forEach(parent ->
                parent.setChildren(childCommentMap.getOrDefault(parent.getId(), Collections.emptyList()))
        );

        // 5. 收集所有用户 ID（包括父子评论的用户 & 被回复用户）
        Set<Long> userIds = new HashSet<>();
        rootComments.forEach(comment -> collectUserIdsRecursive(comment, userIds));

        // 6. 批量查询用户信息
        Map<Long, UserEntity> userMap = userService.getBatchUserInfo(userIds);

        // 7. 填充用户信息
        rootComments.forEach(comment -> fillUserInfoRecursive(comment, userMap));

        return rootComments;
    }

    /**
     * 普通用户删除评论（需要验证权限）
     *
     * @param commentId 评论ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        try {
            log.info("用户删除评论 - commentId: {}", commentId);

            // 1. 验证评论是否存在
            CommentEntity comment = validateCommentExists(commentId);

            // 2. 验证删除权限
            validateDeletePermission(comment);

            // 3. 执行删除操作
            deleteCommentInternal(comment);

        } catch (BusinessException e) {
            log.error("用户删除评论失败 - commentId: {}, error: {}", commentId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("用户删除评论发生未知错误 - commentId: {}", commentId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除评论失败：" + e.getMessage());
        }
    }

    /**
     * 管理员删除评论（无需验证权限）
     *
     * @param commentId 评论ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteCommentByAdmin(Long commentId) {
        try {
            log.info("管理员删除评论 - commentId: {}", commentId);

            // 1. 验证评论是否存在
            CommentEntity comment = validateCommentExists(commentId);

            // 2. 执行删除操作
            deleteCommentInternal(comment);

        } catch (BusinessException e) {
            log.error("管理员删除评论失败 - commentId: {}, error: {}", commentId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("管理员删除评论发生未知错误 - commentId: {}", commentId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除评论失败：" + e.getMessage());
        }
    }

    /**
     * 验证评论是否存在
     *
     * @param commentId 评论ID
     * @return 评论实体
     * @throws BusinessException 评论不存在时抛出异常
     */
    private CommentEntity validateCommentExists(Long commentId) {
        CommentEntity comment = getCommentById(commentId);
        if (comment == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "评论不存在");
        }
        return comment;
    }

    /**
     * 验证用户是否有权限删除评论
     *
     * @param comment 评论实体
     * @throws BusinessException 无权限时抛出异常
     */
    private void validateDeletePermission(CommentEntity comment) {
        Long currentUserId = userService.getCurrentUserId();
        if (!comment.getUserId().equals(currentUserId)) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "只能删除自己发布的评论");
        }
    }

    /**
     * 内部删除评论方法
     *
     * @param comment 评论实体
     */
    private void deleteCommentInternal(CommentEntity comment) {
        int count = 1;
        if (comment.getParentId() == null) {
            // 删除一级评论及其所有回复
            int i = commentRepository.deleteByParentId(comment.getId());
            commentRepository.deleteById(comment.getId());
            count += i;
        } else {
            // 删除单条回复
            commentRepository.deleteById(comment.getId());
        }
        commentEventPublisher.publishCommentDeletedEvent(CommentDeletedEvent.builder()
                .commentId(comment.getId())
                .targetType(comment.getTargetType())
                .isRootComment(comment.getParentId() == null)
                .targetId(comment.getTargetId())
                .build());
        log.info("删除评论成功 - commentId: {}, count: {}", comment.getId(), count);
    }

    @Override
    public CommentEntity getCommentById(Long commentId) {
        try {
            log.info("获取评论信息 - commentId: {}", commentId);
            CommentEntity comment = commentRepository.findById(commentId);
            if (comment == null) {
                log.warn("评论不存在 - commentId: {}", commentId);
            }
            return comment;
        } catch (Exception e) {
            log.error("获取评论信息失败 - commentId: {}", commentId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取评论信息失败：" + e.getMessage());
        }
    }

    /**
     * 分页获取二级评论列表（包含用户信息）
     *
     * @param parentId    父评论ID
     * @param pageRequest 分页参数
     * @return 评论列表
     */
    public List<CommentReplyVO> getPagedRepliesWithUser(Long parentId, PageRequest pageRequest) {
        try {
            log.info("分页获取二级评论列表 - parentId: {}, pageRequest: {}", parentId, pageRequest);

            // 1. 计算分页参数
            int offset = (pageRequest.getPageNo() - 1) * pageRequest.getPageSize();
            int limit = pageRequest.getPageSize();

            // 2. 获取评论列表
            List<CommentEntity> replies = commentRepository.findRepliesByPage(parentId, offset, limit);
            if (replies.isEmpty()) {
                return new ArrayList<>();
            }

            // 3. 收集所有需要查询的用户ID
            Set<Long> userIds = new HashSet<>();
            replies.forEach(reply -> {
                userIds.add(reply.getUserId());
                if (reply.getReplyUserId() != null) {
                    userIds.add(reply.getReplyUserId());
                }
            });

            // 4. 获取用户信息
            Map<Long, UserEntity> userMap = userService.getBatchUserInfo(userIds);

            // 5. 转换为DTO
            return replies.stream()
                    .map(reply -> {
                        CommentReplyVO dto = CommentReplyVO.builder()
                                .id(reply.getId())
                                .content(reply.getContent())
                                .userId(reply.getUserId())
                                .replyUserId(reply.getReplyUserId())
                                .createTime(reply.getCreateTime())
                                .build();

                        // 设置评论用户信息
                        UserEntity user = userMap.get(reply.getUserId());
                        if (user != null) {
                            dto.setNickName(user.getNickname());
                            dto.setAvatar(user.getAvatar());
                        }

                        // 设置被回复用户信息
                        if (reply.getReplyUserId() != null) {
                            UserEntity replyUser = userMap.get(reply.getReplyUserId());
                            if (replyUser != null) {
                                dto.setReplyNickname(replyUser.getNickname());
                                dto.setReplyAvatar(replyUser.getAvatar());
                            }
                        }

                        return dto;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("分页获取二级评论列表失败 - parentId: {}", parentId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取回复列表失败：" + e.getMessage());
        }
    }
}
