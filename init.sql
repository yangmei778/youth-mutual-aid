-- ============================================================
--  青年同城互助平台 - 数据库初始化脚本
--  使用方法: mysql -u root -p < init.sql
--  会自动创建数据库和所有表，并插入管理员账号
-- ============================================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS `mutual_aid`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE `mutual_aid`;

-- ============================================================
--  核心业务表
-- ============================================================

-- 用户表
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`        VARCHAR(50)  NOT NULL                COMMENT '用户名',
    `password`        VARCHAR(255) NOT NULL                COMMENT '密码（BCrypt加密）',
    `nickname`        VARCHAR(50)  DEFAULT NULL            COMMENT '昵称',
    `avatar`          VARCHAR(500) DEFAULT NULL            COMMENT '头像URL',
    `gender`          TINYINT      DEFAULT 0               COMMENT '性别: 0=未知 1=男 2=女',
    `age`             INT          DEFAULT NULL            COMMENT '年龄',
    `city`            VARCHAR(50)  DEFAULT NULL            COMMENT '所在城市',
    `bio`             VARCHAR(500) DEFAULT NULL            COMMENT '个人简介',
    `credit_score`    INT          DEFAULT 80              COMMENT '信用分（默认80）',
    `role`            VARCHAR(20)  DEFAULT 'USER'          COMMENT '角色: USER / ADMIN',
    `status`          TINYINT      DEFAULT 1               COMMENT '状态: 1=正常 0=封禁',
    `last_login_time` DATETIME     DEFAULT NULL            COMMENT '最后登录时间',
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT      DEFAULT 0               COMMENT '逻辑删除: 0=正常 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_status` (`status`),
    KEY `idx_credit_score` (`credit_score`),
    KEY `idx_city` (`city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 技能发布表
DROP TABLE IF EXISTS `t_skill_post`;
CREATE TABLE `t_skill_post` (
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '技能ID',
    `user_id`            BIGINT       NOT NULL                COMMENT '发布者ID',
    `type`               VARCHAR(10)  DEFAULT 'teach'         COMMENT '类型: teach(能教) / learn(想学)',
    `title`              VARCHAR(100) NOT NULL                COMMENT '标题',
    `description`        TEXT         DEFAULT NULL            COMMENT '详细描述',
    `category`           VARCHAR(50)  DEFAULT NULL            COMMENT '技能分类',
    `available_time`     VARCHAR(100) DEFAULT NULL            COMMENT '可用时间描述',
    `preferred_location` VARCHAR(100) DEFAULT NULL            COMMENT '期望地点',
    `online_support`     TINYINT      DEFAULT 0               COMMENT '支持线上: 0=否 1=是',
    `is_anonymous`       TINYINT      DEFAULT 0               COMMENT '匿名发布: 0=否 1=是',
    `status`             TINYINT      DEFAULT 1               COMMENT '状态: 1=正常 0=已下架',
    `view_count`         INT          DEFAULT 0               COMMENT '浏览次数',
    `created_at`         DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`         DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`            TINYINT      DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能发布表';

-- 物品共享表
DROP TABLE IF EXISTS `t_goods_post`;
CREATE TABLE `t_goods_post` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '物品ID',
    `user_id`          BIGINT       NOT NULL                COMMENT '发布者ID',
    `title`            VARCHAR(100) NOT NULL                COMMENT '标题',
    `description`      TEXT         DEFAULT NULL            COMMENT '描述',
    `images`           VARCHAR(2000)DEFAULT NULL            COMMENT '图片URL列表（逗号分隔）',
    `category`         VARCHAR(50)  DEFAULT NULL            COMMENT '分类',
    `condition_level`  INT          DEFAULT 5               COMMENT '新旧程度: 1-10',
    `exchange_type`    VARCHAR(20)  DEFAULT NULL            COMMENT '交换方式: borrow/gift/exchange',
    `expected_items`   VARCHAR(500) DEFAULT NULL            COMMENT '期望交换物品',
    `borrow_days`      INT          DEFAULT NULL            COMMENT '借用天数',
    `is_anonymous`     TINYINT      DEFAULT 0               COMMENT '匿名发布: 0=否 1=是',
    `status`           TINYINT      DEFAULT 1               COMMENT '状态: 1=正常 0=已下架',
    `view_count`       INT          DEFAULT 0               COMMENT '浏览次数',
    `created_at`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`          TINYINT      DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品共享表';

-- 活动发布表
DROP TABLE IF EXISTS `t_activity_post`;
CREATE TABLE `t_activity_post` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '活动ID',
    `user_id`          BIGINT       NOT NULL                COMMENT '发布者ID',
    `type`             VARCHAR(20)  DEFAULT NULL            COMMENT '类型: meal/exhibition/travel/other',
    `title`            VARCHAR(100) NOT NULL                COMMENT '标题',
    `description`      TEXT         DEFAULT NULL            COMMENT '描述',
    `activity_time`    DATETIME     DEFAULT NULL            COMMENT '活动开始时间',
    `end_time`         DATETIME     DEFAULT NULL            COMMENT '活动结束时间',
    `location`         VARCHAR(200) DEFAULT NULL            COMMENT '活动地点',
    `max_members`      INT          DEFAULT 10              COMMENT '最大人数',
    `current_members`  INT          DEFAULT 0               COMMENT '当前已报名人数',
    `cost_desc`        VARCHAR(200) DEFAULT NULL            COMMENT '费用说明（如AA制）',
    `is_anonymous`     TINYINT      DEFAULT 0               COMMENT '匿名发布: 0=否 1=是',
    `status`           TINYINT      DEFAULT 1               COMMENT '状态: 1=正常 0=已下架',
    `view_count`       INT          DEFAULT 0               COMMENT '浏览次数',
    `created_at`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`          TINYINT      DEFAULT 0               COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_activity_time` (`activity_time`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动发布表';

-- 活动报名成员表
DROP TABLE IF EXISTS `t_activity_member`;
CREATE TABLE `t_activity_member` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '报名ID',
    `activity_id`    BIGINT       NOT NULL                COMMENT '活动ID',
    `user_id`        BIGINT       NOT NULL                COMMENT '用户ID',
    `status`         TINYINT      DEFAULT 0               COMMENT '状态: -1=已拒绝 0=待审核 1=已通过',
    `apply_message`  VARCHAR(500) DEFAULT NULL            COMMENT '报名留言',
    `created_at`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_activity_id` (`activity_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动报名成员表';

-- 活动回顾表
DROP TABLE IF EXISTS `t_activity_review`;
CREATE TABLE `t_activity_review` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '回顾ID',
    `activity_id` BIGINT       NOT NULL                COMMENT '活动ID',
    `user_id`     BIGINT       NOT NULL                COMMENT '发布者ID',
    `content`     TEXT         DEFAULT NULL            COMMENT '感受内容',
    `images`      VARCHAR(2000)DEFAULT NULL            COMMENT '图片URL列表（逗号分隔）',
    `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_activity_id` (`activity_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动回顾表';

-- 技能标签表（用户自定义技能标签）
DROP TABLE IF EXISTS `t_skill_tag`;
CREATE TABLE `t_skill_tag` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `user_id`    BIGINT      NOT NULL                COMMENT '用户ID',
    `type`       VARCHAR(10) NOT NULL                COMMENT '类型: teach(我能教) / learn(我想学)',
    `tag_name`   VARCHAR(50) NOT NULL                COMMENT '标签名称',
    `created_at` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能标签表';

-- ============================================================
--  互助与信用
-- ============================================================

-- 互助记录表
DROP TABLE IF EXISTS `t_mutual_record`;
CREATE TABLE `t_mutual_record` (
    `id`                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `type`                 VARCHAR(20)  NOT NULL                COMMENT '类型: skill/goods/activity',
    `initiator_id`         BIGINT       NOT NULL                COMMENT '发起者ID',
    `participant_id`       BIGINT       NOT NULL                COMMENT '参与者ID',
    `related_id`           BIGINT       DEFAULT NULL            COMMENT '关联内容ID',
    `request_message`      VARCHAR(500) DEFAULT NULL            COMMENT '请求留言',
    `accept_message`       VARCHAR(500) DEFAULT NULL            COMMENT '接受留言',
    `status`               VARCHAR(20)  DEFAULT 'pending'       COMMENT '状态: pending/accepted/rejected/completed/cancelled',
    `start_time`           DATETIME     DEFAULT NULL            COMMENT '开始时间',
    `end_time`             DATETIME     DEFAULT NULL            COMMENT '结束时间',
    `initiator_confirmed`  TINYINT      DEFAULT 0               COMMENT '发起者确认完成: 0=否 1=是',
    `participant_confirmed` TINYINT     DEFAULT 0               COMMENT '参与者确认完成: 0=否 1=是',
    `created_at`           DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`           DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_initiator` (`initiator_id`),
    KEY `idx_participant` (`participant_id`),
    KEY `idx_related` (`related_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='互助记录表';

-- 评价表
DROP TABLE IF EXISTS `t_review`;
CREATE TABLE `t_review` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `record_id`   BIGINT       DEFAULT NULL            COMMENT '互助记录ID',
    `reviewer_id` BIGINT       NOT NULL                COMMENT '评价人ID',
    `reviewee_id` BIGINT       NOT NULL                COMMENT '被评价人ID',
    `rating`      INT          NOT NULL                COMMENT '评分: 1-5星',
    `content`     VARCHAR(500) DEFAULT NULL            COMMENT '评价内容',
    `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_record_id` (`record_id`),
    KEY `idx_reviewer` (`reviewer_id`),
    KEY `idx_reviewee` (`reviewee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- 信用变动记录表
DROP TABLE IF EXISTS `t_credit_log`;
CREATE TABLE `t_credit_log` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id`      BIGINT       NOT NULL                COMMENT '用户ID',
    `change_value` INT          NOT NULL                COMMENT '变动分值（正数加分，负数扣分）',
    `before_score` INT          DEFAULT NULL            COMMENT '变动前分数',
    `after_score`  INT          DEFAULT NULL            COMMENT '变动后分数',
    `reason`       VARCHAR(200) DEFAULT NULL            COMMENT '变动原因',
    `related_id`   BIGINT       DEFAULT NULL            COMMENT '关联记录ID（互助/举报等）',
    `created_at`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信用变动记录表';

-- ============================================================
--  消息与通知
-- ============================================================

-- 私信消息表
DROP TABLE IF EXISTS `t_message`;
CREATE TABLE `t_message` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `sender_id`   BIGINT       NOT NULL                COMMENT '发送者ID',
    `receiver_id` BIGINT       NOT NULL                COMMENT '接收者ID',
    `content`     TEXT         NOT NULL                COMMENT '消息内容',
    `type`        VARCHAR(20)  DEFAULT 'text'          COMMENT '消息类型: text / system',
    `is_read`     TINYINT      DEFAULT 0               COMMENT '是否已读: 0=未读 1=已读',
    `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    KEY `idx_sender` (`sender_id`),
    KEY `idx_receiver` (`receiver_id`),
    KEY `idx_is_read` (`is_read`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信消息表';

-- 系统通知表
DROP TABLE IF EXISTS `t_notification`;
CREATE TABLE `t_notification` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `user_id`    BIGINT       NOT NULL                COMMENT '接收者ID',
    `title`      VARCHAR(100) NOT NULL                COMMENT '通知标题',
    `content`    VARCHAR(500) DEFAULT NULL            COMMENT '通知内容',
    `type`       VARCHAR(50)  DEFAULT NULL            COMMENT '通知类型: mutual_request/activity_approved/report_result等',
    `related_id` BIGINT       DEFAULT NULL            COMMENT '关联记录ID',
    `is_read`    TINYINT      DEFAULT 0               COMMENT '是否已读: 0=未读 1=已读',
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_is_read` (`is_read`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';

-- ============================================================
--  管理与系统
-- ============================================================

-- 举报表
DROP TABLE IF EXISTS `t_report`;
CREATE TABLE `t_report` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '举报ID',
    `reporter_id` BIGINT       NOT NULL                COMMENT '举报人ID',
    `target_type` VARCHAR(20)  NOT NULL                COMMENT '举报目标类型: skill/goods/activity/user',
    `target_id`   BIGINT       NOT NULL                COMMENT '举报目标ID',
    `reason`      VARCHAR(50)  NOT NULL                COMMENT '举报原因',
    `description` VARCHAR(500) DEFAULT NULL            COMMENT '详细描述',
    `status`      VARCHAR(20)  DEFAULT 'pending'       COMMENT '状态: pending/approved/rejected',
    `handler_id`  BIGINT       DEFAULT NULL            COMMENT '处理人ID',
    `handle_note` VARCHAR(500) DEFAULT NULL            COMMENT '处理备注',
    `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_reporter` (`reporter_id`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';

-- 系统配置表
DROP TABLE IF EXISTS `t_system_config`;
CREATE TABLE `t_system_config` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `config_key`  VARCHAR(100) NOT NULL                COMMENT '配置键',
    `config_value` TEXT        DEFAULT NULL            COMMENT '配置值（JSON）',
    `description` VARCHAR(200) DEFAULT NULL            COMMENT '配置说明',
    `updated_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 管理员操作日志表（Redis 缓存 + MySQL 双写）
DROP TABLE IF EXISTS `t_operation_log`;
CREATE TABLE `t_operation_log` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `admin_id`   BIGINT       NOT NULL                COMMENT '操作管理员ID',
    `admin_name` VARCHAR(50)  DEFAULT NULL            COMMENT '操作管理员名称',
    `action`     VARCHAR(30)  NOT NULL                COMMENT '操作类型: ban_user/unban_user/offline_post/delete_post/handle_report/adjust_credit',
    `target`     VARCHAR(200) DEFAULT NULL            COMMENT '操作对象描述',
    `detail`     VARCHAR(500) DEFAULT NULL            COMMENT '详细信息',
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_admin` (`admin_id`),
    KEY `idx_action` (`action`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志表';

-- ============================================================
--  初始数据
-- ============================================================

-- 管理员账号（用户名: admin，密码: admin123，BCrypt加密）
INSERT INTO `t_user` (`username`, `password`, `nickname`, `role`, `status`, `credit_score`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '系统管理员', 'ADMIN', 1, 100);

-- 测试用普通用户
INSERT INTO `t_user` (`username`, `password`, `nickname`, `role`, `status`, `credit_score`) VALUES
('test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '测试用户', 'USER', 1, 80);

-- 系统初始配置
INSERT INTO `t_system_config` (`config_key`, `config_value`, `description`) VALUES
('site_name', '"青年同城互助平台"', '站点名称'),
('credit_init', '80', '新用户初始信用分'),
('report_deduct', '10', '举报通过默认扣分'),
('max_upload_size', '10', '上传文件大小限制(MB)');

-- ============================================================
--  完成
-- ============================================================
-- 提示：BCrypt 密码需运行时动态生成
-- 这里预设的密码哈希对应明文 "admin123"，仅用于开发/测试
-- 生产环境请通过注册接口或数据库工具设置真实密码
-- ============================================================
