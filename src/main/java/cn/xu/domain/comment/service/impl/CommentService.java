package cn.xu.domain.comment.service.impl;

import cn.xu.api.system.model.vo.comment.CommentReplyVO;
import cn.xu.api.web.model.dto.comment.CommentRequest;
import cn.xu.api.web.model.vo.comment.CommentVO;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.article.service.IArticleService;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.model.valueobject.CommentType;
import cn.xu.domain.comment.repository.ICommentRepository;
import cn.xu.domain.comment.service.ICommentService;
import cn.xu.domain.message.event.BaseMessageEvent;
import cn.xu.domain.message.event.factory.MessageEventFactory;
import cn.xu.domain.message.service.MessagePublisher;
import cn.xu.domain.topic.service.ITopicService;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.common.request.PageRequest;
import cn.xu.infrastructure.common.response.PageResponse;
import cn.xu.infrastructure.persistent.po.Comment;
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
    private IArticleService articleService;

    @Resource
    private ITopicService topicService;

    @Resource
    private MessagePublisher messagePublisher;

    @Resource
    private MessageEventFactory messageEventFactory;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveComment(CommentRequest request) {
        try {
            log.info("开始保存评论 - type: {}, targetId: {}, parentId: {}", 
                    request.getType(), request.getTargetId(), request.getParentId());

            // 1. 获取当前用户
            Long currentUserId = userService.getCurrentUserId();
            UserEntity currentUser = userService.getUserById(currentUserId);
            if (currentUser == null) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户信息不存在");
            }

            // 2. 构建评论实体
            CommentEntity commentEntity = CommentEntity.builder()
                    .type(CommentType.of(request.getType()))
                    .targetId(request.getTargetId())
                    .parentId(request.getParentId())
                    .userId(currentUserId)
                    .replyUserId(request.getReplyUserId())
                    .content(request.getContent())
                    .build();

            // 3. 验证评论数据
            commentEntity.validate();

            // 4. 处理评论并发送消息
            if (request.getParentId() != null) {
                // 回复评论的情况
                handleReplyComment(commentEntity, currentUser);
            } else {
                // 一级评论的情况
                handleRootComment(commentEntity, currentUser);
            }

            // 5. 保存评论
            commentRepository.save(commentEntity);
            log.info("保存评论成功 - commentId: {}", commentEntity.getId());

        } catch (BusinessException e) {
            log.error("保存评论失败 - error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("保存评论发生未知错误", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "保存评论失败：" + e.getMessage());
        }
    }

    /**
     * 处理回复评论
     */
    private void handleReplyComment(CommentEntity comment, UserEntity currentUser) {
        // 1. 获取父评论
        CommentEntity parentComment = getCommentById(comment.getParentId());
        if (parentComment == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "回复的评论不存在");
        }

        // 2. 确保回复的是一级评论
        if (parentComment.getParentId() != null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "只能回复一级评论");
        }

        // 3. 获取被回复用户信息
        UserEntity replyToUser = userService.getUserById(parentComment.getUserId());
        if (replyToUser == null) {
            log.warn("被回复的用户不存在 - userId: {}", parentComment.getUserId());
            return;
        }

        // 4. 发送消息通知被回复的用户
        BaseMessageEvent event = messageEventFactory.createCommentEvent(
                currentUser.getId(),
                replyToUser.getId(),
                String.format("用户 %s 回复了你的评论: %s",
                    currentUser.getNickname(),
                    comment.getContent()),
                comment.getTargetId()
        );
        messagePublisher.publishMessage(event);
    }

    /**
     * 处理一级评论
     * 根据评论类型分别处理文章评论、话题评论等
     */
    private void handleRootComment(CommentEntity comment, UserEntity currentUser) {
        log.debug("[评论服务] 开始处理一级评论 - type: {}, targetId: {}, userId: {}", 
                comment.getType(), comment.getTargetId(), currentUser.getId());

        if (CommentType.ARTICLE.equals(comment.getType())) {
            handleArticleComment(comment, currentUser);
        } else if (CommentType.TOPIC.equals(comment.getType())) {
            handleTopicComment(comment, currentUser);
        } else {
            log.warn("[评论服务] 未知的评论类型 - type: {}", comment.getType());
        }
    }

    /**
     * 处理文章评论
     */
    private void handleArticleComment(CommentEntity comment, UserEntity currentUser) {
        // 1. 获取文章作者ID
        Long authorId = articleService.getArticleAuthorId(comment.getTargetId());
        if (authorId == null) {
            log.warn("[评论服务] 文章不存在或无法获取作者信息 - articleId: {}", comment.getTargetId());
            return;
        }

        // 2. 获取文章作者信息
        UserEntity author = userService.getUserById(authorId);
        if (author == null) {
            log.warn("[评论服务] 文章作者信息不存在 - authorId: {}", authorId);
            return;
        }

        // 3. 如果评论者不是作者本人，才发送消息通知
        if (!authorId.equals(currentUser.getId())) {
            sendCommentNotification(currentUser, author, comment);
        } else {
            log.debug("[评论服务] 作者评论自己的文章，不发送通知 - authorId: {}", authorId);
        }
    }

    /**
     * 处理话题评论
     */
    private void handleTopicComment(CommentEntity comment, UserEntity currentUser) {
        // 1. 获取话题作者ID
        Long authorId = topicService.getTopicAuthorId(comment.getTargetId());
        if (authorId == null) {
            log.warn("[评论服务] 话题不存在或无法获取作者信息 - topicId: {}", comment.getTargetId());
            return;
        }

        // 2. 获取话题作者信息
        UserEntity author = userService.getUserById(authorId);
        if (author == null) {
            log.warn("[评论服务] 话题作者信息不存在 - authorId: {}", authorId);
            return;
        }

        // 3. 如果评论者不是作者本人，才发送消息通知
        if (!authorId.equals(currentUser.getId())) {
            sendCommentNotification(currentUser, author, comment);
        } else {
            log.debug("[评论服务] 作者评论自己的话题，不发送通知 - authorId: {}", authorId);
        }
    }

    /**
     * 发送评论通知
     */
    private void sendCommentNotification(UserEntity fromUser, UserEntity toUser, CommentEntity comment) {
        String content = String.format("用户 %s 评论了你的%s: %s",
                fromUser.getNickname(),
                getCommentTargetType(comment.getType()),
                comment.getContent().length() > 50 ? comment.getContent().substring(0, 50) + "..." : comment.getContent());

        BaseMessageEvent event = messageEventFactory.createCommentEvent(
                fromUser.getId(),
                toUser.getId(),
                content,
                comment.getTargetId()
        );
        messagePublisher.publishMessage(event);
        log.debug("[评论服务] 已发送评论通知 - from: {}, to: {}", fromUser.getId(), toUser.getId());
    }

    /**
     * 获取评论目标类型的描述
     */
    private String getCommentTargetType(CommentType type) {
        if (type == null) {
            return "内容";
        }
        switch (type) {
            case ARTICLE:
                return "文章";
            case TOPIC:
                return "话题";
            default:
                return "内容";
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
        if (comment.getParentId() == null) {
            // 删除一级评论及其所有回复
            commentRepository.deleteByParentId(comment.getId());
            log.info("删除子评论成功 - parentId: {}", comment.getId());

            commentRepository.deleteById(comment.getId());
            log.info("删除一级评论成功 - commentId: {}", comment.getId());
            log.info("删除一级评论及其回复成功 - commentId: {}", comment.getId());
        } else {
            // 删除单条回复
            commentRepository.deleteById(comment.getId());
            log.info("删除回复评论成功 - commentId: {}", comment.getId());
        }
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
            List<CommentVO> commentVOS = comments.stream()
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
            return PageResponse.of(request.getPageNo(), request.getPageSize(), total, commentVOS);
            
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
                .type(entity.getType().getValue())
                .targetId(entity.getTargetId())
                .parentId(entity.getParentId())
                .userId(entity.getUserId())
                .replyUserId(entity.getReplyUserId())
                .content(entity.getContent())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
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
                .type(entity.getType() != null ? entity.getType().getValue() : null)
                .targetId(entity.getTargetId())
                .parentId(entity.getParentId())
                .userId(entity.getUserId())
                .replyUserId(entity.getReplyUserId())
                .content(entity.getContent())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
