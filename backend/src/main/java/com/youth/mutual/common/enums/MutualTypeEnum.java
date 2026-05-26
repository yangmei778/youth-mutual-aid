package com.youth.mutual.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 互助类型枚举
 */
@Getter
@AllArgsConstructor
public enum MutualTypeEnum {

    SKILL_EXCHANGE("skill", "技能交换"),
    GOODS_SHARE("goods", "物品共享"),
    ACTIVITY_PARTNER("activity", "临时搭伴");

    private final String code;
    private final String description;
}
