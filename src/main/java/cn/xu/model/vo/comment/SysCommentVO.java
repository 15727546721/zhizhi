package cn.xu.model.vo.comment;

import cn.xu.model.entity.Comment;
import cn.xu.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 后台管理-评论VO

 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysCommentVO {
    
    /** 评论ID */
    private Long id;
    
    /** 评论类型 */
    private Integer type;
    
    /** 目标ID */
    private Long targetId;
    
    /** 父评论ID */
    private Long parentId;
    
    /** 评论用户ID */
    private Long userId;
    
    /** 评论用户昵称 */
    private String nickname;
    
    /** 评论用户头像 */
    private String avatar;
    
    /** 被回复用户ID */
    private Long replyUserId;
    
    /** 被回复用户昵称 */
    private String replyNickname;
    
    /** 被回复用户头像 */
    private String replyAvatar;
    
    /** 评论内容 */
    private String content;
    
    /** 点赞数 */
    private Long likeCount;
    
    /** 回复数 */
    private Long replyCount;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /**
     * 从 Comment PO 转换
     */
    public static SysCommentVO fromComment(Comment comment) {
        if (comment == null) {
            return null;
        }
        
        SysCommentVO vo = SysCommentVO.builder()
                .id(comment.getId())
                .type(comment.getTargetType())
                .targetId(comment.getTargetId())
                .parentId(comment.getParentId())
                .userId(comment.getUserId())
                .replyUserId(comment.getReplyUserId())
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .replyCount(comment.getReplyCount())
                .createTime(comment.getCreateTime())
                .build();
        
        // 设置评论用户信息
        User user = comment.getUser();
        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }
        
        // 设置被回复用户信息
        User replyUser = comment.getReplyUser();
        if (replyUser != null) {
            vo.setReplyNickname(replyUser.getNickname());
            vo.setReplyAvatar(replyUser.getAvatar());
        }
        
        return vo;
    }
}
