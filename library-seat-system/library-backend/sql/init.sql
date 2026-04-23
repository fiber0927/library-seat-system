-- 图书馆座位管理系统 - MySQL 8 数据库初始化脚本
-- 数据库版本: MySQL 8.0+
-- 创建时间: 2026-04-07

-- 创建数据库
CREATE DATABASE IF NOT EXISTS library_seat_system 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE library_seat_system;

-- =====================================================
-- 表1: 用户表 (sys_user)
-- =====================================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色: user-普通用户, admin-管理员',
  `violations` INT NOT NULL DEFAULT 0 COMMENT '违约次数',
  `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态: 0-禁用, 1-启用',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_role` (`role`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =====================================================
-- 表2: 座位表 (seat)
-- =====================================================
DROP TABLE IF EXISTS `seat`;
CREATE TABLE `seat` (
  `id` VARCHAR(20) NOT NULL COMMENT '座位号 (格式: 区域+楼层+序号, 如 A101)',
  `floor` INT NOT NULL COMMENT '楼层',
  `area` VARCHAR(10) NOT NULL COMMENT '区域 (A/B/C/D)',
  `row_num` INT NOT NULL COMMENT '行号',
  `col_num` INT NOT NULL COMMENT '列号',
  `status` VARCHAR(20) NOT NULL DEFAULT 'available' COMMENT '状态: available-空闲, reserved-已预约, in_use-使用中, away-暂离, disabled-不可用',
  `features` VARCHAR(500) DEFAULT NULL COMMENT '座位特征 (多个特征用逗号分隔: 电源插座,靠窗,空调,安静区,讨论区)',
  `user_id` BIGINT DEFAULT NULL COMMENT '当前占用用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_floor` (`floor`),
  KEY `idx_area` (`area`),
  KEY `idx_status` (`status`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位表';

-- =====================================================
-- 表3: 预约表 (reservation)
-- =====================================================
DROP TABLE IF EXISTS `reservation`;
CREATE TABLE `reservation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预约ID',
  `seat_id` VARCHAR(20) NOT NULL COMMENT '座位号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `date` DATE NOT NULL COMMENT '预约日期',
  `start_time` TIME NOT NULL COMMENT '预约开始时间',
  `end_time` TIME NOT NULL COMMENT '预约结束时间',
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending-待签到, active-使用中, away-暂离, completed-已完成, cancelled-已取消, violated-已违约',
  `check_in_time` DATETIME DEFAULT NULL COMMENT '签到时间',
  `check_out_time` DATETIME DEFAULT NULL COMMENT '离开时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_seat_id` (`seat_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_date` (`date`),
  KEY `idx_status` (`status`),
  KEY `idx_seat_date_status` (`seat_id`, `date`, `status`),
  KEY `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约表';

-- =====================================================
-- 表4: 违规记录表 (violation)
-- =====================================================
DROP TABLE IF EXISTS `violation`;
CREATE TABLE `violation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '违规ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `seat_id` VARCHAR(20) NOT NULL COMMENT '座位号',
  `reservation_id` BIGINT DEFAULT NULL COMMENT '关联预约ID',
  `date` DATE NOT NULL COMMENT '违规日期',
  `reason` VARCHAR(255) NOT NULL COMMENT '违规原因',
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending-待处理, handled-已处理',
  `handling` VARCHAR(255) DEFAULT NULL COMMENT '处理方式',
  `handled_by` BIGINT DEFAULT NULL COMMENT '处理人ID',
  `handled_at` DATETIME DEFAULT NULL COMMENT '处理时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_seat_id` (`seat_id`),
  KEY `idx_status` (`status`),
  KEY `idx_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='违规记录表';

-- =====================================================
-- 插入测试数据
-- =====================================================

-- 插入管理员账号 (密码: admin123, BCrypt加密)
INSERT INTO `sys_user` (`username`, `password`, `email`, `phone`, `role`, `violations`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@library.com', '13800138000', 'admin', 0),
('student001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'student001@example.com', '13900139000', 'user', 1),
('student002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'student002@example.com', '13700137000', 'user', 0),
('student003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'student003@example.com', '13600136000', 'user', 0);

-- 插入座位数据
-- 1楼A区
INSERT INTO `seat` (`id`, `floor`, `area`, `row_num`, `col_num`, `status`, `features`) VALUES
('A101', 1, 'A', 1, 1, 'available', '电源插座,靠窗'),
('A102', 1, 'A', 1, 2, 'available', '电源插座'),
('A103', 1, 'A', 1, 3, 'available', NULL),
('A104', 1, 'A', 1, 4, 'available', '靠窗'),
('A105', 1, 'A', 2, 1, 'reserved', '电源插座'),
('A106', 1, 'A', 2, 2, 'in_use', NULL),
('A107', 1, 'A', 2, 3, 'available', '电源插座,靠窗'),
('A108', 1, 'A', 2, 4, 'available', NULL);

-- 1楼B区
INSERT INTO `seat` (`id`, `floor`, `area`, `row_num`, `col_num`, `status`, `features`) VALUES
('B101', 1, 'B', 1, 1, 'available', '空调'),
('B102', 1, 'B', 1, 2, 'available', '空调'),
('B103', 1, 'B', 1, 3, 'away', NULL),
('B104', 1, 'B', 1, 4, 'available', '空调,靠窗'),
('B105', 1, 'B', 2, 1, 'available', '空调'),
('B106', 1, 'B', 2, 2, 'available', NULL),
('B107', 1, 'B', 2, 3, 'disabled', '空调'),
('B108', 1, 'B', 2, 4, 'available', '空调,靠窗');

-- 1楼C区
INSERT INTO `seat` (`id`, `floor`, `area`, `row_num`, `col_num`, `status`, `features`) VALUES
('C101', 1, 'C', 1, 1, 'available', '安静区'),
('C102', 1, 'C', 1, 2, 'available', '安静区,电源插座'),
('C103', 1, 'C', 1, 3, 'available', '安静区'),
('C104', 1, 'C', 1, 4, 'available', '安静区,靠窗'),
('C105', 1, 'C', 2, 1, 'reserved', '安静区'),
('C106', 1, 'C', 2, 2, 'available', '安静区,电源插座'),
('C107', 1, 'C', 2, 3, 'available', '安静区'),
('C108', 1, 'C', 2, 4, 'available', '安静区,靠窗');

-- 1楼D区
INSERT INTO `seat` (`id`, `floor`, `area`, `row_num`, `col_num`, `status`, `features`) VALUES
('D101', 1, 'D', 1, 1, 'available', '讨论区'),
('D102', 1, 'D', 1, 2, 'available', '讨论区'),
('D103', 1, 'D', 1, 3, 'available', '讨论区,电源插座'),
('D104', 1, 'D', 1, 4, 'available', '讨论区,靠窗');

-- 2楼座位 (复制1楼结构)
INSERT INTO `seat` (`id`, `floor`, `area`, `row_num`, `col_num`, `status`, `features`)
SELECT CONCAT(area, '2', LPAD(row_num * 4 + col_num - 4, 2, '0')), 2, area, row_num, col_num, 'available', features
FROM seat WHERE floor = 1;

-- 3楼座位 (安静区为主)
INSERT INTO `seat` (`id`, `floor`, `area`, `row_num`, `col_num`, `status`, `features`)
SELECT CONCAT(area, '3', LPAD(row_num * 4 + col_num - 4, 2, '0')), 3, area, row_num, col_num, 'available', CONCAT(IFNULL(CONCAT(features, ','), ''), '安静区')
FROM seat WHERE floor = 1;

-- 插入预约记录示例
INSERT INTO `reservation` (`seat_id`, `user_id`, `date`, `start_time`, `end_time`, `status`, `check_in_time`) VALUES
('A105', 2, CURDATE(), '09:00:00', '12:00:00', 'reserved', NULL),
('C105', 2, CURDATE(), '14:00:00', '17:00:00', 'active', NOW()),
('B103', 3, CURDATE(), '08:00:00', '11:00:00', 'away', NOW());

-- 插入违规记录示例
INSERT INTO `violation` (`user_id`, `seat_id`, `reservation_id`, `date`, `reason`, `status`, `handling`, `handled_by`, `handled_at`) VALUES
(2, 'A102', 1, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '预约后未签到', 'handled', '口头警告', 1, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =====================================================
-- 创建存储过程: 清理超时预约
-- =====================================================
DELIMITER //

CREATE PROCEDURE IF NOT EXISTS `cleanup_expired_reservations`()
BEGIN
    DECLARE GRACE_PERIOD INT DEFAULT 30; -- 宽限期30分钟
    
    -- 更新超时预约为违规状态
    UPDATE reservation r
    INNER JOIN seat s ON r.seat_id = s.id
    SET 
        r.status = 'violated',
        s.status = 'available',
        s.user_id = NULL,
        r.update_time = NOW()
    WHERE 
        r.status = 'pending'
        AND r.date = CURDATE()
        AND ADDTIME(r.date, r.start_time) < DATE_SUB(NOW(), INTERVAL GRACE_PERIOD MINUTE);
        
END //

DELIMITER ;

-- =====================================================
-- 创建事件: 每天凌晨2点执行清理任务
-- =====================================================
SET GLOBAL event_scheduler = ON;

CREATE EVENT IF NOT EXISTS `evt_cleanup_expired_reservations`
ON SCHEDULE EVERY 1 HOUR
STARTS CURRENT_TIMESTAMP
ON COMPLETION PRESERVE
ENABLE
DO CALL cleanup_expired_reservations();

-- =====================================================
-- 视图: 座位使用统计
-- =====================================================
CREATE OR REPLACE VIEW `v_seat_statistics` AS
SELECT 
    floor,
    COUNT(*) as total_seats,
    SUM(CASE WHEN status = 'available' THEN 1 ELSE 0 END) as available_seats,
    SUM(CASE WHEN status = 'reserved' THEN 1 ELSE 0 END) as reserved_seats,
    SUM(CASE WHEN status = 'in_use' THEN 1 ELSE 0 END) as in_use_seats,
    SUM(CASE WHEN status = 'away' THEN 1 ELSE 0 END) as away_seats,
    SUM(CASE WHEN status = 'disabled' THEN 1 ELSE 0 END) as disabled_seats,
    ROUND((COUNT(*) - SUM(CASE WHEN status = 'available' THEN 1 ELSE 0 END)) / COUNT(*) * 100, 1) as usage_rate
FROM seat
WHERE deleted = 0
GROUP BY floor;

-- =====================================================
-- 视图: 用户预约统计
-- =====================================================
CREATE OR REPLACE VIEW `v_user_reservation_stats` AS
SELECT 
    u.id as user_id,
    u.username,
    COUNT(r.id) as total_reservations,
    SUM(CASE WHEN r.status = 'completed' THEN 1 ELSE 0 END) as completed_reservations,
    SUM(CASE WHEN r.status = 'cancelled' THEN 1 ELSE 0 END) as cancelled_reservations,
    SUM(CASE WHEN r.status = 'violated' THEN 1 ELSE 0 END) as violated_reservations,
    u.violations
FROM sys_user u
LEFT JOIN reservation r ON u.id = r.user_id
WHERE u.deleted = 0
GROUP BY u.id, u.username, u.violations;

-- =====================================================
-- 授予权限 (如果需要)
-- =====================================================
-- GRANT ALL PRIVILEGES ON library_seat_system.* TO 'library_user'@'%' IDENTIFIED BY 'your_password';
-- FLUSH PRIVILEGES;

-- =====================================================
-- 完成提示
-- =====================================================
SELECT '数据库初始化完成!' as message;
SELECT COUNT(*) as user_count FROM sys_user;
SELECT COUNT(*) as seat_count FROM seat;
SELECT COUNT(*) as reservation_count FROM reservation;
