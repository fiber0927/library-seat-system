# 图书馆座位预约系统 Library Seat Reservation System

> 前后端分离的图书馆座位在线预约平台，解决座位资源分配不均与抢座冲突问题。
> 后端提供完整的预约业务与鉴权能力，前端实现了座位可视化选座与移动端适配的交互原型。
>
> Spring Boot 3 + MyBatis-Plus + MySQL + JWT ｜ Vue 3 + Pinia + Element Plus

## 技术栈

| 层级 | 技术 |
|---|---|
| 后端 | JDK 17 · Spring Boot 3.2.2 · **MyBatis-Plus 3.5.5**（条件构造器 + 全局逻辑删除）· Lombok |
| 鉴权 | **JWT**（jjwt 0.12.3，HS256）+ Spring MVC `HandlerInterceptor` · **BCrypt** 密码哈希 |
| 定时任务 | Spring Scheduling（`@Scheduled`）· MySQL 存储过程 + EVENT 定时事件 |
| 数据库 | MySQL 8（InnoDB / utf8mb4）· HikariCP 连接池 |
| 前端 | Vue 3（Composition API）· Vue Router 4 · Pinia · Element Plus · Vite 5 |

规模：后端 23 个 Java 文件 / 1617 行，前端 17 个文件 / 5093 行，34 个 REST 接口，4 张表。

## 功能模块

### 用户端

| 模块 | 说明 |
|---|---|
| 注册 / 登录 | 用户名 + 密码 + 邮箱注册，登录签发 JWT |
| 可视化座位地图 | CSS Grid 布局的座位图，楼层 / 区域 / 状态三维筛选，按楼层统计 |
| 座位预约 | 选择楼层 + 区域 + 时段，**一人一座限制** + **时段重叠检测** |
| 签到 / 暂离 / 签退 | 30 分钟签到宽限期；暂离与返回；结束使用 |
| 个人中心 | 预约记录、违约记录查询 |

### 管理员端

| 模块 | 说明 |
|---|---|
| 数据仪表盘 | 座位状态统计、违约统计 |
| 座位管理 | 座位增删改查、状态维护、设施（电源 / 靠窗）设置 |
| 用户管理 | 用户列表、账号启用 / 禁用 |
| 预约管理 | 全部预约记录查询、按状态筛选 |
| 违约管理 | 违约记录处理、违约原因统计 |

## 核心业务设计

### 1. 两套状态机

座位和预约是**两个不同维度**，所以各有一套状态：

| `seat.status` | 含义 | | `reservation.status` | 含义 |
|---|---|---|---|---|
| `available` | 空闲，可预约 | | `pending` | 待签到 |
| `reserved` | 已被预约 | | `active` | 使用中 |
| `in_use` | 使用中 | | `away` | 暂离 |
| `away` | 暂离 | | `violated` | 已违约（超时未签到） |
| `disabled` | 不可用（设备故障） | | | |

两套状态不是一一对应的：**超时违约**这条路径上，预约变成 `violated`，而座位直接回到 `available`。

### 2. 30 分钟签到宽限期

宽限期从**预约开始时间**起算（不是从下单时间起算），时长可配置：

```yaml
system:
  check-in-grace-period: 30   # 签到宽限期（分钟）
```

- 宽限期内签到 → 预约 `active`、座位 `in_use`
- 超过宽限期签到 → 预约 `violated`、释放座位、生成违约记录、违约次数 +1
- 定时任务也会对超时的 `pending` 预约做同样的处理

### 3. 超时清理：应用层 + 数据库层双保险

**应用层**（`ReservationCleanupTask`，`@Scheduled(fixedRate = 60000)`）：
每 60 秒扫一次超时的 `pending` 预约，改 `violated`、释放座位、写违约记录、违约次数 +1。

**数据库层**（`sql/init.sql`）：
另写了一个存储过程 + MySQL EVENT 事件，即使应用进程挂掉，数据库也会把超时预约清掉：

```sql
CREATE PROCEDURE cleanup_expired_reservations()
    UPDATE reservation r INNER JOIN seat s ON r.seat_id = s.id
    SET r.status='violated', s.status='available', s.user_id=NULL
    WHERE r.status='pending' AND r.date=CURDATE()
      AND ADDTIME(r.date, r.start_time) < DATE_SUB(NOW(), INTERVAL GRACE_PERIOD MINUTE);
```

还建了两个统计视图：`v_seat_statistics`（楼层维度座位使用率）、`v_user_reservation_stats`（用户预约统计）。

### 4. 鉴权与权限

- **JWT 无状态认证**：`JwtUtils` 签发（HS256，有效期 24 小时），`JwtInterceptor` 校验，
  把 `userId` / `username` / `role` 放进请求上下文（`HttpServletRequest.setAttribute`）
- **接口级权限**：管理员接口校验 `role`，普通用户只能访问自己的数据，越权返回 403
- **密码**：BCrypt 加盐哈希（`BCryptPasswordEncoder`），数据库里不存明文
- **逻辑删除**：MyBatis-Plus 全局配置，删除即 `deleted = 1`

### 5. 开发过程中修掉的真实 Bug

`开发过程.md文件/` 里记录了带**前后代码对比**的 5 个问题，其中两个值得一看：

**① 座位显示空闲，但后端说已被预约**
取消预约时原本只把预约状态改成 `cancelled`、座位改成 `available`，
但**预约记录还在**，而时段冲突检测查的就是预约表 —— 于是座位"看着能约，其实约不上"。
修法是改成**物理删除**预约记录，并且固定"先释放座位、再删记录"的顺序。

**② 暂离状态下无法取消，座位永久卡死**
原代码禁止在 `active` / `away` 状态取消预约，用户在暂离时点取消被拒，
座位就永远停在"已预约"。改成**任何状态都能取消且都释放座位**，并按原状态返回不同的提示文案。

## 数据库设计

4 张表，`utf8mb4` + InnoDB，索引齐备：

| 表 | 说明 | 关键索引 |
|---|---|---|
| `sys_user` | 用户 | `username` UNIQUE、`email` UNIQUE、`role`、`deleted` |
| `seat` | 座位（主键就是座位号，如 `A101`） | `floor`、`area`、`status`、`user_id`、`deleted` |
| `reservation` | 预约 | `seat_id`、`user_id`、`date`、`status`、**(seat_id, date, status)** |
| `violation` | 违约记录 | `user_id`、`seat_id`、`status`、`date` |

座位按 **3 层 × 4 区域（A/B/C/D）** 布局：每层 28 个（A / B / C 区各 8 个 + D 区 4 个），共 **84 个**。
区域对应不同设施：A 区电源插座 + 靠窗、B 区空调、C 区安静区、D 区讨论区，3 楼在 1 楼设施基础上追加"安静区"。

初始化脚本：`library-seat-system/library-backend/sql/init.sql`（含建表、种子数据、存储过程、事件、视图）。

### 演示账号

| 角色 | 用户名 | 密码 |
|---|---|---|
| 管理员 | `admin` | `123456` |
| 普通用户 | `student001` / `student002` / `student003` | `123456` |

## 项目结构

```
library-seat-system/
├── library-seat-system/                 # 前端 Vue 3
│   ├── src/
│   │   ├── views/
│   │   │   ├── user/                   # 用户端：Home / Login / Register / SeatMap / PersonalCenter
│   │   │   └── admin/                  # 管理端：Dashboard / Seat / User / Reservation / Violation
│   │   ├── stores/                     # Pinia：user / seats / admin
│   │   ├── router/index.js             # 路由 + 登录守卫
│   │   └── styles/global.css
│   ├── vite.config.js                  # 端口 5174
│   └── package.json
│
├── library-seat-system/library-backend/ # 后端 Spring Boot
│   ├── src/main/java/com/library/
│   │   ├── common/Result.java          # 统一响应体
│   │   ├── config/                     # WebMvcConfig（注册拦截器）、CorsConfig
│   │   ├── controller/                 # User / Seat / Reservation / Violation
│   │   ├── entity/                     # 4 个实体
│   │   ├── interceptor/JwtInterceptor  # JWT 校验
│   │   ├── mapper/                     # 4 个 BaseMapper（无 XML）
│   │   ├── scheduler/                  # ReservationCleanupTask
│   │   ├── service/                    # 业务逻辑
│   │   └── utils/JwtUtils
│   ├── src/main/resources/application.yml
│   └── sql/init.sql
│
└── 开发过程.md文件/                     # 13 份开发过程记录（含前后代码对比的踩坑复盘）
```

## 快速启动

### 环境要求

JDK 17+ · Maven 3.9+ · MySQL 8.0+ · Node.js 18+

### 1. 建库

```bash
mysql -u root -p < library-seat-system/library-backend/sql/init.sql
```

脚本会创建 `library_seat_system` 库、4 张表、84 个座位、4 个演示账号，
以及存储过程 / EVENT 事件 / 2 个统计视图。
脚本开头已写 `SET NAMES utf8mb4;`——因为脚本本身是 UTF-8，而 MySQL 客户端默认字符集可能是 gbk，
不显式指定会导致中文数据（座位特征、违约原因）乱码甚至导入失败。
**EVENT 事件需要 `event_scheduler` 开启**（脚本里已执行 `SET GLOBAL event_scheduler = ON`，
若你的 MySQL 启动时未开启，可以手动执行一次）。

### 2. 配置数据库连接与 JWT 密钥

仓库里**不包含数据库密码和 JWT 密钥**。克隆后需要自己新建
`library-seat-system/library-backend/src/main/resources/application-local.yml`：

```yaml
spring:
  datasource:
    password: 你的数据库密码

jwt:
  secret: 你的 JWT 密钥（至少 32 字节）
```

这个文件已在 `.gitignore` 里，不会被提交。
`application.yml` 中通过 `spring.profiles.active: local` 自动加载它，
所以**不需要改任何启动命令**。

### 3. 启动后端

```bash
cd library-seat-system/library-backend
mvn spring-boot:run
```

服务地址 `http://localhost:8080`，接口前缀 `/api`（`server.servlet.context-path: /api`）。

### 4. 启动前端

```bash
cd library-seat-system
npm install
npm run dev
```

访问 `http://localhost:5174`；手机同 WiFi 下用 `http://<本机IP>:5174` 访问。

## API 一览

共 34 个接口，前缀 `/api`。除登录注册外均需携带 `Authorization: Bearer <token>`。

| 模块 | 方法与路径 | 说明 |
|---|---|---|
| 用户 | `POST /users/login`、`POST /users/register` | 登录 / 注册（免鉴权） |
| | `GET /users/current` | 当前登录用户 |
| | `GET /users/{id}`、`PUT /users/{id}` | 用户信息查询 / 修改 |
| | `GET /users` | 用户列表（管理员） |
| | `PUT /users/{id}/toggle-enabled` | 启用 / 禁用账号（管理员） |
| 座位 | `GET /seats`、`GET /seats/{id}` | 座位列表 / 详情 |
| | `GET /seats/floor/{floor}`、`/seats/floor/{floor}/area/{area}`、`/seats/available` | 按楼层 / 区域 / 空闲筛选 |
| | `GET /seats/statistics`、`/seats/statistics/floor/{floor}` | 座位统计 |
| | `POST /seats`、`PUT /seats/{id}/status`、`DELETE /seats/{id}` | 座位增删改（管理员） |
| 预约 | `POST /reservations` | 创建预约（含一人一座 + 时段冲突校验） |
| | `POST /reservations/{id}/check-in`、`/away`、`/return`、`/cancel`、`/checkout` | 签到 / 暂离 / 返回 / 取消 / 签退 |
| | `GET /reservations/user/{userId}`、`/reservations/user/{userId}/active` | 我的预约 / 当前预约 |
| | `GET /reservations`、`/reservations/status/{status}`、`/reservations/date/{date}` | 全部预约查询（管理员） |
| 违约 | `GET /violations`、`/violations/pending`、`/violations/statistics` | 违约查询与统计（管理员） |
| | `GET /violations/user/{userId}` | 我的违约记录 |
| | `POST /violations/{id}/handle`、`DELETE /violations/{id}` | 处理 / 删除违约（管理员） |

## 已知限制

写在这里是为了避免误解，也方便后续补齐：

- **前端目前是交互原型，尚未联调后端**。`src/stores/seats.js` 用 `generateMockSeats()`
  在浏览器里生成座位、状态随机分配，预约记录存在 localStorage；
  后端 34 个接口已经写完但前端一次都没调用过。
  前端的"一键清零"也只是清本地 mock 数据，并没有调用后端。
- **没有并发控制**。预约链路是"先查后写"（查座位状态 → 查一人一座 → 查时段冲突 → 写入），
  没有分布式锁、没有唯一索引、没有乐观锁，两个并发请求可能同时通过校验造成重复预约。
  正确做法是**条件更新**（`UPDATE seat SET status=? WHERE id=? AND status='available'`）
  加**唯一索引**兜底，而不是引入 Redis 分布式锁。
- **座位状态不分日期**。`seat.status` 是座位级的全局状态，所以今天约了 A101，
  明天其他人也约不了它。正确做法是把"座位瞬时状态"和"时段占用"拆成两张表。
- **时段重叠判定用的是闭区间**（`start_time <= end AND end_time >= start`），
  首尾相接的两个时段会被误判为冲突，应改成半开区间。
- **没有全局异常处理**，也没有 `@Valid` 参数校验（依赖引了但没用），参数格式错误会直接 500。
- **没有单元测试**，也没有接口文档。
- **JWT 密钥硬编码在 `application.yml`**，且没有黑名单 / 刷新机制，
  账号被禁用后旧 token 在有效期内仍然可用。

## License

MIT
