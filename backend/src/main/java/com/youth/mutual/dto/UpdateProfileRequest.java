package com.youth.mutual.dto;

import lombok.Data;

/**
 * 更新个人资料请求
 */
@Data
public class UpdateProfileRequest {

    private String nickname;
    private String avatar;
    private Integer gender;
    private Integer age;
    private String city;
    private String bio;
}
