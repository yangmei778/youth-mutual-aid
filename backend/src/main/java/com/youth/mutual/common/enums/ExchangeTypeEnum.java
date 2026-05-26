package com.youth.mutual.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 物品交换方式枚举
 */
@Getter
@AllArgsConstructor
public enum ExchangeTypeEnum {

    BORROW("borrow", "借用"),
    GIFT("gift", "赠送"),
    EXCHANGE("exchange", "交换");

    private final String code;
    private final String description;
}
