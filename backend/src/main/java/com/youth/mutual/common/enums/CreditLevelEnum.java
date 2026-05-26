package com.youth.mutual.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 信用等级枚举
 */
@Getter
@AllArgsConstructor
public enum CreditLevelEnum {

    NEWBIE(0, 20, "新手", "🌱"),
    BRONZE(21, 50, "铜牌", "🥉"),
    SILVER(51, 100, "银牌", "🥈"),
    GOLD(101, 200, "金牌", "🥇"),
    DIAMOND(201, Integer.MAX_VALUE, "钻石", "💎");

    private final int minScore;
    private final int maxScore;
    private final String name;
    private final String badge;

    /**
     * 根据信用分获取等级
     */
    public static CreditLevelEnum getByScore(int score) {
        for (CreditLevelEnum level : values()) {
            if (score >= level.minScore && score <= level.maxScore) {
                return level;
            }
        }
        return NEWBIE;
    }
}
