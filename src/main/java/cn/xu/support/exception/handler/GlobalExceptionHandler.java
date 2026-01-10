package cn.xu.support.exception.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.xu.common.ResponseCode;
import cn.xu.common.response.ResponseEntity;
import cn.xu.support.exception.BusinessException;
import cn.xu.support.log.BizLogger;
import cn.xu.support.log.LogConstants;
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

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * <p>统一处理系统中的各类异常并返回标准化响应</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Void> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        // 业务异常使用 WARN 级别
        BizLogger.of(log)
                .module(LogConstants.MODULE_SYSTEM)
                .op("业务异常")
                .param("path", request.getRequestURI())
                .param("method", request.getMethod())
                .param("code", ex.getCode())
                .warn(ex.getMessage());
        
        return ResponseEntity.<Void>builder()
                .code(ex.getCode())
                .info(ex.getMessage())
                .build();
    }

    // 全局异常拦截（拦截项目中的NotLoginException异常）
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<String> handlerNotLoginException(NotLoginException nle, HttpServletRequest request) {
        BizLogger.of(log)
                .module(LogConstants.MODULE_AUTH)
                .op("未登录")
                .param("path", request.getRequestURI())
                .param("type", nle.getType())
                .warn(nle.getMessage());

        // 判断场景值，定制化异常信息
        String message = "";
        if (nle.getType().equals(NotLoginException.NOT_TOKEN)) {
            message = "未能读取到有效token";
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
                .code(ResponseCode.TOKEN_EXPIRED.getCode())
                .info(message)
                .build();
    }
    /**
     * 处理请求参数缺失异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String parameterName = ex.getParameterName();
        String parameterType = ex.getParameterType();
        String errorMessage = String.format("缺少必要的请求参数 [%s]，参数类型 [%s]", parameterName, parameterType);

        BizLogger.of(log)
                .module(LogConstants.MODULE_SYSTEM)
                .op("参数缺失")
                .param("path", request.getRequestURI())
                .param("param", parameterName)
                .param("type", parameterType)
                .warn(errorMessage);

        return ResponseEntity.<String>builder()
                .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                .info(errorMessage)
                .build();
    }

    /**
     * 处理参数校验失败异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        BizLogger.of(log)
                .module(LogConstants.MODULE_SYSTEM)
                .op("参数校验失败")
                .param("path", request.getRequestURI())
                .param("errors", errors)
                .warn("请求参数校验失败");

        return ResponseEntity.<Map<String, String>>builder()
                .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                .info("请求参数校验失败")
                .data(errors)
                .build();
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String paramName = ex.getName();
        String expectedType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "未知";
        String actualValue = String.valueOf(ex.getValue());
        String errorMessage = String.format("参数类型转换失败: 参数 [%s] 应为 [%s] 类型，实际值为 [%s]",
                paramName, expectedType, actualValue);

        BizLogger.of(log)
                .module(LogConstants.MODULE_SYSTEM)
                .op("类型不匹配")
                .param("path", request.getRequestURI())
                .param("param", paramName)
                .param("expected", expectedType)
                .param("actual", actualValue)
                .warn(errorMessage);
        
        return ResponseEntity.<String>builder()
                .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                .info(errorMessage)
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        BizLogger.of(log)
                .module(LogConstants.MODULE_SYSTEM)
                .op("参数解析失败")
                .param("path", request.getRequestURI())
                .warn(ex.getMessage());
        
        return ResponseEntity.<String>builder()
                .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                .info("请求参数解析失败")
                .build();
    }

    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String supportedMethods = String.join(", ", ex.getSupportedMethods());
        String errorMessage = String.format("请求方法 '%s' 不支持，支持的方法: %s", ex.getMethod(), supportedMethods);
        
        BizLogger.of(log)
                .module(LogConstants.MODULE_SYSTEM)
                .op("方法不支持")
                .param("path", request.getRequestURI())
                .param("method", ex.getMethod())
                .param("supported", supportedMethods)
                .warn(errorMessage);
        
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

        BizLogger.of(log)
                .module(LogConstants.MODULE_SYSTEM)
                .op("参数绑定失败")
                .param("path", request.getRequestURI())
                .param("errors", errors)
                .warn("参数绑定失败");

        return ResponseEntity.<Map<String, String>>builder()
                .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                .info("参数绑定失败")
                .data(errors)
                .build();
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        String errorMessage = ex.getMessage();
        
        if (errorMessage.contains("allowCredentials") && errorMessage.contains("allowedOrigins")) {
            BizLogger.of(log)
                    .module(LogConstants.MODULE_SYSTEM)
                    .op("CORS配置错误")
                    .param("path", request.getRequestURI())
                    .error(errorMessage);
            return ResponseEntity.<String>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("跨域配置错误，请联系管理员")
                    .build();
        }
        
        BizLogger.of(log)
                .module(LogConstants.MODULE_SYSTEM)
                .op("参数异常")
                .param("path", request.getRequestURI())
                .warn(errorMessage);
        
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
        BizLogger.of(log)
                .module(LogConstants.MODULE_SYSTEM)
                .op("系统异常")
                .param("path", request.getRequestURI())
                .param("method", request.getMethod())
                .param("type", ex.getClass().getSimpleName())
                .error(ex.getMessage(), ex);
        
        return ResponseEntity.<String>builder()
                .code(ResponseCode.UN_ERROR.getCode())
                .info("系统异常，请联系管理员")
                .build();
    }
}