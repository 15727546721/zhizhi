package cn.xu.domain.post.model.valobj;

import cn.xu.common.exception.BusinessException;
import cn.xu.common.utils.SensitiveWordFilter;
import lombok.Getter;

/**
 * 帖子标题值对象
 * 封装帖子标题的业务规则和验证逻辑
 */
@Getter
public class PostTitle {
    
    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 100;
    
    private final String value;
    
    // 注入敏感词过滤器
    private static SensitiveWordFilter sensitiveWordFilter;
    
    public static void setSensitiveWordFilterStatic(SensitiveWordFilter filter) {
        PostTitle.sensitiveWordFilter = filter;
    }

    public PostTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException("帖子标题不能为空");
        }
        
        String trimmedTitle = title.trim();
        if (trimmedTitle.length() < MIN_LENGTH) {
            throw new BusinessException("帖子标题长度不能少于" + MIN_LENGTH + "个字符");
        }
        
        if (trimmedTitle.length() > MAX_LENGTH) {
            throw new BusinessException("帖子标题长度不能超过" + MAX_LENGTH + "个字符");
        }
        
        // 检查标题格式（不允许连续空格或特殊符号）
        if (trimmedTitle.matches(".*\\s{2,}.*") || trimmedTitle.matches(".*[\\p{Punct}&&[^,.?!]]+.*")) {
            throw new BusinessException("帖子标题格式不正确，请避免使用连续空格或特殊符号");
        }
        
        // 检查是否包含敏感词
        if (sensitiveWordFilter != null && sensitiveWordFilter.containsSensitiveWord(trimmedTitle)) {
            throw new BusinessException("帖子标题包含敏感词汇，请修改后重试");
        }
        
        this.value = trimmedTitle;
    }

    /**
     * 获取标题预览（截取前30个字符）
     * 
     * @return 标题预览
     */
    public String getPreview() {
        if (value == null) {
            return "";
        }
        if (value.length() <= 30) {
            return value;
        }
        return value.substring(0, 30) + "...";
    }
    
    /**
     * 获取标题长度
     * 
     * @return 标题长度
     */
    public int getLength() {
        return value != null ? value.length() : 0;
    }
    
    /**
     * 判断标题是否包含指定关键词
     * 
     * @param keyword 关键词
     * @return 是否包含关键词
     */
    public boolean containsKeyword(String keyword) {
        if (value == null || keyword == null) {
            return false;
        }
        return value.contains(keyword);
    }
    
    /**
     * 判断是否为长标题
     * 
     * @return 是否为长标题
     */
    public boolean isLongTitle() {
        return value != null && value.length() > 50;
    }
    
    /**
     * 获取标题的单词数（简单实现）
     * 
     * @return 单词数
     */
    public int getWordCount() {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        // 简单的单词计数实现，按空格分割
        String[] words = value.trim().split("\\s+");
        return words.length;
    }
    
    /**
     * 获取首字母大写的标题
     * 
     * @return 首字母大写的标题
     */
    public String getCapitalizedTitle() {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    // 添加getTitle方法以兼容现有代码
    public String getTitle() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostTitle postTitle = (PostTitle) o;
        return value.equals(postTitle.value);
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