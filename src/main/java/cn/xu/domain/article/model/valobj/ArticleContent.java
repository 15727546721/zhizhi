package cn.xu.domain.article.model.valobj;

import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.common.utils.SensitiveWordFilter;
import lombok.Getter;

/**
 * 文章内容值对象
 * 封装文章内容的业务规则和验证逻辑
 */
@Getter
public class ArticleContent {
    
    private static final int MIN_LENGTH = 50;
    private static final int MAX_LENGTH = 100000;
    
    private final String value;
    
    // 注入敏感词过滤器
    private static SensitiveWordFilter sensitiveWordFilter;
    
    public static void setSensitiveWordFilterStatic(SensitiveWordFilter filter) {
        sensitiveWordFilter = filter;
    }

    public ArticleContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("文章内容不能为空");
        }
        
        String trimmedContent = content.trim();
        if (trimmedContent.length() < MIN_LENGTH) {
            throw new BusinessException("文章内容长度不能少于" + MIN_LENGTH + "个字符");
        }
        
        if (trimmedContent.length() > MAX_LENGTH) {
            throw new BusinessException("文章内容长度不能超过" + MAX_LENGTH + "个字符");
        }
        
        // 检查是否包含敏感词
        if (sensitiveWordFilter != null && sensitiveWordFilter.containsSensitiveWord(trimmedContent)) {
            throw new BusinessException("文章内容包含敏感词汇，无法发布");
        }
        
        // 检查内容质量
        if (isLowQuality(trimmedContent)) {
//            throw new BusinessException("文章内容质量不达标，请完善内容后再发布");
        }
        
        this.value = trimmedContent;
    }

    /**
     * 检查内容质量
     */
    private boolean isLowQuality(String content) {
        // 检查是否主要是重复字符
        if (isMainlyRepeatedCharacters(content)) {
            return true;
        }
        
        // 检查是否包含过多的特殊字符
        if (hasExcessiveSpecialCharacters(content)) {
            return true;
        }
        
        return false;
    }

    private boolean isMainlyRepeatedCharacters(String content) {
        if (content.length() < 10) return false;
        
        // 简单检查：如果超过30%是重复字符则认为质量低
        int maxRepeated = 0;
        char mostFrequentChar = 0;
        
        for (char c : content.toCharArray()) {
            int count = (int) content.chars().filter(ch -> ch == c).count();
            if (count > maxRepeated) {
                maxRepeated = count;
                mostFrequentChar = c;
            }
        }
        
        return maxRepeated > content.length() * 0.3 && mostFrequentChar != ' ';
    }

    private boolean hasExcessiveSpecialCharacters(String content) {
        long specialCharCount = content.chars()
                .filter(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c))
                .count();
        return specialCharCount > content.length() * 0.2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticleContent that = (ArticleContent) o;
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