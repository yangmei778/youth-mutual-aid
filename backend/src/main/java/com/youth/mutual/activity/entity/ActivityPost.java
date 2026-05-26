package com.youth.mutual.activity.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动发布实体
 */
@Data
@TableName("t_activity_post")
public class ActivityPost {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String description;
    private LocalDateTime activityTime;
    private LocalDateTime endTime;
    private String location;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer maxMembers;
    private Integer currentMembers;
    private String costDesc;
    private Integer status;
    private Integer viewCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
