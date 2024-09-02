package cn.xu.types.model;


import java.util.List;

public class PageResponse<T> {
    private List<T> items; // 分页数据内容
    private long total; // 总记录数
    private int pages; // 总页数
    private int page; // 当前页码
    private int size; // 每页记录数


    private boolean isLast; // 是否为最后一页
    private boolean isFirst; // 是否为第一页
    private int count; // 当前页的记录数

    public PageResponse() {

    }
    // 构造函数
    public PageResponse(List<T> items, long total, int pages, int page, int size, boolean isLast, boolean isFirst, int count) {
        this.items = items;
        this.total = total;
        this.pages = pages;
        this.page = page;
        this.size = size;
        this.isLast = isLast;
        this.isFirst = isFirst;
        this.count = count;
    }

    // 省略getter和setter方法

    // 内容列表
    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    // 总记录数
    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    // 总页数
    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

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

    // 是否为最后一页
    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean isLast) {
        this.isLast = isLast;
    }

    // 是否为第一页
    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean isFirst) {
        this.isFirst = isFirst;
    }

    // 当前页的记录数
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}

