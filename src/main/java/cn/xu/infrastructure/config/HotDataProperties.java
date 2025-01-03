package cn.xu.infrastructure.config;

import cn.xu.domain.like.model.LikeType;
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
        private LikeType type;
        private boolean needWarmup;
        private boolean needRebuild;
    }
} 