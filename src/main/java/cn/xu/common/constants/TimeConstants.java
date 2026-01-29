package cn.xu.common.constants;

import java.time.format.DateTimeFormatter;

/**
 * 时间常量
 * 
 * <p>定义系统中常用的时间常量（单位：秒）和日期格式化器</p>
 */
public final class TimeConstants {
    
    private TimeConstants() {
        // 防止实例化
    }
    
    // ==================== 日期格式化器 ====================
    
    /** 标准日期时间格式：yyyy-MM-dd HH:mm:ss */
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    
    /** 标准日期格式：yyyy-MM-dd */
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    
    /** 标准时间格式：HH:mm:ss */
    public static final String TIME_PATTERN = "HH:mm:ss";
    
    /** 年月格式：yyyyMM */
    public static final String YEAR_MONTH_PATTERN = "yyyyMM";
    
    /** 标准日期时间格式化器 */
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
    
    /** 标准日期格式化器 */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    
    /** 标准时间格式化器 */
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN);
    
    /** 年月格式化器 */
    public static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern(YEAR_MONTH_PATTERN);
    
    // ==================== 基础时间单位（秒） ====================
    
    /** 1分钟 */
    public static final long ONE_MINUTE = 60L;
    
    /** 5分钟 */
    public static final long FIVE_MINUTES = 5 * ONE_MINUTE;
    
    /** 10分钟 */
    public static final long TEN_MINUTES = 10 * ONE_MINUTE;
    
    /** 30分钟 */
    public static final long THIRTY_MINUTES = 30 * ONE_MINUTE;
    
    /** 1小时 */
    public static final long ONE_HOUR = 60 * ONE_MINUTE;
    
    /** 2小时 */
    public static final long TWO_HOURS = 2 * ONE_HOUR;
    
    /** 6小时 */
    public static final long SIX_HOURS = 6 * ONE_HOUR;
    
    /** 12小时 */
    public static final long TWELVE_HOURS = 12 * ONE_HOUR;
    
    /** 1天 */
    public static final long ONE_DAY = 24 * ONE_HOUR;
    
    /** 7天 */
    public static final long ONE_WEEK = 7 * ONE_DAY;
    
    /** 30天 */
    public static final long ONE_MONTH = 30 * ONE_DAY;
    
    // ==================== 业务相关时间 ====================
    
    /** 验证码有效期（5分钟） */
    public static final long VERIFICATION_CODE_EXPIRE = FIVE_MINUTES;
    
    /** 登录失败计数过期时间（1小时） */
    public static final long LOGIN_FAIL_COUNT_EXPIRE = ONE_HOUR;
    
    /** 密码重置令牌有效期（24小时） */
    public static final long PASSWORD_RESET_TOKEN_EXPIRE = ONE_DAY;
    
    /** IP封禁默认时长（1小时） */
    public static final long IP_BLOCK_DURATION = ONE_HOUR;
    
    /** 缓存默认过期时间（1小时） */
    public static final long CACHE_DEFAULT_EXPIRE = ONE_HOUR;
    
    /** 会话过期时间（7天） */
    public static final long SESSION_EXPIRE = ONE_WEEK;
    
    /** CORS预检请求有效期（1小时） */
    public static final long CORS_MAX_AGE = ONE_HOUR;
    
    // ==================== 毫秒单位 ====================
    
    /** 1秒（毫秒） */
    public static final long ONE_SECOND_MS = 1000L;
    
    /** 1分钟（毫秒） */
    public static final long ONE_MINUTE_MS = ONE_MINUTE * ONE_SECOND_MS;
    
    /** 1小时（毫秒） */
    public static final long ONE_HOUR_MS = ONE_HOUR * ONE_SECOND_MS;
    
    /** 1天（毫秒） */
    public static final long ONE_DAY_MS = ONE_DAY * ONE_SECOND_MS;
}
