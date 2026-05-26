package com.youth.mutual.skill.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 技能标签实体
 */
@Data
@TableName("t_skill_tag")
public class SkillTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String type;

    private String tagName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
