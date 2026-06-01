package com.youth.mutual.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 私信消息实体
 */
@Data
@TableName("t_message")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private String type;
    private Integer isRead;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 发送者昵称（非数据库字段） */
    @TableField(exist = false)
    private String senderName;

    /** 发送者头像（非数据库字段） */
    @TableField(exist = false)
    private String senderAvatar;
}
