package com.oj.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页请求
 */
@Data
@ApiModel(description = "分页请求")
public class PageRequest implements Serializable {
    /**
     * 当前页号
     */
    @ApiModelProperty(value = "当前页号", example = "1")
    private long current = 1;

    /**
     * 页面大小
     */
    @ApiModelProperty(value = "每页条数", example = "10")
    private long pageSize = 10;

    /**
     * 排序字段
     */
    @ApiModelProperty(value = "排序字段", example = "createTime")
    private String sortField;

    /**
     * 排序顺序（默认降序）
     */
    @ApiModelProperty(value = "排序顺序（升序：ascend，降序：descend）", example = "descend")
    private String sortOrder = "descend";

    /**
     * 页面大小限制
     */
    private static final long MAX_PAGE_SIZE = 100;

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize > MAX_PAGE_SIZE ? MAX_PAGE_SIZE : pageSize;
    }
    
    private static final long serialVersionUID = 1L;
} 