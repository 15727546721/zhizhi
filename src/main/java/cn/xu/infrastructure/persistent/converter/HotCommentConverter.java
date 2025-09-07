package cn.xu.infrastructure.persistent.converter;

import cn.xu.api.web.model.vo.comment.CommentUserVO;
import cn.xu.api.web.model.vo.comment.HotCommentVO;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 热点评论转换器
 * 负责将CommentEntity转换为HotCommentVO
 * 
 * @author Lily
 */
@Component
public class HotCommentConverter {

    /**
     * 将CommentEntity转换为HotCommentVO
     * 
     * @param commentEntity 评论实体
     * @return 热点评论VO
     */
    public HotCommentVO convertToVO(CommentEntity commentEntity) {
        if (commentEntity == null) {
            return null;
        }
        
        HotCommentVO vo = new HotCommentVO();
        vo.setId(commentEntity.getId());
        vo.setTargetType(commentEntity.getTargetType());
        vo.setTargetId(commentEntity.getTargetId());
        vo.setUserId(commentEntity.getUserId());
        vo.setContent(commentEntity.getContentValue());
        vo.setLikeCount(commentEntity.getLikeCount());
        vo.setReplyCount(commentEntity.getReplyCount());
        vo.setCreateTime(commentEntity.getCreateTime());
        vo.setHotScore(commentEntity.getHotScore());
        vo.setIsHot(commentEntity.isHot());
        vo.setImageUrls(commentEntity.getImageUrls());
        
        // 转换用户信息
        vo.setUser(convertUserToVO(commentEntity.getUser()));
        vo.setReplyUser(convertUserToVO(commentEntity.getReplyUser()));
        
        // 转换子评论
        if (commentEntity.getChildren() != null) {
            List<HotCommentVO> childrenVOs = commentEntity.getChildren().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            vo.setChildren(childrenVOs);
        }
        
        return vo;
    }

    /**
     * 将UserEntity转换为CommentUserVO
     * 
     * @param userEntity 用户实体
     * @return 评论用户VO
     */
    private CommentUserVO convertUserToVO(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }
        
        CommentUserVO vo = new CommentUserVO();
        vo.setId(userEntity.getId());
        vo.setNickname(userEntity.getNickname());
        vo.setAvatar(userEntity.getAvatar());
        // UserEntity中没有getLevel()方法，所以移除这一行
        
        return vo;
    }
}