package com.oj.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestCaseDTO implements Serializable {
    /**
     * 输入数据
     */
    private String input;

    /**
     * 期望输出
     */
    private String output;

//    /**
//     * 是否为示例
//     */
//    private Boolean isExample;

    private static final long serialVersionUID = 1L;
}