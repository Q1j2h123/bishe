package com.oj.model.request;

import lombok.Data;

@Data
public class PageRequest {
    /**
     * 当前页号
     */
    private long current = 1;

    /**
     * 页面大小
     */
    private long pageSize = 10;

    /**
     * 页面大小限制
     */
    private static final long MAX_PAGE_SIZE = 100;

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize > MAX_PAGE_SIZE ? MAX_PAGE_SIZE : pageSize;
    }
} 