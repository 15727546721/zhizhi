package cn.xu.infrastructure.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
