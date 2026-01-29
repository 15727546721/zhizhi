package cn.xu.cache.monitor;

import cn.xu.cache.core.RedisOperations;
import cn.xu.common.constants.LogConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Redis健康检查指示器
 * <p>用于 Spring Boot Actuator 健康检查</p>

 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisOperations redisOps;

    @Override
    public Health health() {
        log.info(LogConstants.REDIS_HEALTH_CHECK_START);
        long startTime = System.currentTimeMillis();
        
        try (RedisConnection connection = redisOps.getRedisTemplate().getConnectionFactory().getConnection()) {
            String pong = connection.ping();
            if ("PONG".equals(pong)) {
                Properties info = connection.info();
                String version = info.getProperty("redis_version", "unknown");
                String mode = info.getProperty("redis_mode", "unknown");
                String memory = info.getProperty("used_memory_human", "unknown");
                
                long endTime = System.currentTimeMillis();
                log.info(LogConstants.REDIS_HEALTH_CHECK_SUCCESS, version, mode, memory);
                
                return Health.up()
                    .withDetail("version", version)
                    .withDetail("mode", mode)
                    .withDetail("used_memory", memory)
                    .withDetail("total_connections_received", info.getProperty("total_connections_received", "0"))
                    .withDetail("total_commands_processed", info.getProperty("total_commands_processed", "0"))
                    .withDetail("connected_clients", info.getProperty("connected_clients", "0"))
                    .withDetail("uptime_in_seconds", info.getProperty("uptime_in_seconds", "0"))
                    .withDetail("check_time_cost", endTime - startTime + "ms")
                    .build();
            } else {
                log.error(LogConstants.REDIS_PING_FAILED, pong);
                return Health.down()
                    .withDetail("ping", pong)
                    .withDetail("error", "Redis服务器没有正确响应PING命令")
                    .withDetail("check_time_cost", System.currentTimeMillis() - startTime + "ms")
                    .build();
            }
        } catch (Exception e) {
            log.error(LogConstants.REDIS_HEALTH_CHECK_FAILED, e.getMessage());
            return Health.down(e)
                .withDetail("error", e.getMessage())
                .withDetail("check_time_cost", System.currentTimeMillis() - startTime + "ms")
                .build();
        }
    }
} 
