package com.youth.mutual.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动回顾实体
 */
@Data
@TableName("t_activity_review")
public class ActivityReview {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private Long userId;
    private String content;
    /** 图片URL列表，逗号分隔 */
    private String images;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // === 非数据库字段 ===
    @TableField(exist = false)
    private String nickname;
    @TableField(exist = false)
    private String avatar;
}
