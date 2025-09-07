package cn.xu.domain.article.model.valobj;

import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.common.utils.SensitiveWordFilter;
import lombok.Getter;

/**
 * 文章标题值对象
 * 封装文章标题的业务规则和验证逻辑
 */
@Getter
public class ArticleTitle {
    
    private static final int MIN_LENGTH = 5;
    private static final int MAX_LENGTH = 100;
    
    private final String value;
    
    // 注入敏感词过滤器
    private static SensitiveWordFilter sensitiveWordFilter;
    
    public static void setSensitiveWordFilterStatic(SensitiveWordFilter filter) {
        ArticleTitle.sensitiveWordFilter = filter;
    }

    public ArticleTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException("文章标题不能为空");
        }
        
        String trimmedTitle = title.trim();
        if (trimmedTitle.length() < MIN_LENGTH) {
            throw new BusinessException("文章标题长度不能少于" + MIN_LENGTH + "个字符");
        }
        
        if (trimmedTitle.length() > MAX_LENGTH) {
            throw new BusinessException("文章标题长度不能超过" + MAX_LENGTH + "个字符");
        }
        
        // 检查是否包含敏感词或特殊字符
        if (sensitiveWordFilter != null && sensitiveWordFilter.containsSensitiveWord(trimmedTitle)) {
            throw new BusinessException("文章标题包含敏感词汇");
        }
        
        this.value = trimmedTitle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticleTitle that = (ArticleTitle) o;
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
    
    /**
     * 获取标题的修剪版本（兼容性方法）
     */
    public ArticleTitle trim() {
        return this; // 在构造函数中已经处理了trim
    }
    
    /**
     * 获取标题长度（兼容性方法）
     */
    public int length() {
        return value.length();
    }
}