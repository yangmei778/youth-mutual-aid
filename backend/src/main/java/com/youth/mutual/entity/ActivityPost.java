package com.youth.mutual.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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
    private Integer maxMembers;
    private Integer currentMembers;
    private String costDesc;
    @TableField(exist = false)
    private Integer minMembers;
    @TableField(exist = false)
    private String contact;
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

    /** 报名成员列表（非数据库字段，详情展示用） */
    @TableField(exist = false)
    private List<ActivityMember> members;
}
