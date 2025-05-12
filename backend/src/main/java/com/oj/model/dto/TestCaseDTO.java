package com.oj.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestCaseDTO implements Serializable {
    /**
     * 测试用例ID
     */
    private Long id;
    
    /**
     * 输入数据
     */
    private String input;

    /**
     * 期望输出
     */
    private String output;



    private static final long serialVersionUID = 1L;
}