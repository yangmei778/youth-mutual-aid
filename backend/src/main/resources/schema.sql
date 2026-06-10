-- ============================================================
--  Spring Boot 启动时自动执行
--  所有表使用 IF NOT EXISTS，已有表不会重复创建
-- ============================================================

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username        VARCHAR(50)  NOT NULL                COMMENT '用户名',
    password        VARCHAR(255) NOT NULL                COMMENT '密码（BCrypt加密）',
    nickname        VARCHAR(50)  DEFAULT NULL            COMMENT '昵称',
    avatar          VARCHAR(500) DEFAULT NULL            COMMENT '头像URL',
    gender          TINYINT      DEFAULT 0               COMMENT '性别: 0=未知 1=男 2=女',
    age             INT          DEFAULT NULL            COMMENT '年龄',
    city            VARCHAR(50)  DEFAULT NULL            COMMENT '所在城市',
    bio             VARCHAR(500) DEFAULT NULL            COMMENT '个人简介',
    credit_score    INT          DEFAULT 80              COMMENT '信用分',
    role            VARCHAR(20)  DEFAULT 'USER'          COMMENT '角色: USER/ADMIN',
    status          TINYINT      DEFAULT 1               COMMENT '状态: 1=正常 0=封禁',
    last_login_time DATETIME     DEFAULT NULL            COMMENT '最后登录时间',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT      DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_status (status),
    KEY idx_credit_score (credit_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 技能发布表
CREATE TABLE IF NOT EXISTS t_skill_post (
    id                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '技能ID',
    user_id            BIGINT       NOT NULL                COMMENT '发布者ID',
    type               VARCHAR(10)  DEFAULT 'teach'         COMMENT '类型: teach/learn',
    title              VARCHAR(100) NOT NULL                COMMENT '标题',
    description        TEXT         DEFAULT NULL            COMMENT '描述',
    category           VARCHAR(50)  DEFAULT NULL            COMMENT '分类',
    available_time     VARCHAR(100) DEFAULT NULL            COMMENT '可用时间',
    preferred_location VARCHAR(100) DEFAULT NULL            COMMENT '期望地点',
    online_support     TINYINT      DEFAULT 0               COMMENT '支持线上',
    is_anonymous       TINYINT      DEFAULT 0               COMMENT '匿名发布',
    status             TINYINT      DEFAULT 1               COMMENT '状态: 1=正常 0=已下架',
    view_count         INT          DEFAULT 0               COMMENT '浏览次数',
    created_at         DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at         DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted            TINYINT      DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_type (type),
    KEY idx_category (category),
    KEY idx_status (status),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 物品共享表
CREATE TABLE IF NOT EXISTS t_goods_post (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '物品ID',
    user_id          BIGINT       NOT NULL                COMMENT '发布者ID',
    title            VARCHAR(100) NOT NULL                COMMENT '标题',
    description      TEXT         DEFAULT NULL            COMMENT '描述',
    images           VARCHAR(2000)DEFAULT NULL            COMMENT '图片URL列表',
    category         VARCHAR(50)  DEFAULT NULL            COMMENT '分类',
    condition_level  INT          DEFAULT 5               COMMENT '新旧程度 1-10',
    exchange_type    VARCHAR(20)  DEFAULT NULL            COMMENT '交换方式: borrow/gift/exchange',
    expected_items   VARCHAR(500) DEFAULT NULL            COMMENT '期望交换物品',
    borrow_days      INT          DEFAULT NULL            COMMENT '借用天数',
    is_anonymous     TINYINT      DEFAULT 0               COMMENT '匿名发布',
    status           TINYINT      DEFAULT 1               COMMENT '状态: 1=正常 0=已下架',
    view_count       INT          DEFAULT 0               COMMENT '浏览次数',
    created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          TINYINT      DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_category (category),
    KEY idx_status (status),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 活动发布表
CREATE TABLE IF NOT EXISTS t_activity_post (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '活动ID',
    user_id          BIGINT       NOT NULL                COMMENT '发布者ID',
    type             VARCHAR(20)  DEFAULT NULL            COMMENT '类型: meal/exhibition/travel/other',
    title            VARCHAR(100) NOT NULL                COMMENT '标题',
    description      TEXT         DEFAULT NULL            COMMENT '描述',
    activity_time    DATETIME     DEFAULT NULL            COMMENT '活动时间',
    end_time         DATETIME     DEFAULT NULL            COMMENT '结束时间',
    location         VARCHAR(200) DEFAULT NULL            COMMENT '地点',
    max_members      INT          DEFAULT 10              COMMENT '最大人数',
    current_members  INT          DEFAULT 0               COMMENT '当前人数',
    cost_desc        VARCHAR(200) DEFAULT NULL            COMMENT '费用说明',
    is_anonymous     TINYINT      DEFAULT 0               COMMENT '匿名发布',
    status           TINYINT      DEFAULT 1               COMMENT '状态: 1=正常 0=已下架',
    view_count       INT          DEFAULT 0               COMMENT '浏览次数',
    created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          TINYINT      DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_type (type),
    KEY idx_activity_time (activity_time),
    KEY idx_status (status),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 活动报名成员
CREATE TABLE IF NOT EXISTS t_activity_member (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '报名ID',
    activity_id    BIGINT       NOT NULL                COMMENT '活动ID',
    user_id        BIGINT       NOT NULL                COMMENT '用户ID',
    status         TINYINT      DEFAULT 0               COMMENT '状态: -1=拒绝 0=待审核 1=通过',
    apply_message  VARCHAR(500) DEFAULT NULL            COMMENT '报名留言',
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_activity_id (activity_id),
    KEY idx_user_id (user_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 活动回顾
CREATE TABLE IF NOT EXISTS t_activity_review (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '回顾ID',
    activity_id BIGINT       NOT NULL                COMMENT '活动ID',
    user_id     BIGINT       NOT NULL                COMMENT '发布者ID',
    content     TEXT         DEFAULT NULL            COMMENT '内容',
    images      VARCHAR(2000)DEFAULT NULL            COMMENT '图片URL列表',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_activity_id (activity_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 技能标签
CREATE TABLE IF NOT EXISTS t_skill_tag (
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    user_id    BIGINT      NOT NULL                COMMENT '用户ID',
    type       VARCHAR(10) NOT NULL                COMMENT '类型: teach/learn',
    tag_name   VARCHAR(50) NOT NULL                COMMENT '标签名',
    created_at DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 互助记录
CREATE TABLE IF NOT EXISTS t_mutual_record (
    id                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    type                  VARCHAR(20)  NOT NULL                COMMENT '类型: skill/goods/activity',
    initiator_id          BIGINT       NOT NULL                COMMENT '发起者ID',
    participant_id        BIGINT       NOT NULL                COMMENT '参与者ID',
    related_id            BIGINT       DEFAULT NULL            COMMENT '关联内容ID',
    request_message       VARCHAR(500) DEFAULT NULL            COMMENT '请求留言',
    accept_message        VARCHAR(500) DEFAULT NULL            COMMENT '接受留言',
    status                VARCHAR(20)  DEFAULT 'pending'       COMMENT '状态',
    start_time            DATETIME     DEFAULT NULL            COMMENT '开始时间',
    end_time              DATETIME     DEFAULT NULL            COMMENT '结束时间',
    initiator_confirmed   TINYINT      DEFAULT 0               COMMENT '发起者确认',
    participant_confirmed TINYINT      DEFAULT 0               COMMENT '参与者确认',
    created_at            DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at            DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_initiator (initiator_id),
    KEY idx_participant (participant_id),
    KEY idx_related (related_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 评价
CREATE TABLE IF NOT EXISTS t_review (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    record_id   BIGINT       DEFAULT NULL            COMMENT '互助记录ID',
    reviewer_id BIGINT       NOT NULL                COMMENT '评价人ID',
    reviewee_id BIGINT       NOT NULL                COMMENT '被评价人ID',
    rating      INT          NOT NULL                COMMENT '评分 1-5',
    content     VARCHAR(500) DEFAULT NULL            COMMENT '评价内容',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_record_id (record_id),
    KEY idx_reviewer (reviewer_id),
    KEY idx_reviewee (reviewee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 信用变动记录
CREATE TABLE IF NOT EXISTS t_credit_log (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    user_id      BIGINT       NOT NULL                COMMENT '用户ID',
    change_value INT          NOT NULL                COMMENT '变动分值',
    before_score INT          DEFAULT NULL            COMMENT '变动前',
    after_score  INT          DEFAULT NULL            COMMENT '变动后',
    reason       VARCHAR(200) DEFAULT NULL            COMMENT '原因',
    related_id   BIGINT       DEFAULT NULL            COMMENT '关联ID',
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 私信消息
CREATE TABLE IF NOT EXISTS t_message (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    sender_id   BIGINT       NOT NULL                COMMENT '发送者ID',
    receiver_id BIGINT       NOT NULL                COMMENT '接收者ID',
    content     TEXT         NOT NULL                COMMENT '内容',
    type        VARCHAR(20)  DEFAULT 'text'          COMMENT '类型',
    is_read     TINYINT      DEFAULT 0               COMMENT '已读',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (id),
    KEY idx_sender (sender_id),
    KEY idx_receiver (receiver_id),
    KEY idx_is_read (is_read),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 系统通知
CREATE TABLE IF NOT EXISTS t_notification (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    user_id    BIGINT       NOT NULL                COMMENT '接收者ID',
    title      VARCHAR(100) NOT NULL                COMMENT '标题',
    content    VARCHAR(500) DEFAULT NULL            COMMENT '内容',
    type       VARCHAR(50)  DEFAULT NULL            COMMENT '类型',
    related_id BIGINT       DEFAULT NULL            COMMENT '关联ID',
    is_read    TINYINT      DEFAULT 0               COMMENT '已读',
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_type (type),
    KEY idx_is_read (is_read),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 举报
CREATE TABLE IF NOT EXISTS t_report (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '举报ID',
    reporter_id BIGINT       NOT NULL                COMMENT '举报人ID',
    target_type VARCHAR(20)  NOT NULL                COMMENT '目标类型',
    target_id   BIGINT       NOT NULL                COMMENT '目标ID',
    reason      VARCHAR(50)  NOT NULL                COMMENT '原因',
    description VARCHAR(500) DEFAULT NULL            COMMENT '描述',
    status      VARCHAR(20)  DEFAULT 'pending'       COMMENT '状态',
    handler_id  BIGINT       DEFAULT NULL            COMMENT '处理人ID',
    handle_note VARCHAR(500) DEFAULT NULL            COMMENT '处理备注',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_reporter (reporter_id),
    KEY idx_target (target_type, target_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 系统配置
CREATE TABLE IF NOT EXISTS t_system_config (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    config_key   VARCHAR(100) NOT NULL                COMMENT '配置键',
    config_value TEXT         DEFAULT NULL            COMMENT '配置值(JSON)',
    description  VARCHAR(200) DEFAULT NULL            COMMENT '说明',
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 管理员操作日志
CREATE TABLE IF NOT EXISTS t_operation_log (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    admin_id   BIGINT       NOT NULL                COMMENT '管理员ID',
    admin_name VARCHAR(50)  DEFAULT NULL            COMMENT '管理员名',
    action     VARCHAR(30)  NOT NULL                COMMENT '操作类型',
    target     VARCHAR(200) DEFAULT NULL            COMMENT '操作对象',
    detail     VARCHAR(500) DEFAULT NULL            COMMENT '详情',
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_admin (admin_id),
    KEY idx_action (action),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
