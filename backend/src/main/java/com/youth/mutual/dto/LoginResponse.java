package com.youth.mutual.dto;

import lombok.Data;

/**
 * 登录响应
 */
@Data
public class LoginResponse {

    private String accessToken;

    private String refreshToken;

    private UserInfoResponse userInfo;

    public LoginResponse(String accessToken, String refreshToken, UserInfoResponse userInfo) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userInfo = userInfo;
    }
}
