package cn.xu.common.response;

import cn.xu.common.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 标准响应实体
 * 用于封装API接口的统一响应格式
 *
 * @param <T> 响应数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseEntity<T> implements Serializable {

    private Integer code;
    private String info;
    private T data;

    /**
     * 创建成功的响应实体（无数据）
     *
     * @param <T> 数据类型
     * @return 响应实体
     */
    public static <T> ResponseEntity<T> success() {
        return new ResponseEntity<>(ResponseCode.SUCCESS.getCode(), "success", null);
    }

    /**
     * 创建成功的响应实体（带数据）
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 响应实体
     */
    public static <T> ResponseEntity<T> success(T data) {
        return new ResponseEntity<>(ResponseCode.SUCCESS.getCode(), "success", data);
    }

    /**
     * 创建错误响应实体
     *
     * @param <T> 数据类型
     * @return 响应实体
     */
    public static <T> ResponseEntity<T> error() {
        return new ResponseEntity<>(ResponseCode.UN_ERROR.getCode(), "error", null);
    }

    /**
     * 创建错误响应实体（带错误信息）
     *
     * @param message 错误信息
     * @param <T>     数据类型
     * @return 响应实体
     */
    public static <T> ResponseEntity<T> error(String message) {
        return new ResponseEntity<>(ResponseCode.UN_ERROR.getCode(), message, null);
    }

}