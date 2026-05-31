package com.youth.mutual.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 互助记录实体
 */
@Data
@TableName("t_mutual_record")
public class MutualRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String type;

    private Long initiatorId;

    private Long participantId;

    private Long relatedId;

    private String requestMessage;

    private String acceptMessage;

    private String status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer initiatorConfirmed;

    private Integer participantConfirmed;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
