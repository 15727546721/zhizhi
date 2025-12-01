package cn.xu.config;

import cn.xu.model.entity.Like;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 热点数据配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "hot-data")
public class HotDataProperties {

    private List<HotDataItem> items;

    @Data
    public static class HotDataItem {
        private Long targetId;
        private Like.LikeType type;
        private boolean needWarmup;
        private boolean needRebuild;
    }
} 