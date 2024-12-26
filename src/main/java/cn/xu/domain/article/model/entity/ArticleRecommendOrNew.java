package cn.xu.domain.article.model.entity;


import lombok.Data;

import java.util.List;

@Data
public class ArticleRecommendOrNew {
    private Long articleId;
    private String title;
    private String description;
    private String content;
    private String coverUrl;
    private Long userId;
    private String status;
    private Long viewCount;
    private Long collectCount;
    private Long likeCount;
    private String createTime;
    private String updateTime;
    private List<String> tags;
    private Long authorId;
    private String authorNickname;
    private String authorAvatar;
}
