package com.oj.model.enums;

/**
 * 题库权限枚举
 */
public enum QuestionBankPermissionEnum {
    PUBLIC("PUBLIC", "公开"),
    PRIVATE("PRIVATE", "私有"),
    SHARED("SHARED", "共享");

    private final String text;
    private final String value;

    QuestionBankPermissionEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public static QuestionBankPermissionEnum getEnumByValue(String value) {
        if (value == null) {
            return null;
        }
        for (QuestionBankPermissionEnum anEnum : QuestionBankPermissionEnum.values()) {
            if (anEnum.getValue().equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
} 