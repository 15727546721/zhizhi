package cn.xu.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.xu.common.ResponseEntity;
import cn.xu.domain.user.constant.UserErrorCode;
import cn.xu.infrastructure.common.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleAppExceptions(BusinessException ex) {
        log.error("业务异常:{}", ex);
        return ResponseEntity.<String>builder()
                .code(ex.getCode())
                .info(ex.getMessage())
                .build();
    }

    /**
     * 处理参数校验异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("参数校验异常:{}", ex);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.<Map<String, String>>builder()
                .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                .info(ex.getMessage())
                .data(errors)
                .build();
    }

    // 全局异常拦截（拦截项目中的NotLoginException异常）
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<String> handlerNotLoginException(NotLoginException nle) {
        log.error("未登录异常:{}", nle);

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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error("参数解析异常: {}", ex);
        return ResponseEntity.<String>builder()
                .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                .info(ex.getMessage())
                .build();
    }

    /**
     * 处理全局异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleExceptions(Exception ex) {
        log.error("全局异常:{}", ex);
        return ResponseEntity.<String>builder()
                .code(ResponseCode.UN_ERROR.getCode())
                .info(ex.getMessage())
                .build();
    }
}
