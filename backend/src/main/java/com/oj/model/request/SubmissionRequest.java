package com.oj.model.request;

import lombok.Data;

// 基础提交请求
@Data
public class SubmissionRequest {
    private Long problemId;
    private String type; // CHOICE, JUDGE, PROGRAM
}