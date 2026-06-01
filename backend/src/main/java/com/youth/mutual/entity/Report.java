package com.youth.mutual.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_report")
public class Report {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reporterId;
    /** 举报目标类型: skill / goods / activity / user */
    private String targetType;
    private Long targetId;
    /** 举报原因: 虚假信息 / 违规内容 / 欺诈行为 / 其他 */
    private String reason;
    private String description;
    /** 状态: pending / approved / rejected */
    private String status;
    private Long handlerId;
    private String handleNote;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // === 非数据库字段 ===
    @TableField(exist = false)
    private String reporterName;
    @TableField(exist = false)
    private String handlerName;
}
