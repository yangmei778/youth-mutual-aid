package com.youth.mutual.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动参与者实体
 */
@Data
@TableName("t_activity_member")
public class ActivityMember {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private Long userId;
    private Integer status;
    private String applyMessage;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 用户昵称（非数据库字段） */
    @TableField(exist = false)
    private String nickname;

    /** 用户头像（非数据库字段） */
    @TableField(exist = false)
    private String avatar;
}
