package com.oj.model.enums;

/**
 * 题库分类枚举
 */
public enum QuestionBankCategoryEnum {
    BASIC("BASIC", "基础"),
    PROJECT("PROJECT", "项目"),
    INTERVIEW("INTERVIEW", "面试");

    private final String text;
    private final String value;

    QuestionBankCategoryEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public static QuestionBankCategoryEnum getEnumByValue(String value) {
        if (value == null) {
            return null;
        }
        for (QuestionBankCategoryEnum anEnum : QuestionBankCategoryEnum.values()) {
            if (anEnum.getValue().equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
} 