package cn.xu.infrastructure.common.response;

import cn.xu.application.common.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseEntity<T> implements Serializable {

    private Integer code;
    private String info;
    private T data;

    public static <T> ResponseEntity<T> success() {
        return new ResponseEntity<>(ResponseCode.SUCCESS.getCode(), "success", null);
    }
    public static <T> ResponseEntity<T> success(T data) {
        return new ResponseEntity<>(ResponseCode.SUCCESS.getCode(), "success", data);
    }

    public static <T> ResponseEntity<T> fail(String message) {
        return new ResponseEntity<>(ResponseCode.UN_ERROR.getCode(), message, null);
    }
}