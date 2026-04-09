package com.fitness.vo;

import lombok.Data;

/**
 * 登录响应VO
 */
@Data
public class LoginVO {
    private String token;
    private String username;
    private String role;
    private Long userId;
    private String name;
    private String avatar;
}
