package cn.xu.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class AppException extends RuntimeException {

    private static final long serialVersionUID = 5317680961212299217L;

    /**
     * 异常码
     */
    private Integer code;

    /**
     * 异常信息
     */
    private String message;

    public AppException(Integer code) {
        this.code = code;
    }

    public AppException(String message) {
        this.message = message;
    }

    public AppException(Integer code, Throwable cause) {
        this.code = code;
        super.initCause(cause);
    }

    public AppException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public AppException(Integer code, String message, Throwable cause) {
        this.code = code;
        this.message = message;
        super.initCause(cause);
    }


}
