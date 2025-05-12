package com.oj.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

// 编程题提交请求
@Data
@EqualsAndHashCode(callSuper = true)
public class ProgramSubmissionRequest extends SubmissionRequest {
    private String language; // java, python, c++等
    private String code;
}