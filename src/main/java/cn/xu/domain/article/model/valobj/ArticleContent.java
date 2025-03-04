package cn.xu.domain.article.model.valobj;

import lombok.Getter;

@Getter
public class ArticleContent {
    private final String title;
    private final String content;
    private final String summary;
    
    private ArticleContent(String title, String content, String summary) {
        this.title = title;
        this.content = content;
        this.summary = summary != null ? summary : generateSummary(content);
    }
    
    public static ArticleContent of(String title, String content, String summary) {
        validateTitle(title);
        validateContent(content);
        return new ArticleContent(title, content, summary);
    }
    
    private static void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("文章标题不能为空");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("文章标题不能超过100个字符");
        }
    }
    
    private static void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("文章内容不能为空");
        }
    }
    
    private String generateSummary(String content) {
        if (content.length() <= 200) {
            return content;
        }
        return content.substring(0, 197) + "...";
    }
} 