package cn.xu.cache.config;

import cn.xu.common.constants.LogConstants;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.Properties;

/**
 * Redis指标配置
 * <p>为 Micrometer 提供 Redis 监控指标</p>
 
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisMetricsConfig {

    private final RedisConnectionFactory redisConnectionFactory;

    @Bean
    MeterRegistryCustomizer<MeterRegistry> redisMetrics() {
        return registry -> {
            log.info(LogConstants.REDIS_METRICS_COLLECT_START);
            long startTime = System.currentTimeMillis();

            try {
                // 连接Redis并收集指标
                registry.gauge("redis.pool.active",
                        redisConnectionFactory,
                        this::getActiveConnections);

                // 监控Redis内存使用情况
                registry.gauge("redis.memory.used",
                        redisConnectionFactory,
                        this::getUsedMemory);

                // 监控Redis键值对数量
                registry.gauge("redis.keys.total",
                        redisConnectionFactory,
                        this::getTotalKeys);

                long endTime = System.currentTimeMillis();
                log.info(LogConstants.REDIS_METRICS_COLLECT_END, endTime - startTime);

                // 记录当前Redis连接信息
                try (RedisConnection connection = redisConnectionFactory.getConnection()) {
                    Properties info = connection.info();
                    log.info(LogConstants.REDIS_CONNECTION_INFO,
                            info.getProperty("connected_clients", "0"),
                            info.getProperty("used_memory_human", "0"),
                            info.getProperty("db0", "keys=0").split(",")[0].split("=")[1]);
                }
            } catch (Exception e) {
                log.error(LogConstants.REDIS_METRICS_COLLECT_FAILED, "ALL", e.getMessage());
            }
        };
    }

    private double getActiveConnections(RedisConnectionFactory factory) {
        try (RedisConnection connection = factory.getConnection()) {
            Properties info = connection.info("clients");
            String clients = info.getProperty("connected_clients", "0");
            double value = Double.parseDouble(clients);
            log.debug("Redis 活跃连接数: {}", value);
            return value;
        } catch (Exception e) {
            log.error(LogConstants.REDIS_METRICS_COLLECT_FAILED, "连接数", e.getMessage());
            return -1;
        }
    }

    private double getUsedMemory(RedisConnectionFactory factory) {
        try (RedisConnection connection = factory.getConnection()) {
            Properties info = connection.info("memory");
            String memory = info.getProperty("used_memory", "0");
            double value = Double.parseDouble(memory);
            log.debug("Redis 内存使用情况: {} bytes", value);
            return value;
        } catch (Exception e) {
            log.error(LogConstants.REDIS_METRICS_COLLECT_FAILED, "内存使用", e.getMessage());
            return -1;
        }
    }

    private double getTotalKeys(RedisConnectionFactory factory) {
        try (RedisConnection connection = factory.getConnection()) {
            Properties info = connection.info("keyspace");
            String db0Info = info.getProperty("db0", "keys=0,expires=0,avg_ttl=0");
            String keysStr = db0Info.split(",")[0].split("=")[1];
            double value = Double.parseDouble(keysStr);
            log.debug("Redis 键总数: {}", value);
            return value;
        } catch (Exception e) {
            log.error(LogConstants.REDIS_METRICS_COLLECT_FAILED, "键数", e.getMessage());
            return -1;
        }
    }
}
