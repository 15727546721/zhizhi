package cn.xu.domain.comment.model.valueobject;

import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.common.utils.SensitiveWordFilter;
import lombok.Getter;

/**
 * 评论内容值对象
 * 封装评论内容的业务规则和验证逻辑
 */
@Getter
public class CommentContent {
    
    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 500;
    
    private final String value;
    
    // 注入敏感词过滤器
    private static SensitiveWordFilter sensitiveWordFilter;
    
    public static void setSensitiveWordFilterStatic(SensitiveWordFilter filter) {
        CommentContent.sensitiveWordFilter = filter;
    }

    public CommentContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }
        
        String trimmedContent = content.trim();
        if (trimmedContent.length() < MIN_LENGTH) {
            throw new BusinessException("评论内容长度不能少于" + MIN_LENGTH + "个字符");
        }
        
        if (trimmedContent.length() > MAX_LENGTH) {
            throw new BusinessException("评论内容长度不能超过" + MAX_LENGTH + "个字符");
        }
        
        // 检查是否包含敏感词
        if (sensitiveWordFilter != null && sensitiveWordFilter.containsSensitiveWord(trimmedContent)) {
            throw new BusinessException("评论内容包含敏感词汇，请修改后重试");
        }
        
        // 检查是否为垃圾内容
        if (isSpamContent(trimmedContent)) {
            throw new BusinessException("请发表有意义的评论");
        }
        
        this.value = trimmedContent;
    }

    private boolean isSpamContent(String content) {
        // 检查是否为垃圾内容
        // 1. 全是重复字符
        if (isAllRepeatedCharacters(content)) {
            return true;
        }
        
        // 2. 只包含无意义的符号
        if (isOnlyMeaninglessSymbols(content)) {
            return true;
        }
        
        return false;
    }

    private boolean isAllRepeatedCharacters(String content) {
        if (content.length() <= 2) return false;
        
        char firstChar = content.charAt(0);
        for (int i = 1; i < content.length(); i++) {
            if (content.charAt(i) != firstChar) {
                return false;
            }
        }
        return true;
    }

    private boolean isOnlyMeaninglessSymbols(String content) {
        // 检查是否只包含无意义的符号
        String meaninglessPattern = "^[!@#$%^&*()_+\\-=\\[\\]{}|;':\",./<>?~`]+$";
        return content.matches(meaninglessPattern);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentContent that = (CommentContent) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}