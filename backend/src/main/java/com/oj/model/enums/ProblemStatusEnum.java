package com.oj.model.enums;

import lombok.Getter;

/**
 * 题目状态枚举
 */
@Getter
public enum ProblemStatusEnum {
    
    UNSOLVED("UNSOLVED", "未解决"),
    ATTEMPTED("ATTEMPTED", "尝试过"),
    SOLVED("SOLVED", "已解决");

    private final String value;
    private final String text;

    ProblemStatusEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }
    
    /**
     * 根据value获取枚举值
     * @param value
     * @return
     */
    public static ProblemStatusEnum getByValue(String value) {
        if (value == null) {
            return null;
        }
        for (ProblemStatusEnum status : ProblemStatusEnum.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
} 