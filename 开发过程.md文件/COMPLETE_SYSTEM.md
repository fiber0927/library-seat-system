# 图书馆座位管理系统 - 完整系统说明

## 项目概述

这是一个完整的**前后端分离**的图书馆座位管理系统，包含Vue 3前端和Spring Boot 3后端。

---

## 🏗️ 系统架构

```
┌─────────────────────────────────────────┐
│           Vue 3 前端应用               │
│   Element Plus + Vite + Pinia + Vue Router │
│         http://localhost:5173           │
└────────────────┬──────────────────────┘
                 │ HTTP/HTTPS
                 │ REST API
                 │ JSON
                 ▼
┌─────────────────────────────────────────┐
│        Spring Boot 3 后端服务            │
│  MyBatis-Plus + JWT + MySQL 8          │
│         http://localhost:8080            │
└────────────────┬──────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│          MySQL 8 数据库                 │
│      library_seat_system                  │
└─────────────────────────────────────────┘
```

---

## 📦 项目结构

```
library-seat-system/
├── library-seat-system/              # Vue 3 前端
│   ├── src/
│   │   ├── views/
│   │   │   ├── user/               # 用户端页面
│   │   │   │   ├── Login.vue
│   │   │   │   ├── Register.vue
│   │   │   │   ├── Home.vue
│   │   │   │   ├── SeatMap.vue
│   │   │   │   └── PersonalCenter.vue
│   │   │   └── admin/              # 管理员端页面
│   │   │       ├── Dashboard.vue
│   │   │       ├── SeatManagement.vue
│   │   │       ├── UserManagement.vue
│   │   │       ├── ViolationManagement.vue
│   │   │       └── ReservationManagement.vue
│   │   ├── stores/                 # Pinia 状态管理
│   │   │   ├── user.js
│   │   │   ├── seats.js
│   │   │   └── admin.js
│   │   └── router/                # Vue Router 配置
│   ├── package.json
│   ├── vite.config.js
│   └── README.md
│
├── library-backend/                   # Spring Boot 后端
│   ├── src/main/java/com/library/
│   │   ├── controller/             # REST API 控制器
│   │   │   ├── UserController.java
│   │   │   ├── SeatController.java
│   │   │   ├── ReservationController.java
│   │   │   └── ViolationController.java
│   │   ├── service/                # 业务逻辑层
│   │   │   ├── UserService.java
│   │   │   ├── SeatService.java
│   │   │   ├── ReservationService.java
│   │   │   └── ViolationService.java
│   │   ├── mapper/                # 数据访问层
│   │   │   ├── UserMapper.java
│   │   │   ├── SeatMapper.java
│   │   │   ├── ReservationMapper.java
│   │   │   └── ViolationMapper.java
│   │   ├── entity/                # 实体类
│   │   │   ├── User.java
│   │   │   ├── Seat.java
│   │   │   ├── Reservation.java
│   │   │   └── Violation.java
│   │   ├── config/                # 配置类
│   │   │   ├── CorsConfig.java
│   │   │   └── WebMvcConfig.java
│   │   ├── interceptor/           # 拦截器
│   │   │   └── JwtInterceptor.java
│   │   ├── utils/                  # 工具类
│   │   │   └── JwtUtils.java
│   │   ├── common/                 # 通用类
│   │   │   └── Result.java
│   │   └── scheduler/             # 定时任务
│   │       └── ReservationCleanupTask.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── sql/
│   │   └── init.sql              # MySQL 8 数据库初始化脚本
│   ├── pom.xml
│   └── README.md
│
└── README.md                        # 项目总说明
```

---

## 🎯 核心功能

### 1. 用户认证 ✅

**功能特性**：
- ✅ 用户注册（邮箱验证）
- ✅ 用户登录（JWT Token）
- ✅ 密码BCrypt加密存储
- ✅ 会话管理
- ✅ 权限控制（普通用户/管理员）

**技术实现**：
- JWT Token认证
- BCrypt密码加密
- 拦截器权限验证
- 跨域配置

### 2. 座位管理 ✅

**功能特性**：
- ✅ 可视化座位平面图
- ✅ 按楼层筛选（A/B/C/D区）
- ✅ 按状态筛选（空闲/已预约/使用中/暂离）
- ✅ 座位设施标注（电源插座/靠窗/空调/安静区/讨论区）
- ✅ 实时座位状态统计

**座位状态**：
| 状态 | 颜色 | 说明 |
|------|------|------|
| 🟢 available | 绿色 | 空闲，可预约 |
| 🔵 reserved | 蓝色 | 已预约 |
| 🟠 in_use | 橙色 | 使用中 |
| ⚫ away | 灰色 | 暂离 |
| 🔴 disabled | 深灰色 | 不可用 |

### 3. 预约管理（核心功能）✅

#### 3.1 一人一座限制
- ✅ 同一用户同一时间只能预约**1个座位**
- ✅ 预约时自动校验
- ✅ 已预约用户无法重复预约

#### 3.2 30分钟签到宽限期
- ✅ 预约时段开始后**30分钟内**可正常签到
- ✅ 宽限期内签到成功，状态更新为"使用中"
- ✅ 超过30分钟签到失败，预约自动取消

**签到流程**：
```
预约创建
    ↓
状态: pending (待签到)
    ↓
    ├─ 30分钟内签到 → 状态: active (使用中) ✅
    │
    ├─ 30分钟后签到 → 状态: violated (已违约) ❌
    │                   座位自动释放
    │
    ├─ 用户主动取消 → 状态: cancelled (已取消)
    │                   座位自动释放
    │
    └─ 使用完毕 → 状态: completed (已完成)
                    座位自动释放
```

#### 3.3 时段冲突检测
- ✅ 同一座位同一时段不能重复预约
- ✅ 时段重叠也视为冲突

### 4. 管理员后台 ✅

**功能模块**：
- ✅ **仪表盘**：实时座位使用统计
- ✅ **座位管理**：增删改查座位信息
- ✅ **用户管理**：查看/启用/禁用用户
- ✅ **预约管理**：查看所有预约记录
- ✅ **违规管理**：处理违规记录

---

## 🔧 技术栈

### 前端技术栈
- **Vue 3** - 渐进式JavaScript框架
- **Composition API** - 组合式API
- **Element Plus** - UI组件库
- **Vite 5** - 构建工具
- **Vue Router 4** - 路由管理
- **Pinia** - 状态管理

### 后端技术栈
- **Spring Boot 3.2.2** - Spring Boot框架
- **MyBatis-Plus 3.5.5** - ORM框架
- **MySQL 8.0+** - 关系型数据库
- **JWT** - 身份认证
- **Lombok** - 代码简化
- **Spring Scheduling** - 定时任务

---

## 🚀 快速开始

### 前端启动

```bash
cd library-seat-system
npm install
npm run dev
```

访问：http://localhost:5173

### 后端启动

1. **配置MySQL数据库**：

```bash
mysql -u root -p
CREATE DATABASE library_seat_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;

mysql -u root -p library_seat_system < library-backend/sql/init.sql
```

2. **修改数据库配置**：

编辑 `library-backend/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/library_seat_system?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password
```

3. **编译运行**：

```bash
cd library-backend
mvn clean install
mvn spring-boot:run
```

服务运行在：http://localhost:8080

---

## 🧪 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | student001 | admin123 |
| 普通用户 | student002 | admin123 |

---

## 📊 数据库表

### 1. sys_user - 用户表
| 字段 | 说明 |
|------|------|
| id | 用户ID |
| username | 用户名 |
| password | 密码（BCrypt加密）|
| email | 邮箱 |
| phone | 手机号 |
| role | 角色（user/admin）|
| violations | 违约次数 |
| enabled | 账号状态 |
| deleted | 删除标记 |

### 2. seat - 座位表
| 字段 | 说明 |
|------|------|
| id | 座位号（格式：A101）|
| floor | 楼层 |
| area | 区域（A/B/C/D）|
| row_num | 行号 |
| col_num | 列号 |
| status | 座位状态 |
| features | 座位特征 |
| user_id | 当前占用用户 |

### 3. reservation - 预约表
| 字段 | 说明 |
|------|------|
| id | 预约ID |
| seat_id | 座位号 |
| user_id | 用户ID |
| date | 预约日期 |
| start_time | 开始时间 |
| end_time | 结束时间 |
| status | 预约状态 |
| check_in_time | 签到时间 |
| check_out_time | 离开时间 |

### 4. violation - 违规表
| 字段 | 说明 |
|------|------|
| id | 违规ID |
| user_id | 用户ID |
| seat_id | 座位号 |
| reservation_id | 关联预约ID |
| date | 违规日期 |
| reason | 违规原因 |
| status | 状态（pending/handled）|
| handling | 处理方式 |

---

## 📡 API 接口

### 用户接口

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| POST | /api/users/login | 用户登录 | ❌ |
| POST | /api/users/register | 用户注册 | ❌ |
| GET | /api/users/{id} | 获取用户信息 | ✅ |
| GET | /api/users/current | 获取当前用户 | ✅ |
| GET | /api/users | 获取所有用户 | 管理员 |
| PUT | /api/users/{id} | 更新用户 | ✅ |
| PUT | /api/users/{id}/toggle-enabled | 启用/禁用用户 | 管理员 |

### 座位接口

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| GET | /api/seats | 获取所有座位 | ✅ |
| GET | /api/seats/floor/{floor} | 按楼层获取 | ✅ |
| GET | /api/seats/floor/{floor}/area/{area} | 按楼层和区域获取 | ✅ |
| GET | /api/seats/available | 获取空闲座位 | ✅ |
| GET | /api/seats/{id} | 获取座位详情 | ✅ |
| GET | /api/seats/statistics | 获取统计 | ✅ |
| POST | /api/seats | 添加座位 | 管理员 |
| PUT | /api/seats/{id}/status | 更新状态 | 管理员 |
| DELETE | /api/seats/{id} | 删除座位 | 管理员 |

### 预约接口

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| POST | /api/reservations | 创建预约 | ✅ |
| POST | /api/reservations/{id}/check-in | 签到 | ✅ |
| POST | /api/reservations/{id}/away | 暂离 | ✅ |
| POST | /api/reservations/{id}/return | 取消暂离 | ✅ |
| POST | /api/reservations/{id}/cancel | 取消预约 | ✅ |
| POST | /api/reservations/{id}/checkout | 结束使用 | ✅ |
| GET | /api/reservations/user/{userId} | 获取用户预约 | ✅ |
| GET | /api/reservations/user/{userId}/active | 获取当前预约 | ✅ |
| GET | /api/reservations | 获取所有预约 | 管理员 |

### 违规接口

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| GET | /api/violations | 获取所有违规 | 管理员 |
| GET | /api/violations/pending | 获取待处理违规 | 管理员 |
| GET | /api/violations/user/{userId} | 获取用户违规 | ✅ |
| GET | /api/violations/statistics | 获取统计 | 管理员 |
| POST | /api/violations/{id}/handle | 处理违规 | 管理员 |
| DELETE | /api/violations/{id} | 删除违规 | 管理员 |

---

## ⚙️ 配置说明

### JWT配置
```yaml
jwt:
  secret: library-seat-system-jwt-secret-key-2026-library-management
  expiration: 86400000  # 24小时（毫秒）
```

### 系统配置
```yaml
system:
  check-in-grace-period: 30  # 签到宽限期（分钟）
  reservation:
    max-per-user: 1         # 每人最大预约数
```

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/library_seat_system?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: root
```

---

## 🔐 安全特性

### 1. 身份认证
- ✅ JWT Token认证
- ✅ Token过期时间24小时
- ✅ 自动刷新机制

### 2. 密码安全
- ✅ BCrypt加密存储
- ✅ 防暴力破解

### 3. 接口权限
- ✅ 用户接口权限控制
- ✅ 管理员接口保护
- ✅ 用户数据隔离

### 4. 跨域安全
- ✅ CORS配置
- ✅ 跨域资源共享

---

## 📈 性能优化

### 1. 数据库优化
- ✅ HikariCP连接池（最大20连接）
- ✅ 数据库索引优化
- ✅ SQL日志记录

### 2. 代码优化
- ✅ MyBatis-Plus自动SQL优化
- ✅ 逻辑删除（软删除）
- ✅ 自动填充时间戳

### 3. 定时任务
- ✅ 每60秒清理超时预约
- ✅ 自动创建违规记录
- ✅ 自动释放座位

---

## 📱 响应式设计

✅ 支持多终端访问：
- 🖥️ 桌面端 (≥1200px)
- 📱 平板端 (768-1199px)
- 📱 移动端 (<768px)

---

## 📚 项目文档

| 文档 | 说明 |
|------|------|
| [README.md](README.md) | 项目总说明 |
| [library-seat-system/README.md](library-seat-system/README.md) | 前端项目说明 |
| [library-backend/README.md](library-backend/README.md) | 后端项目说明 |
| [OPTIMIZATION.md](OPTIMIZATION.md) | 预约逻辑优化说明 |
| [CHECKIN_OPTIMIZATION.md](CHECKIN_OPTIMIZATION.md) | 签到宽限期优化说明 |

---

## 🎉 系统亮点

1. **完整的前后端分离架构** - 清晰的分层设计
2. **强大的预约管理** - 一人一座、30分钟宽限期
3. **实时状态同步** - 座位状态实时更新
4. **完善的管理后台** - 管理员全方位管理
5. **安全认证** - JWT + 权限控制
6. **数据持久化** - MySQL 8完整存储
7. **响应式设计** - 完美适配各种设备
8. **友好提示** - 分场景用户提示

---

## 🚀 后续优化建议

1. **功能增强**
   - 短信/邮件提醒
   - 座位推荐算法
   - 使用数据分析
   - 预约提醒通知

2. **性能优化**
   - Redis缓存
   - 数据库读写分离
   - 接口限流
   - 日志管理

3. **安全增强**
   - OAuth2.0认证
   - 数据加密传输
   - 操作日志审计
   - 入侵检测

4. **用户体验**
   - 移动端APP
   - 微信小程序
   - 智能客服
   - 使用指南

---

## 📞 技术支持

如有问题，请参考各模块的README文档或提交Issue。

---

## 📄 License

MIT License

---

**🎊 恭喜！图书馆座位管理系统开发完成！**
