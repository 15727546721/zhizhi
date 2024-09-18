package cn.xu.common;

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

    private static final long serialVersionUID = 5130392244064623509L;

    private Integer code;
    private String info;
    private T data;

}