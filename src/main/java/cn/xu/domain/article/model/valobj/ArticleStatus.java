package cn.xu.domain.article.model.valobj;

import lombok.Getter;

@Getter
public enum ArticleStatus {
    DRAFT("草稿"),
    PUBLISHED("已发布"),
    ARCHIVED("已归档");
    
    private final String description;
    
    ArticleStatus(String description) {
        this.description = description;
    }
    
    public boolean canPublish() {
        return this == DRAFT;
    }
    
    public boolean canArchive() {
        return this == PUBLISHED;
    }
    
    public boolean isDraft() {
        return this == DRAFT;
    }
    
    public boolean isPublished() {
        return this == PUBLISHED;
    }
    
    public boolean isArchived() {
        return this == ARCHIVED;
    }
} 