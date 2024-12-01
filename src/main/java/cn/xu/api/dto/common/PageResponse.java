package cn.xu.api.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PageResponse<T> {
    private int page; // 当前页码，从1开始
    private int size; // 每页记录数
    private long total; // 总记录数
    private T data; // 结果数据
}
