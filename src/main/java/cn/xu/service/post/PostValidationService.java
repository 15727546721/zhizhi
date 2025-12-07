package cn.xu.service.post;

import cn.xu.common.ResponseCode;
import cn.xu.support.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 帖子参数校验服务
 * <p>负责帖子内容的参数验证</p>
 
 */
@Service
public class PostValidationService {

    // 帖子标题最小长度
    private static final int TITLE_MIN_LENGTH = 1;
    private static final int TITLE_MAX_LENGTH = 100;
    private static final int CONTENT_MIN_LENGTH = 1;
    private static final int CONTENT_MAX_LENGTH = 50000; // Post.java 中实际的最大长度

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

        if (title.length() < TITLE_MIN_LENGTH || title.length() > TITLE_MAX_LENGTH) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(),
                    String.format("帖子标题长度必须在%d-%d字符之间", TITLE_MIN_LENGTH, TITLE_MAX_LENGTH));
        }

        // 校验内容
        if (StringUtils.isBlank(content)) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "帖子内容不能为空");
        }

        if (content.length() < CONTENT_MIN_LENGTH || content.length() > CONTENT_MAX_LENGTH) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(),
                    String.format("帖子内容长度必须在%d-%d字符之间", CONTENT_MIN_LENGTH, CONTENT_MAX_LENGTH));
        }
    }

    /**
     * 校验帖子标签ID列表
     * @param tagIds 标签ID列表
     */
    public void validateTagIds(java.util.List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "请至少选择一个标签");
        }
        if (tagIds.size() > 5) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "标签最多只能选择5个");
        }
    }

    /**
     * 校验分页参数
     * @param pageNo 页码
     * @param pageSize 每页大小
     */
    public void validatePageParams(Integer pageNo, Integer pageSize) {
        if (pageNo == null || pageNo < 1) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "页码必须大于等于1");
        }

        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "每页大小必须在1到100之间");
        }
    }
}
