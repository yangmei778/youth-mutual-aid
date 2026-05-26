package com.youth.mutual.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // ===== 通用 =====
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),

    // ===== 认证相关 1xxx =====
    UNAUTHORIZED(1001, "未登录或Token已过期"),
    TOKEN_INVALID(1002, "无效的Token"),
    TOKEN_EXPIRED(1003, "Token已过期"),
    LOGIN_FAIL(1004, "登录失败，用户名或密码错误"),
    REGISTER_FAIL(1005, "注册失败"),
    ACCOUNT_DISABLED(1006, "账号已被禁用"),

    // ===== 参数相关 2xxx =====
    PARAM_ERROR(2001, "参数错误"),
    PARAM_MISSING(2002, "缺少必要参数"),
    PARAM_TYPE_ERROR(2003, "参数类型错误"),

    // ===== 用户相关 3xxx =====
    USER_NOT_FOUND(3001, "用户不存在"),
    USER_ALREADY_EXISTS(3002, "用户已存在"),
    USERNAME_ALREADY_EXISTS(3003, "用户名已被注册"),

    // ===== 业务相关 4xxx =====
    SKILL_POST_NOT_FOUND(4001, "技能发布不存在"),
    GOODS_POST_NOT_FOUND(4002, "物品发布不存在"),
    ACTIVITY_NOT_FOUND(4003, "活动不存在"),
    MUTUAL_RECORD_NOT_FOUND(4004, "互助记录不存在"),
    MUTUAL_REQUEST_DUPLICATE(4005, "重复的互助请求"),
    MUTUAL_STATUS_ERROR(4006, "互助状态异常"),
    ACTIVITY_FULL(4007, "活动人数已满"),
    ACTIVITY_ALREADY_JOINED(4008, "已报名该活动"),

    // ===== 信用相关 5xxx =====
    CREDIT_TOO_LOW(5001, "信用分过低，功能受限"),
    CREDIT_LOG_NOT_FOUND(5002, "信用记录不存在"),

    // ===== 文件相关 6xxx =====
    FILE_UPLOAD_FAIL(6001, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(6002, "不支持的文件类型"),
    FILE_SIZE_EXCEEDED(6003, "文件大小超限"),

    // ===== 权限相关 7xxx =====
    ACCESS_DENIED(7001, "无权限访问"),
    ADMIN_REQUIRED(7002, "需要管理员权限");

    private final Integer code;
    private final String message;
}
