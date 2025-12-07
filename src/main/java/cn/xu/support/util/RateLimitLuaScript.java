package cn.xu.support.util;

/**
 * 频控相关的Lua脚本
 * 
 * <p>使用Lua脚本保证Redis操作的原子性，避免竞态条件
 *
 */
public class RateLimitLuaScript {

    /**
     * 原子性频控Lua脚本
     * 
     * <p>逻辑：
     * 1. INCR增加计数器
     * 2. 如果是第一次（count=1），设置过期时间
     * 3. 返回当前计数值
     * 
     * <p>参数：
     * - KEYS[1]: Redis key
     * - ARGV[1]: 过期时间（秒）
     * 
     * <p>返回：当前计数值
     */
    public static final String ATOMIC_RATE_LIMIT_SCRIPT =
        "local key = KEYS[1] " +
        "local ttl = tonumber(ARGV[1]) " +
        "local current = redis.call('INCR', key) " +
        "if current == 1 then " +
        "  redis.call('EXPIRE', key, ttl) " +
        "end " +
        "return current";

    /**
     * 带限制的原子性频控Lua脚本
     * 
     * <p>逻辑：
     * 1. 检查当前计数是否超过限制
     * 2. 如果未超过，增加计数并设置过期时间（如果需要）
     * 3. 返回结果：{当前计数, 是否允许}
     * 
     * <p>参数：
     * - KEYS[1]: Redis key
     * - ARGV[1]: 限制数量
     * - ARGV[2]: 过期时间（秒）
     * 
     * <p>返回：{当前计数, 是否允许} 
     * - 示例：{3, 1} 表示当前计数3，允许执行
     * - 示例：{6, 0} 表示当前计数6，不允许执行
     */
    public static final String ATOMIC_RATE_LIMIT_WITH_CHECK_SCRIPT = 
        "local key = KEYS[1] " +
        "local limit = tonumber(ARGV[1]) " +
        "local ttl = tonumber(ARGV[2]) " +
        "local current = redis.call('GET', key) " +
        "if current == false then current = 0 else current = tonumber(current) end " +
        "if current >= limit then return {current, 0} end " +
        "current = redis.call('INCR', key) " +
        "if current == 1 then redis.call('EXPIRE', key, ttl) end " +
        "return {current, 1}";

    /**
     * 滑动窗口频控Lua脚本（更精确的频控机制）
     * 
     * <p>逻辑：
     * 1. 使用ZSET存储时间戳
     * 2. 移除窗口外的记录
     * 3. 检查窗口内记录数量
     * 4. 如果未超过限制，添加当前时间戳
     * 
     * <p>参数：
     * - KEYS[1]: Redis key (ZSET)
     * - ARGV[1]: 当前时间戳（毫秒）
     * - ARGV[2]: 窗口大小（毫秒）
     * - ARGV[3]: 限制数量
     * - ARGV[4]: 过期时间（秒，用于清理整个key）
     * 
     * <p>返回：{窗口内计数, 是否允许}
     */
    public static final String SLIDING_WINDOW_RATE_LIMIT_SCRIPT = 
        "local key = KEYS[1] " +
        "local now = tonumber(ARGV[1]) " +
        "local window = tonumber(ARGV[2]) " +
        "local limit = tonumber(ARGV[3]) " +
        "local expire = tonumber(ARGV[4]) " +
        "local min_time = now - window " +
        "redis.call('ZREMRANGEBYSCORE', key, 0, min_time) " +
        "local current = redis.call('ZCARD', key) " +
        "if current >= limit then return {current, 0} end " +
        "redis.call('ZADD', key, now, now) " +
        "redis.call('EXPIRE', key, expire) " +
        "return {current + 1, 1}";

    /**
     * 用户级别频控检查脚本
     * 
     * <p>检查用户在指定时间窗口内的操作次数
     * 支持多个时间窗口的组合检查（如1分钟5次，1小时20次）
     */
    public static final String USER_MULTI_WINDOW_RATE_LIMIT_SCRIPT = 
        "local user_id = ARGV[1] " +
        "local now = tonumber(ARGV[2]) " +
        "local windows = {{60, 5}, {3600, 20}, {86400, 100}} " +
        "for i, window_config in ipairs(windows) do " +
        "  local window_seconds = window_config[1] " +
        "  local limit = window_config[2] " +
        "  local window_key = 'pm:rate:' .. user_id .. ':' .. window_seconds " +
        "  local min_time = now - (window_seconds * 1000) " +
        "  redis.call('ZREMRANGEBYSCORE', window_key, 0, min_time) " +
        "  local current = redis.call('ZCARD', window_key) " +
        "  if current >= limit then return {i, current, limit, 0} end " +
        "end " +
        "for i, window_config in ipairs(windows) do " +
        "  local window_seconds = window_config[1] " +
        "  local window_key = 'pm:rate:' .. user_id .. ':' .. window_seconds " +
        "  redis.call('ZADD', window_key, now, now) " +
        "  redis.call('EXPIRE', window_key, window_seconds + 60) " +
        "end " +
        "return {0, 0, 0, 1}";
}