package cn.xu.api.dto.common;

import lombok.Data;

@Data
public class PageRequest {
    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;
}

