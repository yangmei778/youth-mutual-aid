package com.youth.mutual.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物品发布实体
 */
@Data
@TableName("t_goods_post")
public class GoodsPost {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String images;
    private String category;
    private Integer conditionLevel;
    private String exchangeType;
    private String expectedItems;
    private Integer borrowDays;
    /** 预期价格（元），null表示不涉及金钱交易 */
    @TableField(exist = false)
    private java.math.BigDecimal price;
    /** 匿名发布：1=匿名，0=显示昵称 */
    private Integer isAnonymous;
    @TableField(exist = false)
    private String displayNickname;
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
