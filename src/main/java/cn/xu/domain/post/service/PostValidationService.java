package cn.xu.domain.post.service;

import cn.xu.common.ResponseCode;
import cn.xu.common.exception.BusinessException;
import cn.xu.domain.post.model.valobj.PostType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 帖子参数校验服务
 */
@Service
public class PostValidationService {
    
    /**
     * 校验帖子发布参数
     * @param title 标题
     * @param content 内容
     * @param categoryId 分类ID
     * @param type 类型
     */
    public void validatePostPublishParams(String title, String content, Long categoryId, String type) {
        // 校验标题
        if (StringUtils.isBlank(title)) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "帖子标题不能为空");
        }
        
        if (title.length() < 1 || title.length() > 100) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "帖子标题长度必须在1-100个字符之间");
        }
        
        // 校验内容
        if (StringUtils.isBlank(content)) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "帖子内容不能为空");
        }
        
        if (content.length() < 1 || content.length() > 10000) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "帖子内容长度必须在1-10000个字符之间");
        }
        
        // 校验分类ID
        if (categoryId == null) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "分类ID不能为空");
        }
        
        // 校验类型
        if (StringUtils.isBlank(type)) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "帖子类型不能为空");
        }
        
        try {
            PostType.fromCode(type);
        } catch (Exception e) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "帖子类型不正确");
        }
    }
    
    /**
     * 校验帖子分页查询参数
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