package com.youth.mutual.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 信用变动记录实体
 */
@Data
@TableName("t_credit_log")
public class CreditLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer changeValue;
    private Integer beforeScore;
    private Integer afterScore;
    private String reason;
    private Long relatedId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
