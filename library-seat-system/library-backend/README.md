# 图书馆座位管理系统 - 后端

## 项目简介

这是一个基于 Spring Boot 3 + MyBatis-Plus + MySQL 8 的图书馆座位管理系统后端服务。

## 技术栈

- **Spring Boot 3.2.2** - Spring Boot 框架
- **MyBatis-Plus 3.5.5** - MyBatis 增强工具
- **MySQL 8.0+** - 关系型数据库
- **JWT** - JSON Web Token 认证
- **Lombok** - Java 注解库
- **Spring Scheduling** - 定时任务

## 项目结构

```
library-backend/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── library/
│       │           ├── LibrarySeatApplication.java    # 主应用类
│       │           ├── common/                        # 通用类
│       │           │   └── Result.java               # 统一响应结果
│       │           ├── config/                       # 配置类
│       │           │   ├── CorsConfig.java          # 跨域配置
│       │           │   └── WebMvcConfig.java         # Web配置
│       │           ├── controller/                   # 控制层
│       │           │   ├── UserController.java
│       │           │   ├── SeatController.java
│       │           │   ├── ReservationController.java
│       │           │   └── ViolationController.java
│       │           ├── service/                      # 服务层
│       │           │   ├── UserService.java
│       │           │   ├── SeatService.java
│       │           │   ├── ReservationService.java
│       │           │   └── ViolationService.java
│       │           ├── mapper/                       # 数据访问层
│       │           │   ├── UserMapper.java
│       │           │   ├── SeatMapper.java
│       │           │   ├── ReservationMapper.java
│       │           │   └── ViolationMapper.java
│       │           ├── entity/                       # 实体类
│       │           │   ├── User.java
│       │           │   ├── Seat.java
│       │           │   ├── Reservation.java
│       │           │   └── Violation.java
│       │           ├── interceptor/                 # 拦截器
│       │           │   └── JwtInterceptor.java
│       │           ├── utils/                      # 工具类
│       │           │   └── JwtUtils.java
│       │           └── scheduler/                   # 定时任务
│       │               └── ReservationCleanupTask.java
│       └── resources/
│           └── application.yml                    # 配置文件
├── sql/
│   └── init.sql                                 # 数据库初始化脚本
├── pom.xml                                      # Maven 配置
└── README.md                                    # 项目说明
```

## 核心功能

### 1. 用户认证
- ✅ 用户注册
- ✅ 用户登录（JWT Token）
- ✅ 密码BCrypt加密
- ✅ 管理员权限控制

### 2. 座位管理
- ✅ 按楼层、区域查询座位
- ✅ 座位状态管理
- ✅ 座位使用统计
- ✅ 管理员增删改查

### 3. 预约管理（核心功能）
- ✅ **一人一座限制**：同一用户同一时间只能预约1个座位
- ✅ **30分钟签到宽限期**：预约时段开始后30分钟内可正常签到
- ✅ 预约时段冲突检测
- ✅ 签到、暂离、取消、结束使用
- ✅ 超时自动取消预约

### 4. 违规管理
- ✅ 自动记录违约
- ✅ 管理员处理违规
- ✅ 违约统计

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE library_seat_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 导入数据库脚本：
```bash
mysql -u root -p library_seat_system < sql/init.sql
```

3. 修改 `src/main/resources/application.yml` 中的数据库配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/library_seat_system?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: your_username
    password: your_password
```

### 3. 编译运行

```bash
# 编译项目
mvn clean install

# 运行项目
mvn spring-boot:run

# 或者打包后运行
java -jar target/library-seat-system-1.0.0.jar
```

### 4. 验证服务

访问：http://localhost:8080/api/users/login

## API 接口

### 用户接口

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| POST | /api/users/login | 用户登录 | 否 |
| POST | /api/users/register | 用户注册 | 否 |
| GET | /api/users/{id} | 获取用户信息 | 是 |
| GET | /api/users/current | 获取当前用户 | 是 |
| GET | /api/users | 获取所有用户 | 管理员 |
| PUT | /api/users/{id} | 更新用户信息 | 是 |
| PUT | /api/users/{id}/toggle-enabled | 启用/禁用用户 | 管理员 |

### 座位接口

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| GET | /api/seats | 获取所有座位 | 是 |
| GET | /api/seats/floor/{floor} | 按楼层获取 | 是 |
| GET | /api/seats/floor/{floor}/area/{area} | 按楼层和区域获取 | 是 |
| GET | /api/seats/available | 获取空闲座位 | 是 |
| GET | /api/seats/{id} | 获取座位详情 | 是 |
| GET | /api/seats/statistics | 获取座位统计 | 是 |
| POST | /api/seats | 添加座位 | 管理员 |
| PUT | /api/seats/{id}/status | 更新座位状态 | 管理员 |
| DELETE | /api/seats/{id} | 删除座位 | 管理员 |

### 预约接口

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| POST | /api/reservations | 创建预约 | 是 |
| POST | /api/reservations/{id}/check-in | 签到 | 是 |
| POST | /api/reservations/{id}/away | 暂离 | 是 |
| POST | /api/reservations/{id}/return | 取消暂离 | 是 |
| POST | /api/reservations/{id}/cancel | 取消预约 | 是 |
| POST | /api/reservations/{id}/checkout | 结束使用 | 是 |
| GET | /api/reservations/user/{userId} | 获取用户预约 | 是 |
| GET | /api/reservations/user/{userId}/active | 获取用户当前预约 | 是 |
| GET | /api/reservations | 获取所有预约 | 管理员 |
| GET | /api/reservations/status/{status} | 按状态获取预约 | 管理员 |

### 违规接口

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| GET | /api/violations | 获取所有违规 | 管理员 |
| GET | /api/violations/pending | 获取待处理违规 | 管理员 |
| GET | /api/violations/user/{userId} | 获取用户违规 | 是 |
| GET | /api/violations/statistics | 获取违规统计 | 管理员 |
| POST | /api/violations/{id}/handle | 处理违规 | 管理员 |
| DELETE | /api/violations/{id} | 删除违规 | 管理员 |

## 业务规则

### 预约限制

1. **一人一座**
   - 同一用户同一时间只能有1个有效预约（pending/active/away状态）
   - 预约时自动校验

2. **时段冲突**
   - 同一座位同一时段不能重复预约
   - 时段重叠也视为冲突

3. **签到宽限期**
   - 预约时段开始后30分钟内可正常签到
   - 超过30分钟签到失败，预约自动取消
   - 宽限期可配置（application.yml）

### 座位状态

| 状态 | 说明 |
|------|------|
| available | 空闲，可预约 |
| reserved | 已预约 |
| in_use | 使用中 |
| away | 暂离 |
| disabled | 不可用 |

### 预约状态

| 状态 | 说明 |
|------|------|
| pending | 待签到 |
| active | 使用中 |
| away | 暂离中 |
| completed | 已完成 |
| cancelled | 已取消 |
| violated | 已违约 |

## 数据库表

### sys_user - 用户表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 用户ID |
| username | VARCHAR(50) | 用户名 |
| password | VARCHAR(255) | 密码（BCrypt加密） |
| email | VARCHAR(100) | 邮箱 |
| phone | VARCHAR(20) | 手机号 |
| role | VARCHAR(20) | 角色：user/admin |
| violations | INT | 违约次数 |
| enabled | TINYINT | 账号状态 |
| deleted | TINYINT | 删除标记 |

### seat - 座位表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(20) | 座位号（如A101） |
| floor | INT | 楼层 |
| area | VARCHAR(10) | 区域（A/B/C/D） |
| row_num | INT | 行号 |
| col_num | INT | 列号 |
| status | VARCHAR(20) | 座位状态 |
| features | VARCHAR(500) | 座位特征 |
| user_id | BIGINT | 当前占用用户 |

### reservation - 预约表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 预约ID |
| seat_id | VARCHAR(20) | 座位号 |
| user_id | BIGINT | 用户ID |
| date | DATE | 预约日期 |
| start_time | TIME | 开始时间 |
| end_time | TIME | 结束时间 |
| status | VARCHAR(20) | 预约状态 |
| check_in_time | DATETIME | 签到时间 |
| check_out_time | DATETIME | 离开时间 |

### violation - 违规表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 违规ID |
| user_id | BIGINT | 用户ID |
| seat_id | VARCHAR(20) | 座位号 |
| reservation_id | BIGINT | 关联预约ID |
| date | DATE | 违规日期 |
| reason | VARCHAR(255) | 违规原因 |
| status | VARCHAR(20) | 状态 |
| handling | VARCHAR(255) | 处理方式 |

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | student001 | admin123 |
| 普通用户 | student002 | admin123 |

## 配置说明

### JWT配置

```yaml
jwt:
  secret: your-secret-key
  expiration: 86400000  # 24小时
```

### 系统配置

```yaml
system:
  check-in-grace-period: 30  # 签到宽限期（分钟）
  reservation:
    max-per-user: 1           # 每人最大预约数
    advance-booking-days: 7   # 提前预约天数
```

## 定时任务

### 超时预约清理

- 每60秒执行一次
- 自动将超时的预约（pending状态超过30分钟）标记为violated
- 自动释放座位
- 自动创建违规记录
- 自动增加用户违约次数

## 安全性

- ✅ JWT Token认证
- ✅ BCrypt密码加密
- ✅ 接口权限控制
- ✅ 跨域配置
- ✅ SQL注入防护（MyBatis-Plus）
- ✅ 参数校验

## 性能优化

- ✅ HikariCP连接池
- ✅ MyBatis-Plus自动SQL优化
- ✅ 数据库索引优化
- ✅ 定时任务异步执行

## License

MIT
