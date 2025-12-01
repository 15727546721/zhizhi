package cn.xu.support.exception.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.xu.common.ResponseCode;
import cn.xu.common.constant.UserErrorCode;
import cn.xu.common.response.ResponseEntity;
import cn.xu.support.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Void> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.error("业务异常: {} - 请求路径: {} - 请求方法: {}", 
                ex.getMessage(), request.getRequestURI(), request.getMethod(), ex);
        return ResponseEntity.<Void>builder()
                .code(ex.getCode())
                .info(ex.getMessage())
                .build();
    }

    // 全局异常拦截（拦截项目中的NotLoginException异常）
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<String> handlerNotLoginException(NotLoginException nle, HttpServletRequest request) {
        log.error("未登录异常: {} - 请求路径: {} - 请求方法: {} - 异常类型: {}", 
                nle.getMessage(), request.getRequestURI(), request.getMethod(), nle.getType());

        // 判断场景值，定制化异常信息
        String message = "";
        if (nle.getType().equals(NotLoginException.NOT_TOKEN)) {
            message = "未能读取到有效 token";
        } else if (nle.getType().equals(NotLoginException.INVALID_TOKEN)) {
            message = "token 无效";
        } else if (nle.getType().equals(NotLoginException.TOKEN_TIMEOUT)) {
            message = "token 已过期";
        } else if (nle.getType().equals(NotLoginException.BE_REPLACED)) {
            message = "token 已被顶下线";
        } else if (nle.getType().equals(NotLoginException.KICK_OUT)) {
            message = "token 已被踢下线";
        } else {
            message = "当前会话未登录";
        }

        // 返回给前端
        return ResponseEntity.<String>builder()
                .code(UserErrorCode.TOKEN_EXPIRED.getCode())
                .info(message)
                .build();
    }
    /**
     * 处理请求参数缺失异常
     *
     * @param ex 参数缺失异常
     * @return 统一响应对象
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        // 获取错误信息
        String parameterName = ex.getParameterName();
        String parameterType = ex.getParameterType();
        StackTraceElement[] stackTrace = ex.getStackTrace();
        String methodName = stackTrace[0].getMethodName();
        String className = stackTrace[0].getClassName();

        // 构建错误消息
        String errorMessage = String.format("缺少必需的请求参数 [%s]，参数类型 [%s]", parameterName, parameterType);

        // 记录详细日志
        log.error("缺少请求参数异常: {} - 请求路径: {} - 请求方法: {} - 异常位置: {}.{} - 参数信息: 名称 = {}, 类型 = {}", 
                ex.getMessage(), request.getRequestURI(), request.getMethod(), className, methodName, parameterName, parameterType);

        return ResponseEntity.<String>builder()
                .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                .info(errorMessage)
                .build();
    }

    /**
     * 处理参数校验失败异常
     *
     * @param ex 参数校验异常
     * @return 统一响应对象
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        // 获取所有字段错误
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // 获取方法信息
        String methodName = ex.getParameter().getMethod().getName();
        String className = ex.getParameter().getDeclaringClass().getName();

        // 记录详细日志
        log.error("参数校验失败异常: {} - 请求路径: {} - 请求方法: {} - 异常位置: {}.{} - 校验错误详情: {}", 
                ex.getMessage(), request.getRequestURI(), request.getMethod(), className, methodName, errors);

        return ResponseEntity.<Map<String, String>>builder()
                .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                .info("请求参数校验失败")
                .data(errors)
                .build();
    }

    /**
     * 处理参数类型不匹配异常
     *
     * @param ex 参数类型不匹配异常
     * @return 统一响应对象
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        // 获取错误信息
        String paramName = ex.getName();
        String expectedType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "未知";
        String actualValue = String.valueOf(ex.getValue());
        String methodName = ex.getParameter().getMethod().getName();
        String className = ex.getParameter().getDeclaringClass().getName();

        // 构建错误消息
        String errorMessage = String.format("参数类型转换失败: 参数 [%s] 应为 [%s] 类型，实际值为 [%s]",
                paramName, expectedType, actualValue);

        // 记录详细日志
        log.error("参数类型不匹配异常: {} - 请求路径: {} - 请求方法: {} - 异常位置: {}.{} - 参数信息: 名称 = {}, 期望类型 = {}, 实际值 = {}",
                ex.getMessage(), request.getRequestURI(), request.getMethod(), className, methodName, paramName, expectedType, actualValue, ex);
        
        return ResponseEntity.<String>builder()
                .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                .info(errorMessage)
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.error("参数解析异常: {} - 请求路径: {} - 请求方法: {}", ex.getMessage(), request.getRequestURI(), request.getMethod(), ex);
        return ResponseEntity.<String>builder()
                .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                .info("请求参数解析失败: " + ex.getMessage())
                .build();
    }

    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String supportedMethods = String.join(", ", ex.getSupportedMethods());
        String errorMessage = String.format("请求方法 '%s' 不支持，支持的方法: %s", ex.getMethod(), supportedMethods);
        
        log.error("请求方法不支持异常: {} - 请求路径: {} - 支持的方法: {}", 
                ex.getMessage(), request.getRequestURI(), supportedMethods);
        
        return ResponseEntity.<String>builder()
                .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                .info(errorMessage)
                .build();
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, String>> handleBindException(BindException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.error("参数绑定异常: {} - 请求路径: {} - 绑定错误: {}", 
                ex.getMessage(), request.getRequestURI(), errors);

        return ResponseEntity.<Map<String, String>>builder()
                .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                .info("参数绑定失败")
                .data(errors)
                .build();
    }

    /**
     * 处理CORS相关异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        String errorMessage = ex.getMessage();
        
        // 检查是否是CORS相关错误
        if (errorMessage.contains("allowCredentials") && errorMessage.contains("allowedOrigins")) {
            log.error("CORS配置错误: {} - 请求路径: {} - 建议使用allowedOriginPatterns替代allowedOrigins", 
                    errorMessage, request.getRequestURI());
            return ResponseEntity.<String>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("跨域配置错误，请联系管理员")
                    .build();
        }
        
        log.error("参数异常: {} - 请求路径: {}", errorMessage, request.getRequestURI());
        return ResponseEntity.<String>builder()
                .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                .info("参数错误: " + errorMessage)
                .build();
    }

    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleExceptions(Exception ex, HttpServletRequest request) {
        log.error("系统异常: {} - 请求路径: {} - 请求方法: {} - 异常类型: {}", 
                ex.getMessage(), request.getRequestURI(), request.getMethod(), ex.getClass().getSimpleName(), ex);
        
        return ResponseEntity.<String>builder()
                .code(ResponseCode.UN_ERROR.getCode())
                .info("系统异常，请联系管理员")
                .build();
    }
}
