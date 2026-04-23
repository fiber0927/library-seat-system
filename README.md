# 图书馆座位管理系统

## 项目概述

这是一个功能完整的**图书馆座位预约管理系统**，采用前后端分离架构，支持电脑端和手机端访问。系统提供直观的可视化座位地图、灵活的预约管理、签到签退等功能。

### 技术栈

#### 前端技术栈
- **Vue 3** + Composition API（setup语法糖）
- **Element Plus** UI组件库
- **Vite 5** 构建工具（开发服务器端口：5174）
- **Vue Router 4** 页面路由
- **Pinia** 状态管理
- **axios** HTTP客户端
- 响应式设计，完美适配手机端

#### 后端技术栈
- **Spring Boot 3.2.2** 核心框架
- **MyBatis-Plus 3.5.5** ORM框架
- **MySQL 8** 数据库
- **JWT** Token认证
- **BCrypt** 密码加密
- **Spring Security** 安全框架

## 项目结构

```
library-seat-system/
├── library-seat-system/              # 前端项目（Vue3）
│   ├── src/
│   │   ├── views/                  # 页面组件
│   │   │   ├── user/              # 用户端页面
│   │   │   │   ├── Home.vue        # 首页
│   │   │   │   ├── Login.vue      # 登录页
│   │   │   │   ├── Register.vue   # 注册页
│   │   │   │   ├── SeatMap.vue    # 座位地图（核心）
│   │   │   └── PersonalCenter.vue # 个人中心
│   │   │   └── admin/             # 管理员页面
│   │   │       ├── Dashboard.vue         # 管理仪表盘
│   │   │       ├── SeatManagement.vue    # 座位管理
│   │   │       ├── UserManagement.vue    # 用户管理
│   │   │       ├── ReservationManagement.vue  # 预约管理
│   │   │       └── ViolationManagement.vue   # 违规管理
│   │   ├── stores/                 # Pinia状态管理
│   │   │   ├── user.js           # 用户状态
│   │   │   ├── seats.js          # 座位状态
│   │   │   └── admin.js          # 管理员状态
│   │   ├── router/               # 路由配置
│   │   └── styles/               # 全局样式
│   ├── index.html
│   ├── vite.config.js            # Vite配置
│   └── package.json
│
├── library-backend/               # 后端项目（SpringBoot）
│   ├── src/main/java/com/library/
│   │   ├── controller/            # REST API控制器
│   │   │   ├── UserController.java
│   │   │   ├── SeatController.java
│   │   │   ├── ReservationController.java
│   │   │   └── ViolationController.java
│   │   ├── service/              # 业务逻辑层
│   │   │   ├── UserService.java
│   │   │   ├── SeatService.java
│   │   │   ├── ReservationService.java
│   │   │   └── ViolationService.java
│   │   ├── mapper/               # MyBatis数据访问层
│   │   ├── entity/               # 实体类
│   │   ├── config/               # 配置类（CORS、JWT等）
│   │   ├── interceptor/           # 拦截器
│   │   ├── scheduler/             # 定时任务
│   │   └── utils/                # 工具类
│   ├── src/main/resources/
│   │   └── application.yml       # 应用配置
│   ├── sql/
│   │   └── init.sql             # 数据库初始化脚本
│   └── pom.xml
│
└── README.md                    # 项目说明文档
```

## 功能特性

### 🎯 用户端功能

✅ **用户认证**
- 用户注册（用户名、密码、邮箱）
- 用户登录（JWT Token认证）
- 会话管理（自动登录状态保持）

✅ **可视化座位地图**
- **简约一体式座椅图标**设计（带靠背+两侧扶手）
- 楼层切换（1楼/2楼/3楼）
- 区域筛选（A/B/C/D四个区域）
- 状态筛选（空闲/已预约/使用中/暂离/不可用）
- 座位号搜索功能
- **按楼层独立统计**（实时显示各楼层座位状态）

✅ **座位预约**
- 楼层+区域+时段选择
- **一人一座限制**（同一用户只能预约一个座位）
- **30分钟签到宽限期**（超时自动取消预约）
- 实时预约状态更新

✅ **签到签退管理**
- 预约签到（支持宽限期提示）
- 暂离功能（离开座位时使用）
- 取消暂离（返回座位）
- 结束使用（签退）
- **任何状态下都可取消预约**（座位立即释放）

✅ **个人中心**
- 个人信息查看
- 预约记录管理
- 违约记录查询

### 🔧 管理员端功能

✅ **管理仪表盘**
- 实时座位状态统计
- 各楼层座位统计
- 违规记录统计
- **手动清零功能**（适合演示使用）

✅ **座位管理**
- 座位增删改查
- 座位状态管理
- 座位设施设置（电源插座、靠窗）

✅ **用户管理**
- 用户列表查看
- 用户状态管理

✅ **违规管理**
- 违规记录处理
- 违规原因统计

✅ **预约管理**
- 所有预约记录查看
- 预约状态管理

## 座位图标设计

### 简约一体式座椅

采用现代简约设计理念，每个座位由三部分组成：

```
    ┌─────────┐        ← 圆润靠背（顶部圆角15px）
    │         │
    │  A101  │        ← 座位号（白色字体）
    │ ⚡    🪟│        ← 功能图标（电源/靠窗）
────┤         ├────  ← 左右扶手（圆润设计）
    │         │
    └─────────┘        ← 座椅主体（底部圆角）
```

### 座位尺寸规格
- **靠背**：60px × 25px（15px顶部圆角）
- **座椅主体**：75px × 55px（8px圆角）
- **扶手**：8px × 40px（左右各一个）
- **整体**：80px × 85px（统一规格）

### 五种状态颜色

| 状态 | 颜色 | 说明 |
|------|------|------|
| 🟢 空闲 | 绿色渐变 (#81C784 → #66BB6A) | 可预约 |
| 🔵 已预约 | 蓝色渐变 (#64B5F6 → #42A5F5) | 已被预约 |
| 🟠 使用中 | 橙色渐变 (#FFB74D → #FFA726) | 正在使用 |
| ⚪ 暂离 | 灰色渐变 (#B0BEC5 → #90A4AE) | 暂时离开 |
| ⬜ 不可用 | 浅灰渐变 (#E0E0E0 → #BDBDBD) | 不可预约 |

### 交互效果

- **鼠标悬停**：整体上浮2px + 阴影加深
- **选中状态**：蓝色边框高亮 + 发光阴影
- **触摸交互**：流畅的触摸反馈

## 快速开始

### 前端启动（推荐）

```bash
# 进入前端目录
cd library-seat-system

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

**访问地址**：
- 电脑端：http://localhost:5174/
- 手机端：http://192.168.190.1:5174/（需同一WiFi网络）

### 后端启动

#### 1. 数据库配置

```bash
# 登录MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE library_seat_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 退出MySQL
EXIT;

# 导入数据
mysql -u root -p library_seat_system < library-backend/sql/init.sql
```

#### 2. 修改数据库配置

编辑 `library-backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/library_seat_system?useUnicode=true&characterEncoding=utf8
    username: root
    password: your_password
```

#### 3. 编译运行

```bash
# 进入后端目录
cd library-backend

# Maven编译打包
mvn clean package -DskipTests

# 运行JAR包
java -jar target/library-seat-system-1.0.0.jar
```

服务运行在：http://localhost:8080

## 测试账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | admin123 | 管理员权限，可访问管理后台 |
| 普通用户 | student001 | 123456 | 用户权限，基础功能 |
| 普通用户 | student002 | 123456 | 用户权限，基础功能 |
| 普通用户 | test | 123456 | 测试用户 |

## 核心业务流程

### 预约流程

```
用户登录 → 选择楼层/区域 → 选择座位 → 填写预约信息 → 预约成功
                                    ↓
                              等待签到（30分钟内）
                                    ↓
                    ┌───────────┴───────────┐
                    ↓                       ↓
                  签到成功              超时取消
                    ↓                       ↓
              座位状态：使用中        座位状态：空闲
                    ↓                 违约记录+1
              ┌───┴───┐
              ↓       ↓
           暂离     结束使用
              ↓       ↓
          座位状态：暂离    座位状态：空闲
              ↓
          返回使用
              ↓
          座位状态：使用中
```

### 座位状态流转

```
空闲 ←→ 已预约 → 使用中 ↔ 暂离
  ↑       ↓         ↓
  └───────┴─────────┘
           ↓
        已取消/已违约
```

## API 接口

### 用户接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/users/login` | POST | 用户登录 |
| `/api/users/register` | POST | 用户注册 |
| `/api/users/{id}` | GET | 获取用户信息 |

### 座位接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/seats` | GET | 获取所有座位 |
| `/api/seats/floor/{floor}` | GET | 按楼层获取座位 |
| `/api/seats/statistics` | GET | 获取统计信息 |
| `/api/seats/{id}` | POST | 添加座位 |
| `/api/seats/{id}/status` | PUT | 更新座位状态 |

### 预约接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/reservations` | POST | 创建预约 |
| `/api/reservations/{id}/check-in` | POST | 签到（含30分钟宽限期） |
| `/api/reservations/{id}/away` | POST | 暂离 |
| `/api/reservations/{id}/return` | POST | 取消暂离 |
| `/api/reservations/{id}/cancel` | POST | 取消预约（任何状态） |
| `/api/reservations/{id}/checkout` | POST | 结束使用 |
| `/api/reservations/reset` | POST | 管理员清零（演示用） |

## 数据库表结构

### sys_user（用户表）
- id, username, password, email, role, status, violations_count, create_time

### seat（座位表）
- id, seat_number, floor, area, status, has_power, is_window, create_time

### reservation（预约表）
- id, user_id, seat_id, date, start_time, end_time, status, check_in_time, cancel_time, create_time

### violation（违规表）
- id, user_id, seat_id, reservation_id, reason, status, handle_time, create_time

## 手机端适配

### 响应式设计

✅ **全局适配**
- Viewport配置优化
- 字体大小自适应（14px/13px）
- 输入框防缩放（16px字体）

✅ **座位地图适配**
- 楼层切换标签栏优化
- 座位网格自适应列数
- 触摸友好的点击区域（≥44px）

✅ **布局适配**
- 侧边栏变为顶部抽屉
- 卡片布局响应式
- 按钮尺寸适合触摸

### 手机端访问

确保手机和电脑连接同一WiFi，然后访问：

- 图书馆系统：http://192.168.190.1:5174/
- 购物系统（另一个项目）：http://192.168.190.1:5173/

## 项目亮点

### 1. 现代化UI设计
- 简约一体式座椅图标
- Element Plus组件库
- 流畅的动画效果
- 响应式布局

### 2. 业务逻辑完善
- 一人一座限制
- 30分钟签到宽限期
- 任何状态下可取消预约
- 违约记录管理

### 3. 演示友好
- 手动清零功能（一键恢复初始状态）
- 实时状态统计
- 清晰的操作反馈

### 4. 性能优化
- Vite快速热更新
- Pinia状态管理
- 响应式计算属性
- CSS硬件加速

## 开发说明

### 前端开发

```bash
# 代码规范检查
npm run lint

# 构建生产版本
npm run build
```

### 后端开发

```bash
# 运行测试
mvn test

# 只编译
mvn compile

# 跳过测试打包
mvn package -DskipTests
```

## 注意事项

1. **后端未编译**：由于Maven仓库连接超时，后端项目需要手动编译
2. **前端独立运行**：前端项目可以独立运行，使用本地模拟数据
3. **数据库依赖**：完整功能需要MySQL数据库支持
4. **端口占用**：确保5174端口未被占用

## License

MIT License

## 作者

基于 Vue 3 + Spring Boot 的现代化图书馆座位管理系统

## 更新日志

### v1.0.0 (2026-04)
- ✅ 完成前后端基础功能
- ✅ 实现座位预约核心逻辑
- ✅ 优化座位图标设计（简约一体式座椅）
- ✅ 添加手机端适配
- ✅ 实现楼层切换和按楼层统计
- ✅ 添加手动清零功能（演示用）
- ✅ 完善签到签退流程
