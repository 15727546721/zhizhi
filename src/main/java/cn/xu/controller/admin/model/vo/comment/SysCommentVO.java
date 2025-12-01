package cn.xu.controller.admin.model.vo.comment;

import cn.xu.model.entity.Comment;
import cn.xu.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 绯荤粺绠＄悊-璇勮VO
 * 
 * @author xu
 * @since 2025-11-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysCommentVO {
    
    /** 璇勮ID */
    private Long id;
    
    /** 璇勮绫诲瀷 */
    private Integer type;
    
    /** 鐩爣ID */
    private Long targetId;
    
    /** 鐖惰瘎璁篒D */
    private Long parentId;
    
    /** 璇勮鐢ㄦ埛ID */
    private Long userId;
    
    /** 璇勮鐢ㄦ埛鏄电О */
    private String nickname;
    
    /** 璇勮鐢ㄦ埛澶村儚 */
    private String avatar;
    
    /** 琚洖澶嶇敤鎴稩D */
    private Long replyUserId;
    
    /** 琚洖澶嶇敤鎴锋樀绉?*/
    private String replyNickname;
    
    /** 琚洖澶嶇敤鎴峰ご鍍?*/
    private String replyAvatar;
    
    /** 璇勮鍐呭 */
    private String content;
    
    /** 鐐硅禐鏁?*/
    private Long likeCount;
    
    /** 鍥炲鏁?*/
    private Long replyCount;
    
    /** 鍒涘缓鏃堕棿 */
    private LocalDateTime createTime;
    
    /**
     * 浠?Comment PO 杞崲
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
        
        // 濉厖璇勮鐢ㄦ埛淇℃伅
        User user = comment.getUser();
        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }
        
        // 濉厖琚洖澶嶇敤鎴蜂俊鎭?
        User replyUser = comment.getReplyUser();
        if (replyUser != null) {
            vo.setReplyNickname(replyUser.getNickname());
            vo.setReplyAvatar(replyUser.getAvatar());
        }
        
        return vo;
    }
}
