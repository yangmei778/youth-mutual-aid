package com.youth.mutual.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long adminId;
    private String adminName;
    private String action;      // 操作类型: ban_user/unban_user/offline_post/delete_post/handle_report/adjust_credit
    private String target;      // 操作对象: "下架了技能#123" "封禁了用户@小明"
    private String detail;      // 详细信息
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
