# 图书馆座位管理系统 - 预约逻辑优化说明

## 优化时间
2026-04-07

## 优化内容

### 1. 预约限制规则实现 ✅

#### 功能描述
- 限制单个用户（按登录账号唯一标识）同一时间仅能预约 **1个座位**
- 已存在未签到/使用中的预约时，禁止重复预约

#### 实现细节

##### 前端限制逻辑
**文件**: `src/views/user/SeatMap.vue`

1. **添加计算属性检测**
```javascript
const hasActiveReservation = computed(() => {
  const activeReservation = seatsStore.getActiveReservation(userStore.user?.id)
  return activeReservation !== null && activeReservation !== undefined
})
```

2. **获取当前预约信息**
```javascript
const getActiveReservationInfo = () => {
  const reservation = seatsStore.getActiveReservation(userStore.user?.id)
  if (reservation) {
    return `${reservation.seatId} (${reservation.date} ${reservation.startTime}-${reservation.endTime})`
  }
  return ''
}
```

3. **座位地图页面顶部显示**
- 已预约用户：显示"您已有预约：座位号 (日期 时段)"
- 未预约用户：显示"可预约座位：xxx"

4. **预约弹窗表单禁用**
- 日期选择器禁用
- 开始时间选择器禁用
- 结束时间选择器禁用
- 预约按钮禁用并显示"已有限约，不可重复预约"

5. **预约前双重检查**
```javascript
if (hasActiveReservation.value) {
  ElMessage.warning('您当前已预约座位，不可重复预约')
  return
}
```

### 2. 预约成功提示优化 ✅

#### 改进前
```
预约成功！
```

#### 改进后
```
预约成功！仅可预约1个座位，请按时签到后生效
```

**说明**:
- 提示时长延长至5秒（duration: 5000）
- 明确告知用户预约限制规则
- 提醒签到时间和生效条件

### 3. 预约记录保留 ✅

#### 功能说明
- 保留已取消预约的历史记录
- 已取消的预约不影响用户重新预约
- 历史记录可在个人中心查看

### 4. 用户端提示优化 ✅

#### 首页提示
**文件**: `src/views/user/Home.vue`

在座位使用情况上方添加规则提示：
```
预约规则：每位用户仅可预约1个座位，请按时签到后生效
```

#### 座位地图提示
**文件**: `src/views/user/SeatMap.vue`

1. 页面顶部显示当前预约状态
2. 预约弹窗中增加警告提示（当用户已有预约时）
3. 禁用已有限约用户的预约表单

### 5. 管理员端同步限制 ✅

#### 仪表盘提示
**文件**: `src/views/admin/Dashboard.vue`

在页面顶部添加系统规则提示：
```
系统规则：每位用户仅可预约1个座位，签到后生效
```

#### 座位管理提示
**文件**: `src/views/admin/SeatManagement.vue`

添加座位管理规则：
```
座位管理规则：每位用户同一时间仅可占用1个座位
```

#### 用户管理提示
**文件**: `src/views/admin/UserManagement.vue`

添加用户预约规则：
```
用户预约规则：每位用户同一时间仅可预约1个座位
```

## 技术实现

### 预约状态判断逻辑
系统根据预约状态判断是否为"有效预约"：

| 预约状态 | 是否算作有效预约 | 说明 |
|---------|----------------|------|
| pending | ✅ 是 | 待签到 |
| active | ✅ 是 | 使用中 |
| away | ✅ 是 | 暂离中 |
| completed | ❌ 否 | 已完成 |
| cancelled | ❌ 否 | 已取消 |
| violated | ❌ 否 | 已违约 |

### 预约流程限制
```
用户登录
    ↓
进入座位地图
    ↓
查看是否有有效预约
    ↓
    ├─ 有有效预约 → 禁用预约表单，显示提示
    │
    └─ 无有效预约 → 可正常预约
            ↓
        预约成功
            ↓
        提示：仅可预约1个座位，请按时签到后生效
```

## 用户体验优化

### 1. 清晰的预约状态提示
- 首页实时显示用户当前预约状态
- 座位地图顶部显示预约限制信息

### 2. 智能表单禁用
- 检测到已有预约时自动禁用预约表单
- 避免用户误操作

### 3. 友好的错误提示
- 重复预约时明确提示："您当前已预约座位，不可重复预约"
- 提供当前预约的详细信息

### 4. 管理员端透明度
- 所有管理页面同步显示预约规则
- 帮助管理员理解系统限制

## 文件修改清单

| 文件路径 | 修改类型 | 说明 |
|---------|---------|------|
| `src/views/user/SeatMap.vue` | 优化 | 座位地图页面预约限制 |
| `src/views/user/Home.vue` | 优化 | 首页预约规则提示 |
| `src/views/admin/Dashboard.vue` | 优化 | 管理员仪表盘规则提示 |
| `src/views/admin/SeatManagement.vue` | 优化 | 座位管理规则提示 |
| `src/views/admin/UserManagement.vue` | 优化 | 用户管理规则提示 |

## 测试验证

### 测试场景

#### 场景1: 未预约用户
1. 使用 student001 账号登录
2. 进入座位地图
3. 点击空闲座位
4. 填写预约信息并提交
5. **预期结果**: 预约成功，显示优化后的提示信息

#### 场景2: 已预约用户
1. 使用 student001 账号登录（该账号已有 pending 状态的预约）
2. 进入座位地图
3. **预期结果**: 顶部显示"您已有预约：xxx"
4. 点击任意空闲座位
5. **预期结果**: 
   - 弹窗中显示警告："您当前已有预约，不可重复预约"
   - 预约表单全部禁用
   - 预约按钮显示"已有限约，不可重复预约"

#### 场景3: 已完成预约用户
1. 使用 student002 账号登录（该账号预约已完成）
2. 进入座位地图
3. **预期结果**: 顶部显示"可预约座位：xxx"
4. 可正常进行预约操作

#### 场景4: 管理员视角
1. 使用 admin 账号登录
2. 进入任意管理页面
3. **预期结果**: 页面顶部显示预约规则提示

## 注意事项

### 1. 前端限制 vs 后端限制
- 本次优化为**前端限制**，提升用户体验
- 后端仍需实现对应的业务逻辑限制（在 `library-backend` 中）
- 建议后端在预约接口中也增加校验

### 2. 数据持久化
- 预约信息保存在 `localStorage` 中
- 刷新页面后数据保持
- 清除浏览器缓存会重置数据

### 3. 多人同时预约
- 前端限制无法防止并发预约
- 后端需要使用数据库锁或乐观锁保证数据一致性

## 后端优化建议

### 建议在后端实现以下校验

```java
// 在 ReservationService.createReservation() 中添加
public Reservation createReservation(...) {
    // 1. 检查用户是否已有有效预约
    Reservation existingReservation = this.getActiveReservation(userId);
    if (existingReservation != null) {
        throw new RuntimeException("您当前已预约座位，不可重复预约");
    }
    
    // 2. 检查座位是否可用
    Seat seat = seatService.getById(seatId);
    if (seat == null || !"available".equals(seat.getStatus())) {
        throw new RuntimeException("座位不可预约");
    }
    
    // 3. 继续原有逻辑...
}
```

### 数据库唯一性约束建议

```sql
-- 为防止并发问题，可在 reservation 表添加唯一索引
ALTER TABLE reservation 
ADD UNIQUE INDEX idx_user_active (user_id, status);

-- 或者使用触发器确保同一时间一个用户只有一个有效预约
```

## 总结

本次优化完成了以下目标：

✅ 实现了单用户单座位预约限制  
✅ 优化了预约成功提示信息  
✅ 保留了已取消预约的历史记录  
✅ 在用户端和管理员端同步显示限制规则  
✅ 提供了友好的用户体验

前端项目已更新并正在运行中，您可以在浏览器中访问 http://localhost:5173/ 查看效果。
