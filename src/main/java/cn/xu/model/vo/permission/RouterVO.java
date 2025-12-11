package cn.xu.model.vo.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 路由VO
 * 用于返回给前端的路由结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouterVO {
    
    private Long id;
    private String path;
    private String name;
    private String component;
    private String redirect;
    private Integer sort;
    private MetaVO meta;
    private List<RouterVO> children;
    
    /**
     * 路由元信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetaVO {
        private String title;
        private String icon;
        private Integer hidden;
    }
}