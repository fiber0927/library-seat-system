-- 创建数据库
CREATE DATABASE IF NOT EXISTS library_seat_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE library_seat_system;

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `email` VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色: user, admin',
  `violations` INT NOT NULL DEFAULT 0 COMMENT '违约次数',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_username` (`username`),
  INDEX `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 座位表
CREATE TABLE IF NOT EXISTS `seat` (
  `id` VARCHAR(20) NOT NULL COMMENT '座位号',
  `floor` INT NOT NULL COMMENT '楼层',
  `area` VARCHAR(10) NOT NULL COMMENT '区域',
  `row_num` INT NOT NULL COMMENT '行号',
  `col_num` INT NOT NULL COMMENT '列号',
  `status` VARCHAR(20) NOT NULL DEFAULT 'available' COMMENT '状态: available, reserved, in_use, away, disabled',
  `features` VARCHAR(255) DEFAULT NULL COMMENT '设施特征',
  `user_id` BIGINT DEFAULT NULL COMMENT '当前使用用户ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_floor` (`floor`),
  INDEX `idx_area` (`area`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位表';

-- 预约表
CREATE TABLE IF NOT EXISTS `reservation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预约ID',
  `seat_id` VARCHAR(20) NOT NULL COMMENT '座位号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `date` DATE NOT NULL COMMENT '预约日期',
  `start_time` TIME NOT NULL COMMENT '开始时间',
  `end_time` TIME NOT NULL COMMENT '结束时间',
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending, active, away, completed, cancelled, violated',
  `check_in_time` DATETIME DEFAULT NULL COMMENT '签到时间',
  `check_out_time` DATETIME DEFAULT NULL COMMENT '离开时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_seat_id` (`seat_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_date` (`date`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约表';

-- 违规表
CREATE TABLE IF NOT EXISTS `violation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '违规ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `seat_id` VARCHAR(20) NOT NULL COMMENT '座位号',
  `date` DATE NOT NULL COMMENT '违规日期',
  `reason` VARCHAR(255) NOT NULL COMMENT '违规原因',
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending, handled',
  `handling` VARCHAR(255) DEFAULT NULL COMMENT '处理方式',
  `handled_at` DATETIME DEFAULT NULL COMMENT '处理时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='违规表';

-- 插入测试数据
INSERT INTO `sys_user` (`username`, `password`, `email`, `phone`, `role`, `violations`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@library.com', '13800138000', 'admin', 0),
('student001', '$2a$10$XyLrpLpOWqJOxdqLlE.zQeqLlE.zQeqLlE.zQeqLlE.zQeqLlE.z', 'student001@example.com', '13900139000', 'user', 1),
('student002', '$2a$10$XyLrpLpOWqJOxdqLlE.zQeqLlE.zQeqLlE.zQeqLlE.zQeqLlE.z', 'student002@example.com', '13700137000', 'user', 0);

-- 插入座位数据示例
INSERT INTO `seat` (`id`, `floor`, `area`, `row_num`, `col_num`, `status`, `features`) VALUES
('A101', 1, 'A', 1, 1, 'available', '电源插座,靠窗'),
('A102', 1, 'A', 1, 2, 'available', '电源插座'),
('A103', 1, 'A', 1, 3, 'reserved', NULL),
('A104', 1, 'A', 1, 4, 'in_use', '靠窗'),
('B101', 1, 'B', 1, 1, 'available', '空调'),
('B102', 1, 'B', 1, 2, 'away', '空调'),
('C101', 1, 'C', 1, 1, 'available', '安静区'),
('D101', 1, 'D', 1, 1, 'disabled', '讨论区');
