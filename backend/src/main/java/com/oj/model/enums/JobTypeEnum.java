package com.oj.model.enums;

/**
 * 岗位类型枚举
 */
public enum JobTypeEnum {
    FRONTEND("FRONTEND", "前端"),
    BACKEND("BACKEND", "后端"),
    ALGORITHM("ALGORITHM", "算法"),
    FULLSTACK("FULLSTACK", "全栈");

    private final String text;
    private final String value;

    JobTypeEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public static JobTypeEnum getEnumByValue(String value) {
        if (value == null) {
            return null;
        }
        for (JobTypeEnum anEnum : JobTypeEnum.values()) {
            if (anEnum.getValue().equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
} 