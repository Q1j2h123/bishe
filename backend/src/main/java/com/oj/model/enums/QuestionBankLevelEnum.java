package com.oj.model.enums;

/**
 * 题库难度等级枚举
 */
public enum QuestionBankLevelEnum {
    BEGINNER("BEGINNER", "入门"),
    INTERMEDIATE("INTERMEDIATE", "中级"),
    ADVANCED("ADVANCED", "高级");

    private final String text;
    private final String value;

    QuestionBankLevelEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public static QuestionBankLevelEnum getEnumByValue(String value) {
        if (value == null) {
            return null;
        }
        for (QuestionBankLevelEnum anEnum : QuestionBankLevelEnum.values()) {
            if (anEnum.getValue().equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
} 