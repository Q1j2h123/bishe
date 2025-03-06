package com.oj.model.enums;

import lombok.Getter;

@Getter
public enum PermissionEnum {
    PUBLIC("公开"),
    PRIVATE("私有"),
    SHARED("共享");

    private final String description;

    PermissionEnum(String description) {
        this.description = description;
    }

    public static boolean contains(String permission) {
        for (PermissionEnum permissionEnum : PermissionEnum.values()) {
            if (permissionEnum.name().equals(permission)) {
                return true;
            }
        }
        return false;
    }
} 