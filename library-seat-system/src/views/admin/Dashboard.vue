<template>
  <div class="admin-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h1>管理后台</h1>
          <div class="user-info">
            <el-dropdown @command="handleCommand">
              <span class="user-dropdown">
                <el-icon><User /></el-icon>
                {{ userStore.user?.username }}
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="home">返回首页</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <el-container>
        <el-aside width="200px" class="sidebar">
          <el-menu
            :default-active="activeMenu"
            router
          >
            <el-menu-item index="/admin">
              <el-icon><DataAnalysis /></el-icon>
              <span>仪表盘</span>
            </el-menu-item>
            <el-menu-item index="/admin/reservations">
              <el-icon><Calendar /></el-icon>
              <span>预约管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/seats">
              <el-icon><Grid /></el-icon>
              <span>座位管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/users">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/violations">
              <el-icon><Warning /></el-icon>
              <span>违规管理</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <el-main class="main-content">
          <div class="dashboard">
            <el-row :gutter="20">
              <el-col :span="24">
                <el-alert
                  title="系统规则：每位用户仅可预约1个座位，签到后生效"
                  type="warning"
                  :closable="false"
                  show-icon
                />
              </el-col>
            </el-row>

            <el-row :gutter="20" style="margin-top: 10px;">
              <el-col :span="24">
                <el-alert
                  title="签到规则：预约时段开始后30分钟为签到宽限期，超时预约自动取消"
                  type="info"
                  :closable="false"
                  show-icon
                />
              </el-col>
            </el-row>

            <el-row :gutter="20" style="margin-top: 20px;">
              <el-col :xs="24" :sm="12" :md="6">
                <el-card class="stat-card">
                  <div class="stat-content">
                    <div class="stat-icon" style="background-color: #409eff;">
                      <el-icon :size="30"><Grid /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-value">{{ seatsStore.statistics.total }}</div>
                      <div class="stat-label">总座位数</div>
                    </div>
                  </div>
                </el-card>
              </el-col>

              <el-col :xs="24" :sm="12" :md="6">
                <el-card class="stat-card">
                  <div class="stat-content">
                    <div class="stat-icon" style="background-color: #67c23a;">
                      <el-icon :size="30"><CircleCheck /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-value">{{ seatsStore.statistics.available }}</div>
                      <div class="stat-label">空闲座位</div>
                    </div>
                  </div>
                </el-card>
              </el-col>

              <el-col :xs="24" :sm="12" :md="6">
                <el-card class="stat-card">
                  <div class="stat-content">
                    <div class="stat-icon" style="background-color: #e6a23c;">
                      <el-icon :size="30"><Clock /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-value">{{ seatsStore.statistics.inUse + seatsStore.statistics.reserved }}</div>
                      <div class="stat-label">使用中/已预约</div>
                    </div>
                  </div>
                </el-card>
              </el-col>

              <el-col :xs="24" :sm="12" :md="6">
                <el-card class="stat-card">
                  <div class="stat-content">
                    <div class="stat-icon" style="background-color: #f56c6c;">
                      <el-icon :size="30"><Warning /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-value">{{ adminStore.violationStatistics.pending }}</div>
                      <div class="stat-label">待处理违规</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <el-row :gutter="20" style="margin-top: 20px;">
              <el-col :span="24">
                <el-card class="reset-card">
                  <template #header>
                    <div class="card-header">
                      <span>管理员操作</span>
                    </div>
                  </template>
                  <div class="admin-actions">
                    <el-alert
                      title="手动清零说明：点击后将清空所有当日预约记录，所有座位状态重置为「空闲」"
                      type="warning"
                      :closable="false"
                      show-icon
                      style="margin-bottom: 15px;"
                    />
                    <el-button
                      type="danger"
                      size="large"
                      :loading="resetLoading"
                      @click="handleResetAll"
                      class="reset-button"
                    >
                      <el-icon><Delete /></el-icon>
                      一键清零（演示用）
                    </el-button>
                    <el-tag type="info" style="margin-left: 15px;">
                      适合课程作业演示，一键恢复初始状态
                    </el-tag>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <el-row :gutter="20" style="margin-top: 20px;">
              <el-col :span="24">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>座位实时状态</span>
                      <el-tag type="success">使用率: {{ seatsStore.statistics.usageRate }}%</el-tag>
                    </div>
                  </template>
                  <div class="status-chart">
                    <div class="status-bar">
                      <div
                        class="status-segment available"
                        :style="{ width: getPercentage(seatsStore.statistics.available) }"
                      ></div>
                      <div
                        class="status-segment reserved"
                        :style="{ width: getPercentage(seatsStore.statistics.reserved) }"
                      ></div>
                      <div
                        class="status-segment in-use"
                        :style="{ width: getPercentage(seatsStore.statistics.inUse) }"
                      ></div>
                      <div
                        class="status-segment away"
                        :style="{ width: getPercentage(seatsStore.statistics.away) }"
                      ></div>
                      <div
                        class="status-segment disabled"
                        :style="{ width: getPercentage(seatsStore.statistics.disabled) }"
                      ></div>
                    </div>
                    <div class="status-legend">
                      <div class="legend-item">
                        <span class="legend-color available"></span>
                        <span>空闲 ({{ seatsStore.statistics.available }})</span>
                      </div>
                      <div class="legend-item">
                        <span class="legend-color reserved"></span>
                        <span>已预约 ({{ seatsStore.statistics.reserved }})</span>
                      </div>
                      <div class="legend-item">
                        <span class="legend-color in-use"></span>
                        <span>使用中 ({{ seatsStore.statistics.inUse }})</span>
                      </div>
                      <div class="legend-item">
                        <span class="legend-color away"></span>
                        <span>暂离 ({{ seatsStore.statistics.away }})</span>
                      </div>
                      <div class="legend-item">
                        <span class="legend-color disabled"></span>
                        <span>不可用 ({{ seatsStore.statistics.disabled }})</span>
                      </div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <el-row :gutter="20" style="margin-top: 20px;">
              <el-col :xs="24" :lg="12">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>各楼层座位统计</span>
                    </div>
                  </template>
                  <div class="floor-stats">
                    <div
                      v-for="floor in seatsStore.floors"
                      :key="floor"
                      class="floor-stat-item"
                    >
                      <div class="floor-label">{{ floor }}楼</div>
                      <div class="floor-bars">
                        <div
                          v-for="(count, status) in getFloorStats(floor)"
                          :key="status"
                          class="floor-bar-segment"
                          :class="`status-${status}`"
                          :style="{ width: getFloorPercentage(floor, count) }"
                          :title="`${getStatusLabel(status)}: ${count}`"
                        ></div>
                      </div>
                      <div class="floor-total">{{ getFloorTotal(floor) }}</div>
                    </div>
                  </div>
                </el-card>
              </el-col>

              <el-col :xs="24" :lg="12">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>违规统计</span>
                    </div>
                  </template>
                  <div class="violation-stats">
                    <el-descriptions :column="1" border>
                      <el-descriptions-item label="总违规数">
                        {{ adminStore.violationStatistics.total }}
                      </el-descriptions-item>
                      <el-descriptions-item label="待处理">
                        <el-tag type="warning">
                          {{ adminStore.violationStatistics.pending }}
                        </el-tag>
                      </el-descriptions-item>
                      <el-descriptions-item label="已处理">
                        <el-tag type="success">
                          {{ adminStore.violationStatistics.handled }}
                        </el-tag>
                      </el-descriptions-item>
                    </el-descriptions>

                    <div class="reason-stats">
                      <h4>违规原因分布</h4>
                      <div
                        v-for="(count, reason) in adminStore.violationStatistics.reasonCount"
                        :key="reason"
                        class="reason-item"
                      >
                        <span>{{ reason }}</span>
                        <el-tag type="danger">{{ count }}</el-tag>
                      </div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useSeatsStore } from '@/stores/seats'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const userStore = useUserStore()
const seatsStore = useSeatsStore()
const adminStore = useAdminStore()

const activeMenu = ref('/admin')
const resetLoading = ref(false)

const getPercentage = (count) => {
  const total = seatsStore.statistics.total
  return `${(count / total * 100).toFixed(1)}%`
}

const getFloorStats = (floor) => {
  const floorSeats = seatsStore.seats.filter(s => s.floor === floor)
  return {
    available: floorSeats.filter(s => s.status === 'available').length,
    reserved: floorSeats.filter(s => s.status === 'reserved').length,
    in_use: floorSeats.filter(s => s.status === 'in_use').length,
    away: floorSeats.filter(s => s.status === 'away').length,
    disabled: floorSeats.filter(s => s.status === 'disabled').length
  }
}

const getFloorTotal = (floor) => {
  return seatsStore.seats.filter(s => s.floor === floor).length
}

const getFloorPercentage = (floor, count) => {
  const total = getFloorTotal(floor)
  return `${(count / total * 100).toFixed(1)}%`
}

const getStatusLabel = (status) => {
  const labels = {
    available: '空闲',
    reserved: '已预约',
    in_use: '使用中',
    away: '暂离',
    disabled: '不可用'
  }
  return labels[status] || status
}

const handleCommand = (command) => {
  if (command === 'home') {
    router.push('/home')
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}

const handleResetAll = async () => {
  try {
    await ElMessageBox.confirm(
      '此操作将清空所有当日预约记录，所有座位状态重置为「空闲」。是否继续？',
      '警告',
      {
        confirmButtonText: '确定清零',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    resetLoading.value = true

    setTimeout(() => {
      seatsStore.resetAllSeats()
      adminStore.clearReservations()
      localStorage.removeItem('reservations')

      resetLoading.value = false
      ElMessage.success('清零成功！所有座位已重置为「空闲」状态')
    }, 500)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清零失败，请重试')
      resetLoading.value = false
    }
  }
}
</script>

<style scoped>
.admin-container {
  width: 100%;
  min-height: 100vh;
}

.header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h1 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: #f5f7fa;
}

.sidebar {
  background-color: #fff;
  min-height: calc(100vh - 60px);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
}

.main-content {
  padding: 20px;
  background-color: #f5f7fa;
}

.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.stat-card {
  margin-bottom: 20px;
}

.reset-card {
  border: 2px solid #f56c6c;
  background: linear-gradient(135deg, #fef0f0 0%, #fff 100%);
}

.admin-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.reset-button {
  font-weight: bold;
  padding: 12px 24px;
  font-size: 16px;
}

.reset-button:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.4);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-chart {
  padding: 20px 0;
}

.status-bar {
  display: flex;
  height: 40px;
  border-radius: 8px;
  overflow: hidden;
  background-color: #e0e0e0;
}

.status-segment {
  height: 100%;
  transition: width 0.3s ease;
}

.status-segment.available {
  background-color: #67c23a;
}

.status-segment.reserved {
  background-color: #409eff;
}

.status-segment.in-use {
  background-color: #e6a23c;
}

.status-segment.away {
  background-color: #909399;
}

.status-segment.disabled {
  background-color: #c0c4cc;
}

.status-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-top: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
}

.legend-color.available {
  background-color: #67c23a;
}

.legend-color.reserved {
  background-color: #409eff;
}

.legend-color.in-use {
  background-color: #e6a23c;
}

.legend-color.away {
  background-color: #909399;
}

.legend-color.disabled {
  background-color: #c0c4cc;
}

.floor-stats {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.floor-stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.floor-label {
  width: 50px;
  font-weight: bold;
  color: #333;
}

.floor-bars {
  flex: 1;
  display: flex;
  height: 24px;
  border-radius: 4px;
  overflow: hidden;
  background-color: #f0f0f0;
}

.floor-bar-segment {
  height: 100%;
  transition: width 0.3s ease;
}

.floor-bar-segment.status-available {
  background-color: #67c23a;
}

.floor-bar-segment.status-reserved {
  background-color: #409eff;
}

.floor-bar-segment.status-in_use {
  background-color: #e6a23c;
}

.floor-bar-segment.status-away {
  background-color: #909399;
}

.floor-bar-segment.status-disabled {
  background-color: #c0c4cc;
}

.floor-total {
  width: 40px;
  text-align: right;
  color: #666;
}

.violation-stats {
  padding: 10px 0;
}

.reason-stats {
  margin-top: 20px;
}

.reason-stats h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.reason-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

@media (max-width: 768px) {
  .sidebar {
    display: none;
  }
}
</style>
