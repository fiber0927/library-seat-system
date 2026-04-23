# 图书馆座位管理系统 - 开发计划

## 项目概述
开发一套完整的图书馆座位管理系统前端应用，采用 Vue3 + Element Plus + Vite 技术栈，实现用户端和管理员端的完整功能。

## 技术栈
- **框架**: Vue 3 (Composition API + script setup)
- **构建工具**: Vite 5
- **UI 组件库**: Element Plus
- **路由**: Vue Router 4
- **状态管理**: Pinia
- **样式**: CSS3 + 响应式设计

## 项目结构
```
library-seat-system/
├── public/
├── src/
│   ├── assets/           # 静态资源
│   ├── components/       # 公共组件
│   │   ├── FloorPlan.vue       # 座位平面图组件
│   │   ├── SeatCard.vue        # 座位卡片组件
│   │   ├── SeatGrid.vue        # 座位网格组件
│   │   └── StatusLegend.vue    # 状态图例组件
│   ├── views/            # 页面视图
│   │   ├── user/         # 用户端页面
│   │   │   ├── Login.vue
│   │   │   ├── Register.vue
│   │   │   ├── Home.vue
│   │   │   ├── SeatMap.vue
│   │   │   └── PersonalCenter.vue
│   │   └── admin/        # 管理员端页面
│   │       ├── Dashboard.vue
│   │       ├── SeatManagement.vue
│   │       ├── UserManagement.vue
│   │       └── ViolationManagement.vue
│   ├── stores/           # Pinia 状态管理
│   │   ├── user.js
│   │   ├── seats.js
│   │   └── admin.js
│   ├── router/           # 路由配置
│   ├── utils/            # 工具函数
│   ├── styles/           # 全局样式
│   ├── App.vue
│   └── main.js
├── index.html
├── vite.config.js
├── package.json
└── README.md
```

## 功能模块详细规划

### 1. 用户认证模块
**页面**:
- `Login.vue` - 用户登录页面
- `Register.vue` - 用户注册页面

**功能**:
- 用户名/密码登录
- 邮箱/手机号注册
- 表单验证
- 登录状态持久化 (localStorage)

### 2. 座位地图可视化模块
**组件**:
- `FloorPlan.vue` - 楼层平面图主组件
- `SeatCard.vue` - 座位卡片组件
- `SeatGrid.vue` - 座位网格布局
- `StatusLegend.vue` - 状态图例

**座位状态定义**:
- 🟢 **空闲** (available) - 绿色 #67C23A
- 🔵 **已预约** (reserved) - 蓝色 #409EFF  
- 🟠 **使用中** (in_use) - 橙色 #E6A23C
- ⚫ **暂离** (away) - 灰色 #909399
- 🔴 **不可用** (disabled) - 深灰色 #C0C4CC

**筛选功能**:
- 按楼层筛选 (1F, 2F, 3F)
- 按区域筛选 (A区, B区, C区, D区)
- 按状态筛选 (空闲, 已预约, 使用中, 暂离)
- 搜索座位号

### 3. 预约功能模块
**功能**:
- 查看座位详情
- 选择预约时段 (08:00-22:00)
- 确认预约
- 签到功能 (预约成功后需在15分钟内签到)
- 暂离功能 (最长30分钟)
- 取消预约
- 查看当前预约状态

### 4. 个人中心模块
**页面**: `PersonalCenter.vue`

**功能**:
- 查看个人信息
- 预约记录列表
  - 显示：座位号、预约时间、时段、状态
  - 支持时间范围筛选
- 违约信息
  - 显示违约记录
  - 违约次数统计
- 账户设置

### 5. 管理员端模块
**页面**:
- `Dashboard.vue` - 管理仪表盘
- `SeatManagement.vue` - 座位管理
- `UserManagement.vue` - 用户管理
- `ViolationManagement.vue` - 违规记录管理

**功能**:
- **仪表盘**: 
  - 今日座位使用统计
  - 实时座位状态分布
  - 预约统计图表
  
- **座位管理**:
  - 查看所有座位状态
  - 添加/编辑/删除座位
  - 设置座位可用性
  - 批量操作
  
- **用户管理**:
  - 查看用户列表
  - 搜索用户
  - 禁用/启用用户
  - 查看用户详情和违规记录
  
- **违规管理**:
  - 查看所有违规记录
  - 处理违规
  - 违规统计

## 响应式设计
**断点**:
- 桌面端: >= 1200px
- 平板端: 768px - 1199px
- 移动端: < 768px

**适配策略**:
- 座位网格在移动端自适应缩放
- 侧边栏在移动端变为底部导航
- 筛选面板在移动端变为抽屉组件
- 座位信息在移动端以卡片形式展示

## Mock 数据设计
为了演示功能，将使用本地模拟数据：

### 座位数据结构
```javascript
{
  id: 'A101',
  floor: 1,
  area: 'A',
  row: 1,
  col: 1,
  status: 'available', // available|reserved|in_use|away|disabled
  features: ['电源插座', '靠窗']
}
```

### 用户数据结构
```javascript
{
  id: 1,
  username: 'student001',
  email: 'student@example.com',
  phone: '13800138000',
  role: 'user', // user|admin
  violations: 0,
  reservations: []
}
```

### 预约数据结构
```javascript
{
  id: 1,
  seatId: 'A101',
  userId: 1,
  date: '2026-04-07',
  startTime: '09:00',
  endTime: '12:00',
  status: 'active', // active|completed|cancelled|violated
  checkInTime: '09:02',
  checkOutTime: null
}
```

## 开发阶段

### 阶段1: 项目初始化 (预计时间: 30分钟)
- [ ] 初始化 Vite + Vue3 项目
- [ ] 安装 Element Plus、Vue Router、Pinia
- [ ] 配置项目结构
- [ ] 设置路由

### 阶段2: 用户认证 (预计时间: 1小时)
- [ ] 实现登录页面
- [ ] 实现注册页面
- [ ] 实现表单验证
- [ ] 实现登录状态管理

### 阶段3: 座位可视化 (预计时间: 2小时)
- [ ] 设计楼层平面图布局
- [ ] 实现座位组件
- [ ] 实现筛选功能
- [ ] 实现状态显示

### 阶段4: 预约功能 (预计时间: 1.5小时)
- [ ] 实现座位详情弹窗
- [ ] 实现时段选择器
- [ ] 实现预约流程
- [ ] 实现签到/暂离/取消

### 阶段5: 个人中心 (预计时间: 1小时)
- [ ] 实现个人信息展示
- [ ] 实现预约记录列表
- [ ] 实现违约信息展示

### 阶段6: 管理员端 (预计时间: 2小时)
- [ ] 实现管理仪表盘
- [ ] 实现座位管理页面
- [ ] 实现用户管理页面
- [ ] 实现违规管理页面

### 阶段7: 响应式优化 (预计时间: 1小时)
- [ ] 适配平板端
- [ ] 适配移动端
- [ ] 优化交互体验

### 阶段8: 测试与优化 (预计时间: 1小时)
- [ ] 功能测试
- [ ] 界面检查
- [ ] 性能优化

## 预估总时间
- **总计**: 约 10 小时

## 预期成果
1. 完整的用户端功能（登录注册、座位预约、个人中心）
2. 完整的管理员端功能（座位管理、用户管理、违规管理）
3. 响应式的用户界面
4. 可直接在浏览器中预览的完整应用
