package cn.xu.api.controller.web.like.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LikeCountResponse {
    private Long count;
    private String type;
    private Long targetId;
} 