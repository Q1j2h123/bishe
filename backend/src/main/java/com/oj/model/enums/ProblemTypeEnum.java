package com.oj.model.enums;

import lombok.Getter;

@Getter
public enum ProblemTypeEnum {
    CHOICE("选择题"),
    JUDGE("判断题"),
    PROGRAM("编程题");

    private final String description;

    ProblemTypeEnum(String description) {
        this.description = description;
    }

    public static boolean contains(String type) {
        for (ProblemTypeEnum typeEnum : ProblemTypeEnum.values()) {
            if (typeEnum.name().equals(type)) {
                return true;
            }
        }
        return false;
    }
} 