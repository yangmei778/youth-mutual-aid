package com.youth.mutual.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 技能类型枚举
 */
@Getter
@AllArgsConstructor
public enum SkillTypeEnum {

    TEACH("teach", "我能教"),
    LEARN("learn", "我想学");

    private final String code;
    private final String description;
}
