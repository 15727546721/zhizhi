package cn.xu.api.web.model.vo.comment;

import lombok.Data;

/**
 * 评论用户Response
 * 用于评论接口返回的用户信息
 * 
 * @author Lily
 */
@Data
public class CommentUserResponse {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 用户等级
     */
    private Integer level;
}