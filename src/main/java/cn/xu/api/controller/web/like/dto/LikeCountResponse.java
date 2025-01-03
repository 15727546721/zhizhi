package cn.xu.api.controller.web.like.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 点赞数量响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeCountResponse {
    
    /**
     * 点赞数量
     */
    private Long count;
    
    /**
     * 点赞类型
     */
    private String type;
    
    /**
     * 目标ID
     */
    private Long targetId;
} 