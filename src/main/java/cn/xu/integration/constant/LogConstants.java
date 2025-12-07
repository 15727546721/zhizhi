package cn.xu.integration.constant;

/**
 * 日志常量类
 * <p>定义系统中使用的日志格式常量</p>

 */
public class LogConstants {

    // ==================== Redis操作相关日志 ====================
    public static final String REDIS_OPERATION_SUCCESS = "[Redis] 操作成功 - 操作类型: {}, Key: {}, 耗时: {}ms";
    public static final String REDIS_OPERATION_FAILED = "[Redis] 操作失败 - 操作类型: {}, Key: {}, 错误信息: {}";
    public static final String REDIS_KEY_NOT_EXIST = "[Redis] 键不存在 - Key: {}";
    public static final String REDIS_KEY_EXPIRED = "[Redis] 键已过期 - Key: {}";
    public static final String REDIS_CONNECTION_FAILED = "[Redis] 连接失败 - 错误信息: {}";

    // ==================== Redis监控相关日志 ====================
    public static final String REDIS_METRICS_COLLECT_START = "[Redis] 开始收集监控指标";
    public static final String REDIS_METRICS_COLLECT_END = "[Redis] 监控指标收集完成 - 耗时: {}ms";
    public static final String REDIS_METRICS_COLLECT_FAILED = "[Redis] 监控指标收集失败 - 指标类型: {}, 错误信息: {}";
    public static final String REDIS_CONNECTION_INFO = "[Redis] 连接信息 - 活跃连接数: {}, 内存使用: {}, 键总数: {}";

    // ==================== Redis健康检查相关日志 ====================
    public static final String REDIS_HEALTH_CHECK_START = "[Redis] 开始健康检查";
    public static final String REDIS_HEALTH_CHECK_SUCCESS = "[Redis] 健康检查通过 - 版本: {}, 模式: {}, 内存使用: {}";
    public static final String REDIS_HEALTH_CHECK_FAILED = "[Redis] 健康检查失败 - 错误信息: {}";
    public static final String REDIS_PING_FAILED = "[Redis] ping响应异常 - 响应结果: {}";

    // ==================== 缓存操作相关日志 ====================
    public static final String CACHE_HIT = "[缓存] 命中 - Key: {}, 耗时: {}ms";
    public static final String CACHE_MISS = "[缓存] 未命中 - Key: {}, 将从数据库加载";
    public static final String CACHE_UPDATE = "[缓存] 更新 - Key: {}, 过期时间: {}秒";
    public static final String CACHE_EVICT = "[缓存] 删除 - Key: {}, 原因: {}";

    // ==================== 缓存预热相关日志 ====================
    public static final String CACHE_WARMUP_START = "[缓存] 开始预热 - 类型: {}";
    public static final String CACHE_WARMUP_END = "[缓存] 预热完成 - 类型: {}, 预热数量: {}, 耗时: {}ms";
    public static final String CACHE_WARMUP_FAILED = "[缓存] 预热失败 - 类型: {}, 错误信息: {}";
}
