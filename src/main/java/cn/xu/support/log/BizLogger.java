package cn.xu.support.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 业务日志工具类
 * <p>
 * 提供统一的日志格式，支持链式调用
 * </p>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 简单用法
 * BizLogger.of(log).module("帖子").op("创建").success("postId", postId);
 * 
 * // 链式调用
 * BizLogger.of(log)
 *     .module("用户")
 *     .op("登录")
 *     .userId(userId)
 *     .param("ip", clientIp)
 *     .success();
 * 
 * // 失败日志
 * BizLogger.of(log)
 *     .module("帖子")
 *     .op("删除")
 *     .userId(userId)
 *     .param("postId", postId)
 *     .fail("无权限删除");
 * 
 * // 异常日志
 * BizLogger.of(log)
 *     .module("文件")
 *     .op("上传")
 *     .error("上传失败", e);
 * }</pre>
 * 
 * <h3>输出格式：</h3>
 * <pre>
 * [帖子] 创建成功 | postId=123, userId=456
 * [用户] 登录成功 | userId=123, ip=192.168.1.1
 * [帖子] 删除失败 | userId=123, postId=456 | 原因: 无权限删除
 * </pre>
 */
public class BizLogger {

    private final Logger logger;
    private String module;
    private String operation;
    private Long userId;
    private final Map<String, Object> params = new LinkedHashMap<>();

    private BizLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * 创建日志构建器
     * @param logger SLF4J Logger
     */
    public static BizLogger of(Logger logger) {
        return new BizLogger(logger);
    }

    /**
     * 创建日志构建器（通过类名）
     * @param clazz 类
     */
    public static BizLogger of(Class<?> clazz) {
        return new BizLogger(LoggerFactory.getLogger(clazz));
    }

    /**
     * 设置模块名称
     */
    public BizLogger module(String module) {
        this.module = module;
        return this;
    }

    /**
     * 设置操作类型
     */
    public BizLogger op(String operation) {
        this.operation = operation;
        return this;
    }

    /**
     * 设置用户ID
     */
    public BizLogger userId(Long userId) {
        this.userId = userId;
        if (userId != null) {
            this.params.put("userId", userId);
        }
        return this;
    }

    /**
     * 添加参数
     */
    public BizLogger param(String key, Object value) {
        if (key != null && value != null) {
            this.params.put(key, value);
        }
        return this;
    }

    /**
     * 添加多个参数
     */
    public BizLogger params(Map<String, Object> params) {
        if (params != null) {
            this.params.putAll(params);
        }
        return this;
    }

    // ==================== 日志输出方法 ====================

    /**
     * 记录成功日志（INFO级别）
     */
    public void success() {
        logger.info(buildMessage(LogConstants.RESULT_SUCCESS, null));
    }

    /**
     * 记录成功日志，附带额外参数
     */
    public void success(String key, Object value) {
        param(key, value);
        success();
    }

    /**
     * 记录成功日志，附带消息
     */
    public void successMsg(String message) {
        logger.info(buildMessage(LogConstants.RESULT_SUCCESS, message));
    }

    /**
     * 记录失败日志（WARN级别）
     */
    public void fail(String reason) {
        logger.warn(buildMessage(LogConstants.RESULT_FAIL, reason));
    }

    /**
     * 记录失败日志，附带额外参数
     */
    public void fail(String key, Object value, String reason) {
        param(key, value);
        fail(reason);
    }

    /**
     * 记录异常日志（ERROR级别）
     */
    public void error(String message, Throwable e) {
        logger.error(buildMessage(LogConstants.RESULT_ERROR, message), e);
    }

    /**
     * 记录异常日志（无异常对象）
     */
    public void error(String message) {
        logger.error(buildMessage(LogConstants.RESULT_ERROR, message));
    }

    /**
     * 记录DEBUG日志
     */
    public void debug(String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(buildMessage(null, message));
        }
    }

    /**
     * 记录INFO日志（自定义消息）
     */
    public void info(String message) {
        logger.info(buildMessage(null, message));
    }

    /**
     * 记录WARN日志（自定义消息）
     */
    public void warn(String message) {
        logger.warn(buildMessage(null, message));
    }

    // ==================== 私有方法 ====================

    /**
     * 构建日志消息
     * 格式: [模块] 操作结果 | 参数列表 | 原因/消息
     */
    private String buildMessage(String result, String extra) {
        StringBuilder sb = new StringBuilder();

        // [模块]
        if (module != null && !module.isEmpty()) {
            sb.append("[").append(module).append("] ");
        }

        // 操作 + 结果
        if (operation != null && !operation.isEmpty()) {
            sb.append(operation);
            if (result != null) {
                sb.append(result);
            }
        } else if (result != null) {
            sb.append(result);
        }

        // 参数列表
        if (!params.isEmpty()) {
            sb.append(" | ");
            StringJoiner joiner = new StringJoiner(", ");
            params.forEach((k, v) -> joiner.add(k + "=" + v));
            sb.append(joiner);
        }

        // 额外信息
        if (extra != null && !extra.isEmpty()) {
            sb.append(" | ").append(extra);
        }

        return sb.toString();
    }

    // ==================== 快捷静态方法 ====================

    /**
     * 快速记录成功日志
     */
    public static void logSuccess(Logger logger, String module, String op, String key, Object value) {
        of(logger).module(module).op(op).param(key, value).success();
    }

    /**
     * 快速记录失败日志
     */
    public static void logFail(Logger logger, String module, String op, String reason) {
        of(logger).module(module).op(op).fail(reason);
    }

    /**
     * 快速记录异常日志
     */
    public static void logError(Logger logger, String module, String op, String message, Throwable e) {
        of(logger).module(module).op(op).error(message, e);
    }
}
