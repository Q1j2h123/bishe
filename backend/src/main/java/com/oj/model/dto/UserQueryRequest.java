package com.oj.model.dto;

import lombok.Data;

@Data
public class UserQueryRequest {
    private String userName;
    private String userAccount;
    private String createTimeOrder; // asc/desc
    private Integer current = 1;
    private Integer pageSize = 10;
} 