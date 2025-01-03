package cn.xu.api.controller.web.like.dto;

import lombok.Data;

@Data
public class LikeRequest {
    private Long userId;
    private Long targetId;
    /**
     * ARTICLE(1, "文章"),
     * TOPIC(2, "话题"),
     * COMMENT(3, "评论");
     */
    private String type;
} 