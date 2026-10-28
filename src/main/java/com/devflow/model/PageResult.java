package com.devflow.model;

import java.util.List;

public class PageResult<T> {
    private final List<T> items;
    private final int total, page, size;

    public PageResult(List<T> items, int total, int page, int size) {
        this.items = items; this.total = total; this.page = page; this.size = size;
    }
    public List<T> getItems() { return items; }
    public int getTotal() { return total; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public int getTotalPages() { return Math.max(1, (int) Math.ceil(total / (double) size)); }
    public boolean isHasPrev() { return page > 1; }
    public boolean isHasNext() { return page < getTotalPages(); }
}
