package com.oj.model.enums;

/**
 * 题目类型枚举
 */
public enum ProblemTypeEnum {
    ALL("ALL", "全部"),
    CHOICE("CHOICE", "选择题"),
    JUDGE("JUDGE", "判断题"),
    PROGRAM("PROGRAM", "编程题");

    private final String text;
    private final String value;

    ProblemTypeEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public static ProblemTypeEnum getEnumByValue(String value) {
        if (value == null) {
            return null;
        }
        for (ProblemTypeEnum anEnum : ProblemTypeEnum.values()) {
            if (anEnum.getValue().equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
} 