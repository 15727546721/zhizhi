package cn.xu.infrastructure.persistent.converter;

import cn.xu.api.web.model.converter.UserVOConverter;
import cn.xu.api.web.model.vo.comment.CommentUserResponse;
import cn.xu.api.web.model.vo.comment.HotCommentResponse;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 热点评论转换器
 * 负责将CommentEntity转换为HotCommentResponse
 * 
 * @author Lily
 */
@Component
public class HotCommentConverter {
    
    @Resource
    private UserVOConverter userVOConverter;

    /**
     * 将CommentEntity转换为HotCommentResponse
     * 
     * @param commentEntity 评论实体
     * @return 热点评论Response
     */
    public HotCommentResponse convertToVO(CommentEntity commentEntity) {
        if (commentEntity == null) {
            return null;
        }
        
        HotCommentResponse vo = new HotCommentResponse();
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
            List<HotCommentResponse> childrenVOs = commentEntity.getChildren().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            vo.setChildren(childrenVOs);
        }
        
        return vo;
    }

    /**
     * 将UserEntity转换为CommentUserResponse
     * 
     * @param userEntity 用户实体
     * @return 评论用户Response
     */
    private CommentUserResponse convertUserToVO(UserEntity userEntity) {
        // 直接使用UserVOConverter进行转换，确保转换逻辑的一致性
        return userVOConverter.convertToCommentUserResponse(userEntity);
    }
}