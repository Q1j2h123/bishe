package com.oj.model.enums;

import lombok.Getter;

@Getter
public enum DifficultyEnum {
    EASY("简单"),
    MEDIUM("中等"),
    HARD("困难");

    private final String description;

    DifficultyEnum(String description) {
        this.description = description;
    }

    public static boolean contains(String difficulty) {
        for (DifficultyEnum difficultyEnum : DifficultyEnum.values()) {
            if (difficultyEnum.name().equals(difficulty)) {
                return true;
            }
        }
        return false;
    }
} 