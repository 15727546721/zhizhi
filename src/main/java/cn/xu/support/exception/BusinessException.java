package cn.xu.support.exception;

import cn.xu.common.ResponseCode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 业务异常类
 * 用于封装业务逻辑中的异常情况
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 5317680961212299217L;

    /**
     * 异常码
     */
    private Integer code;

    /**
     * 异常信息
     */
    private String message;

    public BusinessException(Integer code) {
        this.code = code;
    }

    public BusinessException(String message) {
        this.code = ResponseCode.UN_ERROR.getCode();
        this.message = message;
    }

    public BusinessException(Integer code, Throwable cause) {
        this.code = code;
        super.initCause(cause);
    }

    public BusinessException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        this.code = code;
        this.message = message;
        super.initCause(cause);
    }
}