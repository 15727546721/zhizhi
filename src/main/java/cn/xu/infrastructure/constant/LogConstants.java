package cn.xu.infrastructure.constant;

/**
 * 日志常量类
 */
public class LogConstants {
    
    // Redis操作相关日志
    public static final String REDIS_OPERATION_SUCCESS = "Redis操作成功 - 操作类型: {}, Key: {}, 耗时: {}ms";
    public static final String REDIS_OPERATION_FAILED = "Redis操作失败 - 操作类型: {}, Key: {}, 错误信息: {}";
    public static final String REDIS_KEY_NOT_EXIST = "Redis键不存在 - Key: {}";
    public static final String REDIS_KEY_EXPIRED = "Redis键已过期 - Key: {}";
    public static final String REDIS_CONNECTION_FAILED = "Redis连接失败 - 错误信息: {}";
    
    // Redis监控相关日志
    public static final String REDIS_METRICS_COLLECT_START = "开始收集Redis监控指标";
    public static final String REDIS_METRICS_COLLECT_END = "Redis监控指标收集完成 - 耗时: {}ms";
    public static final String REDIS_METRICS_COLLECT_FAILED = "Redis监控指标收集失败 - 指标类型: {}, 错误信息: {}";
    public static final String REDIS_CONNECTION_INFO = "Redis连接信息 - 活跃连接数: {}, 内存使用: {}, 键总数: {}";
    
    // Redis健康检查相关日志
    public static final String REDIS_HEALTH_CHECK_START = "开始Redis健康检查";
    public static final String REDIS_HEALTH_CHECK_SUCCESS = "Redis健康检查通过 - 版本: {}, 模式: {}, 内存使用: {}";
    public static final String REDIS_HEALTH_CHECK_FAILED = "Redis健康检查失败 - 错误信息: {}";
    public static final String REDIS_PING_FAILED = "Redis ping响应异常 - 响应结果: {}";
    
    // 缓存操作相关日志
    public static final String CACHE_HIT = "缓存命中 - Key: {}, 耗时: {}ms";
    public static final String CACHE_MISS = "缓存未命中 - Key: {}, 将从数据库加载";
    public static final String CACHE_UPDATE = "更新缓存 - Key: {}, 过期时间: {}秒";
    public static final String CACHE_EVICT = "删除缓存 - Key: {}, 原因: {}";
    
    // 缓存预热相关日志
    public static final String CACHE_WARMUP_START = "开始缓存预热 - 类型: {}";
    public static final String CACHE_WARMUP_END = "缓存预热完成 - 类型: {}, 预热数量: {}, 耗时: {}ms";
    public static final String CACHE_WARMUP_FAILED = "缓存预热失败 - 类型: {}, 错误信息: {}";
} 