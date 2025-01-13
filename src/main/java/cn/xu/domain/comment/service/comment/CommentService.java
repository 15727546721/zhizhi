package cn.xu.domain.comment.service.comment;

import cn.xu.api.controller.web.comment.request.CommentRequest;
import cn.xu.api.dto.comment.CommentDTO;
import cn.xu.api.dto.comment.CommentReplyDTO;
import cn.xu.api.dto.common.PageRequest;
import cn.xu.api.dto.common.PageResponse;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.model.valueobject.CommentType;
import cn.xu.domain.comment.repository.ICommentRepository;
import cn.xu.domain.comment.service.ICommentService;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import cn.xu.exception.BusinessException;
import cn.xu.infrastructure.common.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;

@Slf4j
@Service
public class CommentService implements ICommentService {

    @Resource
    private ICommentRepository commentRepository;

    @Resource
    private IUserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addComment(CommentRequest request) {
        CommentEntity commentEntity = convertToEntity(request);
        commentEntity.validate();
        commentRepository.addComment(commentEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyComment(CommentRequest request) {
        // 检查父评论是否存在
        CommentEntity parentComment = commentRepository.findById(request.getParentId());
        if (parentComment == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "回复的评论不存在");
        }

        CommentEntity commentEntity = convertToEntity(request);
        commentEntity.validate();
        commentRepository.replyComment(commentEntity);
    }

    @Override
    public List<CommentEntity> getCommentsByTypeAndTargetId(CommentType type, Long targetId) {
        if (type == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "评论类型不能为空");
        }
        if (targetId == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "目标ID不能为空");
        }

        try {
            log.info("开始查询评论列表 - type: {}, targetId: {}", type.getDescription(), targetId);

            // 1. 获取所有评论
            List<CommentEntity> comments = commentRepository.findByTypeAndTargetId(type.getValue(), targetId);

            // 2. 构建评论树形结构
            List<CommentEntity> nestedComments = buildNestedComments(comments);

            log.info("查询评论列表完成 - type: {}, targetId: {}, count: {}",
                    type.getDescription(), targetId, nestedComments.size());

            return nestedComments;
        } catch (Exception e) {
            log.error("查询评论列表失败 - type: {}, targetId: {}", type.getDescription(), targetId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询评论列表失败：" + e.getMessage());
        }
    }

    /**
     * 构建评论的树形结构
     *
     * @param comments 原始评论列表
     * @return 构建好父子关系的评论列表
     */
    private List<CommentEntity> buildNestedComments(List<CommentEntity> comments) {
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // 1. 创建父子关系映射
            Map<Long, List<CommentEntity>> parentChildMap = new HashMap<>();
            for (CommentEntity comment : comments) {
                Long parentId = comment.getParentId();
                parentChildMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(comment);
            }

            // 2. 构建树形结构
            List<CommentEntity> rootComments = new ArrayList<>();
            for (CommentEntity comment : comments) {
                if (comment.getParentId() == null) {
                    // 设置子评论
                    comment.setChildren(parentChildMap.get(comment.getId()));
                    rootComments.add(comment);
                }
            }

            // 3. 按创建时间排序（先按父评论排序，再按子评论排序）
            rootComments.sort((c1, c2) -> c2.getCreateTime().compareTo(c1.getCreateTime()));
            for (CommentEntity rootComment : rootComments) {
                if (rootComment.getChildren() != null) {
                    rootComment.getChildren().sort((c1, c2) -> c2.getCreateTime().compareTo(c1.getCreateTime()));
                }
            }

            return rootComments;
        } catch (Exception e) {
            log.error("构建评论树形结构失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "构建评论结构失败：" + e.getMessage());
        }
    }

    private CommentEntity convertToEntity(CommentRequest request) {
        return CommentEntity.builder()
                .type(request.getType())
                .targetId(request.getTargetId())
                .parentId(request.getParentId())
                .userId(request.getUserId())
                .replyUserId(request.getReplyUserId())
                .content(request.getContent())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long userId) {
        try {
            log.info("开始删除评论 - commentId: {}, userId: {}", commentId, userId);

            // 1. 获取评论信息
            CommentEntity comment = commentRepository.findById(commentId);
            if (comment == null) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "评论不存在");
            }

            // 2. 验证是否为评论作者
            if (!comment.getUserId().equals(userId)) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "只能删除自己发布的评论");
            }

            // 3. 如果是一级评论（parentId为null），则需要删除所有子评论
            List<Long> commentIdsToDelete = new ArrayList<>();
            commentIdsToDelete.add(commentId);

            if (comment.getParentId() == null) {
                // 3.1 获取所有子评论
                List<CommentEntity> childComments = commentRepository.findByParentId(commentId);
                if (childComments != null && !childComments.isEmpty()) {
                    commentIdsToDelete.addAll(
                            childComments.stream()
                                    .map(CommentEntity::getId)
                                    .collect(Collectors.toList())
                    );
                }
            }

            // 4. 批量删除评论
            commentRepository.batchDelete(commentIdsToDelete);
            log.info("删除评论成功 - commentId: {}, 删除评论数量: {}", commentId, commentIdsToDelete.size());

        } catch (BusinessException e) {
            log.error("删除评论失败 - commentId: {}, userId: {}, error: {}", commentId, userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("删除评论发生未知错误 - commentId: {}, userId: {}", commentId, userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除评论失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取评论信息
     *
     * @param id 评论ID
     * @return 评论实体
     */
    public CommentEntity getCommentById(Long id) {
        try {
            log.info("获取评论信息 - id: {}", id);
            CommentEntity comment = commentRepository.findById(id);
            if (comment == null) {
                log.warn("评论不存在 - id: {}", id);
            }
            return comment;
        } catch (Exception e) {
            log.error("获取评论信息失败 - id: {}", id, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取评论信息失败：" + e.getMessage());
        }
    }

    /**
     * 删除评论（包括子评论）
     *
     * @param id 评论ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCommentWithReplies(Long id) {
        try {
            log.info("开始删除一级评论及其回复 - commentId: {}", id);
            
            // 1. 获取评论信息
            CommentEntity comment = commentRepository.findById(id);
            if (comment == null) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "评论不存在");
            }
            
            // 2. 验证是否为一级评论
            if (comment.getParentId() != null) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "只能删除一级评论");
            }
            
            // 3. 删除所有子评论
            commentRepository.deleteByParentId(id);
            log.info("删除子评论成功 - parentId: {}", id);
            
            // 4. 删除一级评论
            commentRepository.deleteById(id);
            log.info("删除一级评论成功 - commentId: {}", id);
            
        } catch (BusinessException e) {
            log.error("删除一级评论及其回复失败 - commentId: {}, error: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("删除一级评论及其回复发生未知错误 - commentId: {}", id, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除评论失败：" + e.getMessage());
        }
    }

    /**
     * 删除单条评论
     *
     * @param id 评论ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long id) {
        try {
            log.info("开始删除评论 - commentId: {}", id);
            
            // 1. 获取评论信息
            CommentEntity comment = commentRepository.findById(id);
            if (comment == null) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "评论不存在");
            }
            
            // 2. 删除评论
            commentRepository.deleteById(id);
            log.info("删除评论成功 - commentId: {}", id);
            
        } catch (BusinessException e) {
            log.error("删除评论失败 - commentId: {}, error: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("删除评论发生未知错误 - commentId: {}", id, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除评论失败：" + e.getMessage());
        }
    }

    /**
     * 分页获取一级评论列表（包含用户信息）
     *
     * @param request 评论请求参数
     * @return 分页评论列表
     */
    public PageResponse<List<CommentDTO>> getRootCommentsWithUserByPage(CommentRequest request) {
        try {
            log.info("分页获取一级评论列表 - request: {}", request);
            
            // 1. 获取总记录数
            long total = commentRepository.countRootComments(request.getType(), request.getUserId());
            
            // 2. 如果没有记录，直接返回空列表
            if (total == 0) {
                List<CommentDTO> emptyList = new ArrayList<>();
                return PageResponse.of(request.getPageNo(), request.getPageSize(), 0L, emptyList);
            }
            
            // 3. 计算分页参数
            int offset = (request.getPageNo() - 1) * request.getPageSize();
            int limit = request.getPageSize();
            
            // 4. 获取评论列表
            List<CommentEntity> comments = commentRepository.findRootCommentsByPage(
                request.getType(), 
                request.getUserId(), 
                offset, 
                limit
            );
            
            // 5. 获取用户信息
            Set<Long> userIds = comments.stream()
                    .map(CommentEntity::getUserId)
                    .collect(Collectors.toSet());
            Map<Long, UserEntity> userMap = userService.getUserMapByIds(userIds);
            
            // 6. 转换为DTO
            List<CommentDTO> commentDTOs = comments.stream()
                    .map(comment -> {
                        CommentDTO dto = convertToDTO(comment);
                        UserEntity user = userMap.get(comment.getUserId());
                        if (user != null) {
                            dto.setNickname(user.getNickname());
                            dto.setAvatar(user.getAvatar());
                        }
                        return dto;
                    })
                    .collect(Collectors.toList());
            
            // 7. 构建分页响应
            return PageResponse.of(request.getPageNo(), request.getPageSize(), total, commentDTOs);
            
        } catch (Exception e) {
            log.error("分页获取一级评论列表失败 - request: {}", request, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取评论列表失败：" + e.getMessage());
        }
    }

    /**
     * 分页获取二级评论列表
     *
     * @param parentId     父评论ID
     * @param pageRequest 分页参数
     * @return 评论列表
     */
    public List<CommentEntity> getPagedReplies(Long parentId, PageRequest pageRequest) {
        try {
            log.info("分页获取二级评论列表 - parentId: {}, pageRequest: {}", parentId, pageRequest);
            
            // 1. 计算分页参数
            int offset = (pageRequest.getPageNo() - 1) * pageRequest.getPageSize();
            int limit = pageRequest.getPageSize();
            
            // 2. 获取评论列表
            List<CommentEntity> replies = commentRepository.findRepliesByPage(parentId, offset, limit);
            
            log.info("分页获取二级评论列表成功 - parentId: {}, replies: {}", parentId, replies);
            return replies;
            
        } catch (Exception e) {
            log.error("分页获取二级评论列表失败 - parentId: {}", parentId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取回复列表失败：" + e.getMessage());
        }
    }

    /**
     * 将评论实体转换为DTO
     */
    private CommentDTO convertToDTO(CommentEntity entity) {
        if (entity == null) {
            return null;
        }
        return CommentDTO.builder()
                .id(entity.getId())
                .type(entity.getType())
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
     * 分页获取二级评论列表（包含用户信息）
     *
     * @param parentId    父评论ID
     * @param pageRequest 分页参数
     * @return 评论列表
     */
    public List<CommentReplyDTO> getPagedRepliesWithUser(Long parentId, PageRequest pageRequest) {
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
            Map<Long, UserEntity> userMap = userService.getUserMapByIds(userIds);
            
            // 5. 转换为DTO
            return replies.stream()
                    .map(reply -> {
                        CommentReplyDTO dto = CommentReplyDTO.builder()
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
