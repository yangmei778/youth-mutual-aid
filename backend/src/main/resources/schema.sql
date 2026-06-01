-- Spring Boot 启动时自动执行，无需手动建表
CREATE TABLE IF NOT EXISTS t_activity_review (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '回顾ID',
    activity_id     BIGINT          NOT NULL COMMENT '活动ID',
    user_id         BIGINT          NOT NULL COMMENT '发布者ID',
    content         TEXT            DEFAULT NULL COMMENT '感受内容',
    images          VARCHAR(2000)   DEFAULT NULL COMMENT '图片URL列表',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_activity_id (activity_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_report (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '举报ID',
    reporter_id     BIGINT          NOT NULL COMMENT '举报人ID',
    target_type     VARCHAR(20)     NOT NULL COMMENT '举报目标类型',
    target_id       BIGINT          NOT NULL COMMENT '举报目标ID',
    reason          VARCHAR(50)     NOT NULL COMMENT '举报原因',
    description     VARCHAR(500)    DEFAULT NULL COMMENT '详细描述',
    status          VARCHAR(20)     DEFAULT 'pending' COMMENT '状态',
    handler_id      BIGINT          DEFAULT NULL COMMENT '处理人ID',
    handle_note     VARCHAR(500)    DEFAULT NULL COMMENT '处理备注',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_reporter (reporter_id),
    KEY idx_target (target_type, target_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_system_config (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    config_key      VARCHAR(100)    NOT NULL COMMENT '配置键',
    config_value    TEXT            DEFAULT NULL COMMENT '配置值（JSON）',
    description     VARCHAR(200)    DEFAULT NULL COMMENT '配置说明',
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
