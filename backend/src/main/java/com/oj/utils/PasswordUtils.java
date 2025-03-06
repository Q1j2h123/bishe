package com.oj.utils;

import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

@Component
public class PasswordUtils {
    
    /**
     * 密码加密
     * @param password 原始密码
     * @return 加密后的密码
     */
    public String encryptPassword(String password) {
        return DigestUtils.md5DigestAsHex(password.getBytes());
    }

    /**
     * 密码校验
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return encryptPassword(rawPassword).equals(encodedPassword);
    }
} 