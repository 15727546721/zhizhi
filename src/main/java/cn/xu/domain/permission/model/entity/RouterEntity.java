package cn.xu.domain.permission.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RouterEntity {

    private Long id;
    private String component;
    private String path;
    private String name;

    private String redirect;

    private Integer sort;
    private MetaEntity meta;

    private List<RouterEntity> children;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MetaEntity {
        private String title;
        private String icon;
        private Boolean hidden;

        public MetaEntity(String title, String icon, Integer hidden) {
            this.title = title;
            this.icon = icon;
            this.hidden = hidden != null && hidden == 0;
        }
    }
}