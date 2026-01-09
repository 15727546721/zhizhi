package cn.xu.service.comment;

import cn.xu.model.entity.Comment;
import cn.xu.model.entity.User;
import cn.xu.model.vo.comment.CommentVO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评论转换器
 * <p>负责 Comment 实体与 VO 之间的转换</p>
 */
@Component
public class CommentConverter {

    /**
     * 批量转换评论为 VO
     *
     * @param comments      评论列表
     * @param userLikeSet   当前用户点赞的评论ID集合
     * @param authorLikeSet 作者点赞的评论ID集合
     * @param authorId      帖子作者ID
     */
    public List<CommentVO> toVOList(List<Comment> comments,
                                    Set<Long> userLikeSet,
                                    Set<Long> authorLikeSet,
                                    Long authorId) {
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }
        return comments.stream()
                .map(c -> toVO(c, userLikeSet, authorLikeSet, authorId))
                .collect(Collectors.toList());
    }

    /**
     * 单个评论转换为 VO
     */
    public CommentVO toVO(Comment comment,
                          Set<Long> userLikeSet,
                          Set<Long> authorLikeSet,
                          Long authorId) {
        if (comment == null) {
            return null;
        }

        List<CommentVO> childrenVOs = new ArrayList<>();
        if (comment.getChildren() != null && !comment.getChildren().isEmpty()) {
            childrenVOs = comment.getChildren().stream()
                    .map(child -> toVO(child, userLikeSet, authorLikeSet, authorId))
                    .collect(Collectors.toList());
        }

        return CommentVO.builder()
                .id(comment.getId())
                .postId(comment.getTargetId())
                .content(comment.getContent())
                .imageUrls(comment.getImageUrls())
                .parentId(comment.getParentId())
                .replyToUserId(comment.getReplyUserId())
                .replyToNickname(comment.getReplyUser() != null ? comment.getReplyUser().getNickname() : null)
                .level(calculateLevel(comment))
                .userId(comment.getUserId())
                .nickname(comment.getUser() != null ? comment.getUser().getNickname() : null)
                .avatar(comment.getUser() != null ? comment.getUser().getAvatar() : null)
                .userType(comment.getUser() != null ? comment.getUser().getUserType() : 0)
                .likeCount(comment.getLikeCount())
                .replyCount(comment.getReplyCount())
                .isLiked(userLikeSet != null && userLikeSet.contains(comment.getId()))
                .isAuthorLiked(authorLikeSet != null && authorLikeSet.contains(comment.getId()))
                .isTop(false)
                .isHot(comment.getLikeCount() != null && comment.getLikeCount() > 10)
                .isAuthor(authorId != null && authorId.equals(comment.getUserId()))
                .isAuthorLiked(authorLikeSet != null && authorLikeSet.contains(comment.getId()))
                .status(1)
                .createTime(comment.getCreateTime())
                .updateTime(comment.getUpdateTime())
                .replies(childrenVOs)
                .hasMoreReplies(comment.getReplyCount() != null && comment.getReplyCount() > childrenVOs.size())
                .build();
    }

    /**
     * 计算评论层级
     */
    private Integer calculateLevel(Comment comment) {
        if (comment.getParentId() == null || comment.getParentId() == 0) {
            return 1;
        }
        return 2;
    }

    /**
     * 收集评论及子评论的所有ID
     */
    public List<Long> collectAllIds(List<Comment> comments) {
        List<Long> ids = new ArrayList<>();
        if (comments == null) {
            return ids;
        }
        for (Comment c : comments) {
            if (c.getId() != null) {
                ids.add(c.getId());
            }
            if (c.getChildren() != null) {
                ids.addAll(collectAllIds(c.getChildren()));
            }
        }
        return ids;
    }

    /**
     * 填充用户信息到评论
     */
    public void fillUserInfo(List<Comment> comments, Map<Long, User> userMap) {
        if (comments == null || comments.isEmpty() || userMap == null) {
            return;
        }
        for (Comment comment : comments) {
            if (comment.getUserId() != null) {
                comment.setUser(userMap.get(comment.getUserId()));
            }
            if (comment.getReplyUserId() != null) {
                comment.setReplyUser(userMap.get(comment.getReplyUserId()));
            }
            // 递归处理子评论
            if (comment.getChildren() != null && !comment.getChildren().isEmpty()) {
                fillUserInfo(comment.getChildren(), userMap);
            }
        }
    }
}
