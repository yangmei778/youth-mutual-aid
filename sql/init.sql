-- ============================================================
-- 青年同城互助平台 - 数据库初始化脚本
-- 版本: v1.1
-- 日期: 2026-05-25
-- 变更: 移除实名认证字段，改用用户名登录，移除经纬度字段(简化定位)
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS mutual_aid DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE mutual_aid;

-- ============================================================
-- 1. 用户表
-- ============================================================
CREATE TABLE t_user (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username        VARCHAR(30)     NOT NULL COMMENT '用户名（登录账号）',
    password        VARCHAR(100)    NOT NULL COMMENT '密码（BCrypt加密）',
    nickname        VARCHAR(50)     NOT NULL COMMENT '昵称',
    avatar          VARCHAR(255)    DEFAULT NULL COMMENT '头像URL',
    gender          TINYINT         DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    age             TINYINT         DEFAULT NULL COMMENT '年龄',
    city            VARCHAR(50)     DEFAULT NULL COMMENT '所在城市',
    bio             VARCHAR(500)    DEFAULT NULL COMMENT '个人简介',
    credit_score    INT             DEFAULT 0 COMMENT '信用分',
    role            VARCHAR(20)     DEFAULT 'USER' COMMENT '角色：USER-普通用户，ADMIN-管理员',
    status          TINYINT         DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    last_login_time DATETIME        DEFAULT NULL COMMENT '最后登录时间',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT         DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_city (city),
    KEY idx_credit_score (credit_score),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- ============================================================
-- 2. 技能标签表
-- ============================================================
CREATE TABLE t_skill_tag (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    user_id         BIGINT          NOT NULL COMMENT '用户ID',
    type            VARCHAR(10)     NOT NULL COMMENT '类型：teach-我能教，learn-我想学',
    tag_name        VARCHAR(30)     NOT NULL COMMENT '标签名称',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_type_tag (type, tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='技能标签表';

-- ============================================================
-- 3. 技能发布表
-- ============================================================
CREATE TABLE t_skill_post (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '发布ID',
    user_id             BIGINT          NOT NULL COMMENT '发布者ID',
    type                VARCHAR(10)     NOT NULL COMMENT '类型：teach-我能教，learn-我想学',
    title               VARCHAR(100)    NOT NULL COMMENT '技能标题',
    description         TEXT            DEFAULT NULL COMMENT '详细描述',
    category            VARCHAR(30)     DEFAULT NULL COMMENT '技能分类',
    available_time      VARCHAR(200)    DEFAULT NULL COMMENT '可交换时间',
    preferred_location  VARCHAR(200)    DEFAULT NULL COMMENT '偏好地点',
    online_support      TINYINT         DEFAULT 0 COMMENT '是否支持线上：0-否，1-是',
    status              TINYINT         DEFAULT 1 COMMENT '状态：0-下架，1-上架',
    view_count          INT             DEFAULT 0 COMMENT '浏览次数',
    created_at          DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT         DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_type (type),
    KEY idx_category (category),
    KEY idx_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='技能发布表';

-- ============================================================
-- 4. 物品发布表
-- ============================================================
CREATE TABLE t_goods_post (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '发布ID',
    user_id         BIGINT          NOT NULL COMMENT '发布者ID',
    title           VARCHAR(100)    NOT NULL COMMENT '物品名称',
    description     TEXT            DEFAULT NULL COMMENT '物品描述',
    images          VARCHAR(2000)   DEFAULT NULL COMMENT '图片URL列表（逗号分隔）',
    category        VARCHAR(30)     DEFAULT NULL COMMENT '物品分类',
    condition_level TINYINT         DEFAULT 5 COMMENT '新旧程度：1-10分',
    exchange_type   VARCHAR(20)     NOT NULL COMMENT '交换方式：borrow-借用，gift-赠送，exchange-交换',
    expected_items  VARCHAR(500)    DEFAULT NULL COMMENT '期望交换的物品（exchange时填写）',
    borrow_days     INT             DEFAULT NULL COMMENT '借用天数（borrow时填写）',
    status          TINYINT         DEFAULT 1 COMMENT '状态：0-下架，1-可用，2-已借出，3-已送出',
    view_count      INT             DEFAULT 0 COMMENT '浏览次数',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT         DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_category (category),
    KEY idx_exchange_type (exchange_type),
    KEY idx_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='物品发布表';

-- ============================================================
-- 5. 活动发布表
-- ============================================================
CREATE TABLE t_activity_post (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '发布ID',
    user_id         BIGINT          NOT NULL COMMENT '发起人ID',
    type            VARCHAR(20)     NOT NULL COMMENT '活动类型：meal-拼饭，exhibition-拼展，travel-拼旅行，other-其他',
    title           VARCHAR(100)    NOT NULL COMMENT '活动标题',
    description     TEXT            DEFAULT NULL COMMENT '活动描述',
    activity_time   DATETIME        NOT NULL COMMENT '活动时间',
    end_time        DATETIME        DEFAULT NULL COMMENT '活动结束时间',
    location        VARCHAR(200)    NOT NULL COMMENT '活动地点',
    max_members     INT             DEFAULT 10 COMMENT '最大人数',
    current_members INT             DEFAULT 1 COMMENT '当前人数（含发起人）',
    cost_desc       VARCHAR(500)    DEFAULT NULL COMMENT '费用说明',
    status          TINYINT         DEFAULT 1 COMMENT '状态：0-取消，1-报名中，2-已满员，3-进行中，4-已结束',
    view_count      INT             DEFAULT 0 COMMENT '浏览次数',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT         DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_type (type),
    KEY idx_activity_time (activity_time),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='活动发布表';

-- ============================================================
-- 6. 活动参与者表
-- ============================================================
CREATE TABLE t_activity_member (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    activity_id     BIGINT          NOT NULL COMMENT '活动ID',
    user_id         BIGINT          NOT NULL COMMENT '参与者ID',
    status          TINYINT         DEFAULT 0 COMMENT '状态：0-待审核，1-已通过，2-已拒绝，3-已退出',
    apply_message   VARCHAR(200)    DEFAULT NULL COMMENT '申请留言',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_activity_user (activity_id, user_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='活动参与者表';

-- ============================================================
-- 7. 互助记录表
-- ============================================================
CREATE TABLE t_mutual_record (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    type                VARCHAR(20)     NOT NULL COMMENT '互助类型：skill-技能交换，goods-物品共享，activity-临时搭伴',
    initiator_id        BIGINT          NOT NULL COMMENT '发起人ID',
    participant_id      BIGINT          NOT NULL COMMENT '参与人ID',
    related_id          BIGINT          NOT NULL COMMENT '关联ID（技能/物品/活动发布ID）',
    request_message     VARCHAR(500)   DEFAULT NULL COMMENT '请求留言',
    accept_message      VARCHAR(500)   DEFAULT NULL COMMENT '接受留言',
    status              VARCHAR(20)     DEFAULT 'pending' COMMENT '状态：pending-待确认，ongoing-进行中，completed-已完成，cancelled-已取消，disputed-有争议',
    start_time          DATETIME        DEFAULT NULL COMMENT '开始时间',
    end_time            DATETIME        DEFAULT NULL COMMENT '结束时间',
    initiator_confirmed TINYINT         DEFAULT 0 COMMENT '发起人是否确认完成：0-否，1-是',
    participant_confirmed TINYINT       DEFAULT 0 COMMENT '参与人是否确认完成：0-否，1-是',
    created_at          DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_initiator (initiator_id),
    KEY idx_participant (participant_id),
    KEY idx_type_related (type, related_id),
    KEY idx_status (status),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='互助记录表';

-- ============================================================
-- 8. 评价表
-- ============================================================
CREATE TABLE t_review (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    record_id       BIGINT          NOT NULL COMMENT '互助记录ID',
    reviewer_id     BIGINT          NOT NULL COMMENT '评价人ID',
    reviewee_id     BIGINT          NOT NULL COMMENT '被评价人ID',
    rating          TINYINT         NOT NULL COMMENT '评分：1-5星',
    content         VARCHAR(500)    DEFAULT NULL COMMENT '评价内容',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_record_id (record_id),
    KEY idx_reviewee_id (reviewee_id),
    UNIQUE KEY uk_record_reviewer (record_id, reviewer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评价表';

-- ============================================================
-- 9. 信用变动记录表
-- ============================================================
CREATE TABLE t_credit_log (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    user_id         BIGINT          NOT NULL COMMENT '用户ID',
    change_value    INT             NOT NULL COMMENT '变动分值（正/负）',
    before_score    INT             NOT NULL COMMENT '变动前分值',
    after_score     INT             NOT NULL COMMENT '变动后分值',
    reason          VARCHAR(100)    NOT NULL COMMENT '变动原因',
    related_id      BIGINT          DEFAULT NULL COMMENT '关联记录ID',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='信用变动记录表';

-- ============================================================
-- 10. 私信消息表
-- ============================================================
CREATE TABLE t_message (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    sender_id       BIGINT          NOT NULL COMMENT '发送者ID',
    receiver_id     BIGINT          NOT NULL COMMENT '接收者ID',
    content         TEXT            NOT NULL COMMENT '消息内容',
    type            VARCHAR(20)     DEFAULT 'text' COMMENT '消息类型：text-文字，image-图片',
    is_read         TINYINT         DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_sender (sender_id),
    KEY idx_receiver (receiver_id),
    KEY idx_receiver_read (receiver_id, is_read),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='私信消息表';

-- ============================================================
-- 11. 系统通知表
-- ============================================================
CREATE TABLE t_notification (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    user_id         BIGINT          NOT NULL COMMENT '接收用户ID',
    title           VARCHAR(100)    NOT NULL COMMENT '通知标题',
    content         VARCHAR(500)    DEFAULT NULL COMMENT '通知内容',
    type            VARCHAR(30)     NOT NULL COMMENT '通知类型：mutual_request-互助请求，mutual_confirm-互助确认，mutual_complete-互助完成，credit_change-信用变动，system-系统通知',
    related_id      BIGINT          DEFAULT NULL COMMENT '关联ID',
    is_read         TINYINT         DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_user_read (user_id, is_read),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统通知表';

-- ============================================================
-- 12. 举报表
-- ============================================================
CREATE TABLE t_report (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '举报ID',
    reporter_id     BIGINT          NOT NULL COMMENT '举报人ID',
    target_type     VARCHAR(20)     NOT NULL COMMENT '举报对象类型：user-用户，skill-技能，goods-物品，activity-活动',
    target_id       BIGINT          NOT NULL COMMENT '举报对象ID',
    reason          VARCHAR(500)    NOT NULL COMMENT '举报原因',
    status          TINYINT         DEFAULT 0 COMMENT '处理状态：0-待处理，1-已处理-有效，2-已处理-无效',
    handle_result   VARCHAR(500)    DEFAULT NULL COMMENT '处理结果',
    handler_id      BIGINT          DEFAULT NULL COMMENT '处理人ID',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_reporter (reporter_id),
    KEY idx_target (target_type, target_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='举报表';

-- ============================================================
-- 初始数据
-- ============================================================

-- 插入管理员账号（密码: admin123，BCrypt加密）
INSERT INTO t_user (username, password, nickname, role, credit_score, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'ADMIN', 100, 1);

-- 技能分类初始数据（用于技能发布的 category 字段）
-- 实际开发中可放配置表，此处作为参考
-- 编程、语言、音乐、绘画、健身、烹饪、摄影、设计、写作、其他
