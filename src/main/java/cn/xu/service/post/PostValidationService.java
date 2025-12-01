package cn.xu.service.post;

import cn.xu.common.ResponseCode;
import cn.xu.support.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 帖子参数校验服务
 * 
 * <p>提供帖子相关的参数校验功能，包括发布参数、标签、分页参数等校验
 * 
 * @author xu
 * @since 2025-11-26
 */
@Service
public class PostValidationService {
    
    /**
     * 校验帖子发布参数
     * @param title 标题
     * @param content 内容
     */
    public void validatePostPublishParams(String title, String content) {
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
    }
    
    /**
     * 校验标签ID列表
     * @param tagIds 标签ID列表
     */
    public void validateTagIds(java.util.List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "请至少选择一个标签");
        }
        if (tagIds.size() > 5) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "标签不能超过5个");
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