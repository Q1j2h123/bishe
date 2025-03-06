package com.oj.model.enums;

import lombok.Getter;

@Getter
public enum SubmissionStatusEnum {
    PENDING("待判题"),
    JUDGING("判题中"),
    ACCEPTED("通过"),
    WRONG_ANSWER("答案错误"),
    COMPILE_ERROR("编译错误"),
    RUNTIME_ERROR("运行时错误"),
    TIME_LIMIT_EXCEEDED("超时"),
    MEMORY_LIMIT_EXCEEDED("内存超限"),
    SYSTEM_ERROR("系统错误");

    private final String description;

    SubmissionStatusEnum(String description) {
        this.description = description;
    }
} 