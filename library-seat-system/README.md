# 图书馆座位管理系统 - 前端

## 项目简介

这是一个基于 Vue3 + Element Plus + Vite 开发的图书馆座位管理系统前端应用。

## 技术栈

- **Vue 3** - 渐进式JavaScript框架
- **Vite 5** - 新一代前端构建工具
- **Element Plus** - 基于 Vue 3 的组件库
- **Vue Router 4** - Vue.js 官方路由管理器
- **Pinia** - Vue.js 的状态管理库

## 功能特性

### 用户端功能
- ✅ 用户登录注册
- ✅ 可视化座位平面图展示
- ✅ 不同颜色区分座位状态（空闲、已预约、使用中、暂离）
- ✅ 按楼层和区域筛选座位
- ✅ 预约指定时段座位
- ✅ 签到、暂离、取消预约
- ✅ 个人中心查看预约记录与违约信息

### 管理员端功能
- ✅ 管理仪表盘统计
- ✅ 座位实时状态管理
- ✅ 用户管理
- ✅ 违规记录处理

## 快速开始

### 安装依赖
```bash
npm install
```

### 启动开发服务器
```bash
npm run dev
```

### 构建生产版本
```bash
npm run build
```

### 预览生产版本
```bash
npm run preview
```

## 测试账号

- **管理员账号**: admin / admin123
- **普通用户账号**: student001 / 123456

## 项目结构

```
library-seat-system/
├── src/
│   ├── assets/           # 静态资源
│   ├── components/       # 公共组件
│   ├── views/           # 页面视图
│   │   ├── user/       # 用户端页面
│   │   └── admin/      # 管理员端页面
│   ├── stores/         # Pinia 状态管理
│   ├── router/         # 路由配置
│   ├── utils/          # 工具函数
│   ├── styles/         # 全局样式
│   ├── App.vue
│   └── main.js
├── public/
├── index.html
├── vite.config.js
└── package.json
```

## 座位状态说明

- 🟢 **空闲** - 绿色，可预约
- 🔵 **已预约** - 蓝色，已被预约
- 🟠 **使用中** - 橙色，正在使用
- ⚫ **暂离** - 灰色，使用者暂离
- 🔴 **不可用** - 深灰色，暂不可用

## 响应式设计

- 桌面端: >= 1200px
- 平板端: 768px - 1199px
- 移动端: < 768px

## License

MIT
