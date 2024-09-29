package cn.xu.api.dto.common;

public class PageRequest {

    private int page; // 当前页码，从1开始
    private int size; // 每页记录数

    // 可选字段，用于排序
    private String sort; // 排序字段
    private String order; // 排序方向（asc 或 desc）

    // 构造函数
    public PageRequest() {
        // 默认值
        this.page = 1;
        this.size = 10;
        this.sort = null;
        this.order = "asc";
    }

    // 省略getter和setter方法

    // 当前页码
    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    // 每页记录数
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    // 排序字段
    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    // 排序方向
    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }
}

