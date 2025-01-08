package cn.xu.domain.comment.service.comment;

import cn.xu.api.controller.web.comment.request.CommentRequest;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.model.valueobject.CommentType;
import cn.xu.domain.comment.repository.ICommentRepository;
import cn.xu.domain.comment.service.ICommentService;
import cn.xu.exception.AppException;
import cn.xu.infrastructure.common.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommentService implements ICommentService {

    @Resource
    private ICommentRepository commentRepository;

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
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "回复的评论不存在");
        }

        CommentEntity commentEntity = convertToEntity(request);
        commentEntity.validate();
        commentRepository.replyComment(commentEntity);
    }

    @Override
    public List<CommentEntity> getCommentsByTypeAndTargetId(CommentType type, Long targetId) {
        if (type == null) {
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "评论类型不能为空");
        }
        if (targetId == null) {
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "目标ID不能为空");
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
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "查询评论列表失败：" + e.getMessage());
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
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "构建评论结构失败：" + e.getMessage());
        }
    }

    private CommentEntity convertToEntity(CommentRequest request) {
        return CommentEntity.builder()
                .type(request.getType())
                .targetId(request.getTargetId())
                .parentId(request.getParentId())
                .userId(request.getUserId())
                .replyToUserId(request.getReplyToUserId())
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
                throw new AppException(ResponseCode.UN_ERROR.getCode(), "评论不存在");
            }

            // 2. 验证是否为评论作者
            if (!comment.getUserId().equals(userId)) {
                throw new AppException(ResponseCode.UN_ERROR.getCode(), "只能删除自己发布的评论");
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

        } catch (AppException e) {
            log.error("删除评论失败 - commentId: {}, userId: {}, error: {}", commentId, userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("删除评论发生未知错误 - commentId: {}, userId: {}", commentId, userId, e);
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "删除评论失败：" + e.getMessage());
        }
    }
}
