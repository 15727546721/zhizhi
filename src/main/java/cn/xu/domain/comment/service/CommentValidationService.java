package cn.xu.domain.comment.service;

import cn.xu.common.ResponseCode;
import cn.xu.common.exception.BusinessException;
import cn.xu.domain.comment.model.valueobject.CommentType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 评论参数校验服务
 */
@Service
public class CommentValidationService {
    
    /**
     * 校验评论创建参数
     * @param type 评论类型
     * @param targetId 目标ID
     * @param content 内容
     */
    public void validateCommentCreateParams(Integer type, Long targetId, String content) {
        // 校验评论类型
        if (type == null) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "评论类型不能为空");
        }
        
        try {
            CommentType.valueOf(type);
        } catch (Exception e) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "评论类型不正确");
        }
        
        // 校验目标ID
        if (targetId == null) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "评论目标ID不能为空");
        }
        
        // 校验内容
        if (StringUtils.isBlank(content)) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "评论内容不能为空");
        }
        
        if (content.length() > 1000) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "评论内容不能超过1000字");
        }
    }
    
    /**
     * 校验评论回复参数
     * @param type 评论类型
     * @param targetId 目标ID
     * @param commentId 父评论ID
     * @param replyUserId 被回复用户ID
     * @param content 内容
     */
    public void validateCommentReplyParams(Integer type, Long targetId, Long commentId, Long replyUserId, String content) {
        // 校验评论类型
        if (type == null) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "评论类型不能为空");
        }
        
        try {
            CommentType.valueOf(type);
        } catch (Exception e) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "评论类型不正确");
        }
        
        // 校验目标ID
        if (targetId == null) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "评论目标ID不能为空");
        }
        
        // 校验父评论ID
        if (commentId == null) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "父评论ID不能为空");
        }
        
        // 校验被回复用户ID
        if (replyUserId == null) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "被回复用户ID不能为空");
        }
        
        // 校验内容
        if (StringUtils.isBlank(content)) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "评论内容不能为空");
        }
        
        if (content.length() > 1000) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "评论内容不能超过1000字");
        }
    }
    
    /**
     * 校验分页查询参数
     * @param pageNo 页码
     * @param pageSize 页面大小
     */
    public void validatePageParams(Integer pageNo, Integer pageSize) {
        if (pageNo == null || pageNo < 1) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "页码不能小于1");
        }
        
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "每页数量必须在1-100之间");
        }
    }
}