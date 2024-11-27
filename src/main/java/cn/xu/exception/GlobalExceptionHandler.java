package cn.xu.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.xu.common.Constants;
import cn.xu.common.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
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
    @ExceptionHandler(AppException.class)
    public ResponseEntity<String> handleAppExceptions(AppException ex) {
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
                .code(Constants.ResponseCode.ILLEGAL_PARAMETER.getCode())
                .info(ex.getMessage())
                .data(errors)
                .build();
    }

    // 全局异常拦截（拦截项目中的NotLoginException异常）
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<String> handlerNotLoginException(NotLoginException nle)
            throws Exception {
        log.error("未登录异常:{}", nle);
        // 打印堆栈，以供调试
        nle.printStackTrace();

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
                .code(Constants.ResponseCode.UN_ERROR.getCode())
                .info(message)
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
                .code(Constants.ResponseCode.UN_ERROR.getCode())
                .info(ex.getMessage())
                .build();
    }
}
