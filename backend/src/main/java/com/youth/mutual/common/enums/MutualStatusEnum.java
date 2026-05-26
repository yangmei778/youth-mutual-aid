package com.youth.mutual.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 互助状态枚举
 */
@Getter
@AllArgsConstructor
public enum MutualStatusEnum {

    PENDING("pending", "待确认"),
    ONGOING("ongoing", "进行中"),
    COMPLETED("completed", "已完成"),
    CANCELLED("cancelled", "已取消"),
    DISPUTED("disputed", "有争议");

    private final String code;
    private final String description;
}
