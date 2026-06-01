package com.youth.mutual.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_system_config")
public class SystemConfig {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String configKey;
    /** JSON格式配置值 */
    private String configValue;
    private String description;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
