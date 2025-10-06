package cn.xu.domain.post.model.valobj;

import cn.xu.common.exception.BusinessException;
import cn.xu.common.utils.SensitiveWordFilter;
import lombok.Getter;

/**
 * 帖子内容值对象
 * 封装帖子内容的业务规则和验证逻辑
 */
@Getter
public class PostContent {
    
    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 10000;
    
    private final String value;
    
    // 注入敏感词过滤器
    private static SensitiveWordFilter sensitiveWordFilter;
    
    public static void setSensitiveWordFilterStatic(SensitiveWordFilter filter) {
        PostContent.sensitiveWordFilter = filter;
    }

    public PostContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("帖子内容不能为空");
        }
        
        String trimmedContent = content.trim();
        if (trimmedContent.length() < MIN_LENGTH) {
            throw new BusinessException("帖子内容长度不能少于" + MIN_LENGTH + "个字符");
        }
        
        if (trimmedContent.length() > MAX_LENGTH) {
            throw new BusinessException("帖子内容长度不能超过" + MAX_LENGTH + "个字符");
        }
        
        // 检查是否包含敏感词
        if (sensitiveWordFilter != null && sensitiveWordFilter.containsSensitiveWord(trimmedContent)) {
            throw new BusinessException("帖子内容包含敏感词汇，请修改后重试");
        }
        
        this.value = trimmedContent;
    }

    /**
     * 获取内容预览（截取前200个字符）
     * 
     * @return 内容预览
     */
    public String getPreview() {
        if (value == null) {
            return "";
        }
        if (value.length() <= 200) {
            return value;
        }
        return value.substring(0, 200) + "...";
    }
    
    /**
     * 获取格式化的内容（处理换行符等）
     * 
     * @return 格式化后的内容
     */
    public String getFormattedContent() {
        if (value == null) {
            return "";
        }
        // 将换行符替换为HTML的<br>标签
        return value.replace("\n", "<br>");
    }
    
    /**
     * 获取纯文本内容（去除HTML标签）
     * 
     * @return 纯文本内容
     */
    public String getPlainText() {
        if (value == null) {
            return "";
        }
        // 简单的HTML标签去除
        return value.replaceAll("<[^>]*>", "");
    }
    
    /**
     * 获取内容长度
     * 
     * @return 内容长度
     */
    public int getLength() {
        return value != null ? value.length() : 0;
    }
    
    /**
     * 判断内容是否包含指定关键词
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
     * 获取内容的单词数（简单实现）
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostContent that = (PostContent) o;
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