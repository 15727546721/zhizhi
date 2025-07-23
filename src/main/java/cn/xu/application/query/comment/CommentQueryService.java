package cn.xu.application.query.comment;

import cn.xu.api.web.model.dto.comment.FindChildCommentItemVO;
import cn.xu.api.web.model.dto.comment.FindCommentItemVO;
import cn.xu.api.web.model.dto.comment.FindCommentReq;
import cn.xu.api.web.model.dto.comment.FindReplyReq;
import cn.xu.application.query.comment.dto.CommentDTO;
import cn.xu.application.query.comment.dto.CommentQuery;
import cn.xu.application.query.comment.dto.CommentWithRepliesDTO;
import cn.xu.application.query.comment.dto.ReplyQuery;
import cn.xu.application.query.user.UserDTO;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.repository.ICommentRepository;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import cn.xu.domain.user.service.impl.UserServiceImpl;
import cn.xu.infrastructure.cache.CommentCacheService;
import cn.xu.infrastructure.common.response.PageResponse;
import cn.xu.infrastructure.persistent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentQueryService {
    private final ICommentRepository commentRepository;
    private final UserRepository userRepository;
    private final IUserService userService;
    private final CommentCacheService commentCacheService;

    // 获取带预览回复的一级评论
    public List<CommentWithRepliesDTO> getCommentsWithPreview(CommentQuery query) {
        // 1. 尝试从缓存获取
        return commentCacheService.getCommentsWithPreview(
                query.getTargetType(),
                query.getTargetId(),
                query.getPageNo(),
                query.getPageSize(),
                () -> {
                    // 2. 缓存未命中则查询数据库
                    List<CommentEntity> comments = commentRepository.findRootComments(
                            query.getTargetType(),
                            query.getTargetId(),
                            query.getPageNo(),
                            query.getPageSize()
                    );

                    // 3. 获取预览回复
                    return buildCommentsWithReplies(comments, query.getPreviewSize());
                }
        );
    }

    // 获取评论的完整回复列表
    public PageResponse<List<CommentDTO>> getCommentReplies(ReplyQuery query) {
        return commentCacheService.getCommentReplies(
                query.getCommentId(),
                query.getPageNo(),
                query.getPageSize(),
                () -> {
                    List<CommentEntity> replies = commentRepository.findRepliesByPage(
                            query.getCommentId(),
                            (query.getPageNo() - 1) * query.getPageSize(),
                            query.getPageSize()
                    );

                    long total = commentRepository.countReplies(query.getCommentId());
                    List<CommentDTO> dtos = convertToCommentDTOs(replies);

                    return PageResponse.of(query.getPageNo(), query.getPageSize(), total, dtos);
                }
        );
    }

    // 构建带预览回复的评论列表
    private List<CommentWithRepliesDTO> buildCommentsWithReplies(
            List<CommentEntity> comments, int previewSize) {

        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量获取用户信息和预览回复
        List<Long> commentIds = comments.stream()
                .map(CommentEntity::getId)
                .collect(Collectors.toList());

        Map<Long, List<CommentEntity>> previewReplies =
                commentRepository.findPreviewRepliesByParentIds(commentIds, previewSize);

        Set<Long> userIds = collectUserIds(comments, previewReplies.values());
        Map<Long, UserEntity> userMap = userService.getUserMapByIds(userIds);

        // 构建返回结果
        return comments.stream().map(comment -> {
            CommentWithRepliesDTO dto = convertToCommentWithReplies(comment, userMap);

            List<CommentEntity> replies = previewReplies.getOrDefault(
                    comment.getId(), Collections.emptyList());

            dto.setPreviewReplies(convertToCommentDTOs(replies, userMap));
            long replyCount = comment.getReplyCount() != null ? comment.getReplyCount() : 0L;
            dto.setReplyCount(replyCount);


            return dto;
        }).collect(Collectors.toList());
    }

    private List<CommentDTO> convertToCommentDTOs(List<CommentEntity> comments) {
        return comments.stream().map(comment -> {
            CommentDTO dto = new CommentDTO();
            dto.setId(comment.getId());
            dto.setContent(comment.getContent());
            dto.setCreateTime(comment.getCreateTime());
            return dto;
        }).collect(Collectors.toList());
    }

    private List<CommentDTO> convertToCommentDTOs(List<CommentEntity> comments, Map<Long, UserEntity> userMap) {
        return comments.stream().map(comment -> {
            CommentDTO dto = new CommentDTO();
            dto.setId(comment.getId());
            dto.setContent(comment.getContent());
            dto.setCreateTime(comment.getCreateTime());
            dto.setUser(convertToUserDTO(userMap.get(comment.getUserId())));
            return dto;
        }).collect(Collectors.toList());
    }

    private CommentWithRepliesDTO convertToCommentWithReplies(CommentEntity comment, Map<Long, UserEntity> userMap) {
        CommentWithRepliesDTO dto = new CommentWithRepliesDTO();

        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreateTime(comment.getCreateTime());
        dto.setUser(convertToUserDTO(userMap.get(comment.getUserId())));
        dto.setReplyCount(comment.getReplyCount());

        return dto;
    }

    private Set<Long> collectUserIds(List<CommentEntity> comments, Collection<List<CommentEntity>> replyGroups) {
        Set<Long> userIds = comments.stream()
                .map(CommentEntity::getUserId)
                .collect(Collectors.toSet());

        for (List<CommentEntity> replies : replyGroups) {
            userIds.addAll(
                    replies.stream()
                            .map(CommentEntity::getUserId)
                            .collect(Collectors.toSet())
            );
        }

        return userIds;
    }

    public List<FindCommentItemVO> findTopComments(FindCommentReq request) {
        return commentRepository.findRootCommentWithUser(
                request.getTargetType(),
                request.getTargetId(),
                request.getPageNo(),
                request.getPageSize()
        );
    }

    public List<FindChildCommentItemVO> findChildComments(FindReplyReq request) {
        return commentRepository.findReplyPageWithUser(
                request.getCommentId(),
                request.getPageNo(),
                request.getPageSize()
        );
    }

    private UserDTO convertToUserDTO(UserEntity entity) {
        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setNickname(entity.getNickname());
        // 其他字段赋值
        return dto;
    }

}