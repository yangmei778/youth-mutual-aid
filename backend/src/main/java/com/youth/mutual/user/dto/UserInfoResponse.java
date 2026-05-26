package com.youth.mutual.user.dto;

import lombok.Data;

/**
 * 用户信息响应
 */
@Data
public class UserInfoResponse {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer gender;
    private Integer age;
    private String city;
    private String bio;
    private Integer creditScore;
    private String role;
}
