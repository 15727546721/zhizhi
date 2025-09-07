package cn.xu.api.web.model.dto.like;

import lombok.Data;

/**
 * 点赞数响应DTO
 */
@Data
public class LikeCountResponse {
    
    /**
     * 目标ID
     */
    private Long targetId;
    
    /**
     * 点赞类型
     */
    private String type;
    
    /**
     * 点赞数
     */
    private Long count;
    
    /**
     * 是否已点赞
     */
    private Boolean liked;
    
    public LikeCountResponse() {}
    
    public LikeCountResponse(Long targetId, String type, Long count, Boolean liked) {
        this.targetId = targetId;
        this.type = type;
        this.count = count;
        this.liked = liked;
    }
}