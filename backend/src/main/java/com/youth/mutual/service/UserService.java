package com.youth.mutual.service;

import com.youth.mutual.common.result.PageResult;
import com.youth.mutual.dto.*;
import com.youth.mutual.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    void register(RegisterRequest request);

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 获取当前用户信息
     */
    UserInfoResponse getCurrentUserInfo();

    /**
     * 获取指定用户信息
     */
    UserInfoResponse getUserInfo(Long userId);

    /**
     * 更新个人资料
     */
    void updateProfile(Long userId, UpdateProfileRequest request);

    /**
     * 更新用户信用分
     */
    void updateCreditScore(Long userId, int changeValue);

    /**
     * 分页查询用户列表（管理后台）
     */
    PageResult<User> getUserList(int pageNum, int pageSize, String keyword, String city, Integer status);
}
