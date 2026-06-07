package com.youth.mutual.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 技能发布实体
 */
@Data
@TableName("t_skill_post")
public class SkillPost {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String type;

    private String title;

    private String description;

    private String category;

    private String availableTime;

    private String preferredLocation;

    private Integer onlineSupport;
    private Integer isAnonymous;

    private Integer status;

    private Integer viewCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    /** 发布者昵称（非数据库字段，列表展示用） */
    @TableField(exist = false)
    private String nickname;

    /** 发布者头像（非数据库字段，列表展示用） */
    @TableField(exist = false)
    private String avatar;

    /** 发布者信用分（非数据库字段，列表展示用） */
    @TableField(exist = false)
    private Integer creditScore;
}
