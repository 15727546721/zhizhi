package cn.xu.domain.comment.service.impl;

import cn.xu.api.system.model.vo.comment.CommentReplyVO;
import cn.xu.api.web.model.dto.comment.*;
import cn.xu.api.web.model.vo.comment.CommentVO;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.comment.event.CommentCountEvent;
import cn.xu.domain.comment.event.CommentEvent;
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
import com.lmax.disruptor.RingBuffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommentService implements ICommentService {
    @Resource
    private ICommentRepository commentRepository;
    @Resource
    private IUserService userService;
    @Resource
    private RingBuffer<CommentCountEvent> ringBuffer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveComment(CommentEvent request) {
        try {
            log.info("开始保存评论 - type: {}, targetId: {}, commentId: {}",
                    request.getTargetType(), request.getTargetId(), request.getCommentId());

            // 1. 获取当前用户
            Long currentUserId = userService.getCurrentUserId();
            UserEntity currentUser = userService.getUserById(currentUserId);
            if (currentUser == null) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户信息不存在");
            }

            // 2. 构建评论实体
            CommentEntity commentEntity = CommentEntity.builder()
                    .targetType(request.getTargetType().getValue())
                    .targetId(request.getTargetId())
                    .parentId(request.getCommentId())
                    .userId(currentUserId)
                    .replyUserId(request.getReplyUserId())
                    .content(request.getContent())
                    .build();

            // 3. 验证评论数据

            // 4. 保存评论
            Long commentId = commentRepository.save(commentEntity);
            log.info("保存评论成功 - commentId: {}", commentId);
            commentEntity.setId(commentId);

            // 5. 评论计数
            pushCommentEvent(CommentCountEvent.builder()
                    .targetType(request.getTargetType())
                    .level(request.getCommentId() == null ? 1 : 2)
                    .targetId(request.getTargetId())
                    .count(1)
                    .build());

            // 6. 处理评论并发送消息

            return commentId;
        } catch (BusinessException e) {
            log.error("保存评论失败 - error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("保存评论发生未知错误", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "保存评论失败：" + e.getMessage());
        }
    }

    /**
     * 获取一级评论列表
     */
    private List<CommentEntity> findRootComments(Long targetId, CommentType type) {
        return commentRepository.findRootComments(targetId, type != null ? type.getValue() : null);
    }

    @Override
    public List<CommentEntity> getPagedComments(CommentType type, Long targetId, Integer pageNum, Integer pageSize) {
        try {
            log.info("开始分页查询评论列表 - type: {}, targetId: {}, pageNum: {}, pageSize: {}",
                    type != null ? type.getDescription() : "null", targetId, pageNum, pageSize);

            // 1. 获取分页的一级评论
            List<CommentEntity> rootComments = commentRepository.findRootCommentsByPage(
                    type != null ? type.getValue() : null, targetId, (pageNum - 1) * pageSize, pageSize);

            if (rootComments.isEmpty()) {
                return Collections.emptyList();
            }

            // 2. 获取一级评论的ID列表
            List<Long> rootCommentIds = rootComments.stream()
                    .map(CommentEntity::getId)
                    .collect(Collectors.toList());

            // 3. 批量获取子评论
            List<CommentEntity> replies = commentRepository.findRepliesByParentIds(rootCommentIds);

            // 4. 构建评论树
            Map<Long, List<CommentEntity>> replyMap = replies.stream()
                    .collect(Collectors.groupingBy(CommentEntity::getParentId));

            // 5. 设置子评论并排序
            rootComments.forEach(comment -> {
                List<CommentEntity> commentReplies = replyMap.getOrDefault(comment.getId(), new ArrayList<>());
                commentReplies.sort((c1, c2) -> c1.getCreateTime().compareTo(c2.getCreateTime()));
                comment.setChildren(commentReplies);
            });

            log.info("分页查询评论列表完成 - 一级评论数: {}, 总回复数: {}",
                    rootComments.size(), replies.size());

            return rootComments;

        } catch (Exception e) {
            log.error("分页查询评论列表失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询评论列表失败：" + e.getMessage());
        }
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
        pushCommentEvent(CommentCountEvent.builder()
                .targetType(CommentType.valueOf(comment.getTargetType()))
                .level(comment.getParentId() == null ? 1 : 2)
                .targetId(comment.getTargetId())
                .count(-count)
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

    @Override
    public List<CommentEntity> getCommentList(CommentQueryRequest request) {
        log.info("获取评论列表 - request: {}", request);
        CommentSortType sortType = CommentSortType.valueOf(request.getSortType().toUpperCase());
        // 1. 获取评论列表
        List<CommentEntity> comments = commentRepository.findRootCommentList(request);
        if (comments.isEmpty()) {
            return Collections.emptyList();
        }
        // 2. 获取子评论
        List<Long> commentIds = comments.stream()
                .map(CommentEntity::getId)
                .collect(Collectors.toList());
        List<CommentEntity> replyListByPage = commentRepository.findReplyListByPage(commentIds, sortType, 1, 2);

        // 3. 获取用户信息
        Set<Long> rootCommentUserIds = comments.stream()
                .map(CommentEntity::getUserId)
                .collect(Collectors.toSet());
        Set<Long> replyUserIds1 = replyListByPage.stream()
                .map(CommentEntity::getUserId) // 假设 parentId 是被回复的评论的作者 userId
                .collect(Collectors.toSet());
        Set<Long> replyUserIds2 = replyListByPage.stream()
                .map(CommentEntity::getReplyUserId) // 假设 parentId 是被回复的评论的作者 userId
                .collect(Collectors.toSet());
        rootCommentUserIds.addAll(replyUserIds1);
        rootCommentUserIds.addAll(replyUserIds2);
        Map<Long, UserEntity> userMap = userService.getBatchUserInfo(rootCommentUserIds);

        // 4. 构建评论树并设置用户信息
        comments.forEach(comment -> {
            // 设置根评论的用户信息
            comment.setUser(userMap.get(comment.getUserId()));

            List<CommentEntity> commentReplies = replyListByPage.stream()
                    .filter(reply -> reply.getParentId().equals(comment.getId()))
                    .collect(Collectors.toList());

            // 设置子评论的用户信息及其 replyUserId
            commentReplies.forEach(reply -> {
                reply.setUser(userMap.get(reply.getUserId()));
                reply.setReplyUser(userMap.get(reply.getParentId())); // 假设 parentId 是被回复的评论的作者 userId
            });

            comment.setChildren(commentReplies);
        });

        return comments;
    }

    @Override
    public CommentEntity findCommentWithUserById(Long commentId) {
        CommentEntity byId = commentRepository.findCommentWithUserById(commentId);
        return byId;
    }

    @Override
    public List<FindCommentItemVO> findCommentPageList(FindCommentReq request) {

        Integer targetType = request.getTargetType();
        Long targetId = request.getTargetId();
        Integer pageNo = request.getPageNo();
        Integer pageSize = 10;

        // 1. 获取评论列表
        List<FindCommentItemVO> commentList = commentRepository.findRootCommentWithUser(targetType, targetId, pageNo, pageSize);

        // 2. 获取子评论
        List<Long> commentIds = commentList.stream()
               .map(FindCommentItemVO::getId)
               .collect(Collectors.toList());
        Map<Long, List<FindChildCommentItemVO>> childCommentMap
                = commentRepository.findReplyWithUser(commentIds, 1, 6);

        // 3. 构建评论树
        for (FindCommentItemVO comment : commentList) {
            comment.setReplyComment(childCommentMap.get(comment.getId()));
        }

        return commentList;
    }

    @Override
    public List<FindChildCommentItemVO> findReplyPageList(FindReplyReq request) {
        Long parentId = request.getCommentId();
        Integer pageNo = request.getPageNo();
        Integer pageSize = 6;
        List<FindChildCommentItemVO> replyList
                = commentRepository.findReplyPageWithUser(parentId, pageNo, pageSize);
        return replyList;
    }


    /**
     * 分页获取一级评论列表（包含用户信息）
     *
     * @param request 评论请求参数
     * @return 分页评论列表
     */
    public PageResponse<List<CommentVO>> getRootCommentsWithUserByPage(CommentRequest request) {
        try {
            log.info("分页获取一级评论列表 - request: {}", request);

            // 1. 获取总记录数
            long total = commentRepository.countRootComments(request.getType(), null);

            // 2. 如果没有记录，直接返回空列表
            if (total == 0) {
                return PageResponse.of(request.getPageNo(), request.getPageSize(), 0L, new ArrayList<>());
            }

            // 3. 计算分页参数
            int offset = (request.getPageNo() - 1) * request.getPageSize();
            int limit = request.getPageSize();

            // 4. 获取评论列表
            List<CommentEntity> comments = commentRepository.findRootCommentsByPage(
                    request.getType(),
                    null,
                    offset,
                    limit
            );

            // 5. 获取用户信息
            Set<Long> userIds = comments.stream()
                    .map(CommentEntity::getUserId)
                    .collect(Collectors.toSet());
            Map<Long, UserEntity> userMap = userService.getBatchUserInfo(userIds);

            // 6. 转换为DTO
            List<CommentVO> CommentVOS = comments.stream()
                    .map(comment -> {
                        CommentVO dto = convertToDTO(comment);
                        UserEntity user = userMap.get(comment.getUserId());
                        if (user != null) {
                            dto.setNickname(user.getNickname());
                            dto.setAvatar(user.getAvatar());
                        }
                        return dto;
                    })
                    .collect(Collectors.toList());

            // 7. 构建分页响应
            return PageResponse.of(request.getPageNo(), request.getPageSize(), total, CommentVOS);

        } catch (Exception e) {
            log.error("分页获取一级评论列表失败 - request: {}", request, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取评论列表失败：" + e.getMessage());
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

    /**
     * 将评论实体转换为DTO
     */
    private CommentVO convertToDTO(CommentEntity entity) {
        if (entity == null) {
            return null;
        }
        return CommentVO.builder()
                .id(entity.getId())
                .type(entity.getTargetType())
                .targetId(entity.getTargetId())
                .parentId(entity.getParentId())
                .userId(entity.getUserId())
                .replyUserId(entity.getReplyUserId())
                .content(entity.getContent())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }



    private void pushCommentEvent(CommentCountEvent comment) {
        ringBuffer.publishEvent((event, sequence) -> {
            event.setCommentId(comment.getCommentId());
            event.setTargetType(comment.getTargetType());
            event.setTargetId(comment.getTargetId());
            event.setLevel(comment.getLevel());
            event.setCount(comment.getCount());
        });
    }
}
