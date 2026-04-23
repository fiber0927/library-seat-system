<template>
  <div class="home-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h1>图书馆座位管理系统</h1>
          <div class="user-info">
            <el-dropdown @command="handleCommand">
              <span class="user-dropdown">
                <el-icon><User /></el-icon>
                {{ userStore.user?.username }}
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="personal">个人中心</el-dropdown-item>
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
            @select="handleSelect"
          >
            <el-menu-item index="/home">
              <el-icon><House /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/seat-map">
              <el-icon><Grid /></el-icon>
              <span>座位地图</span>
            </el-menu-item>
            <el-menu-item index="/personal">
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.isAdmin" index="/admin">
              <el-icon><Setting /></el-icon>
              <span>管理后台</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <el-main class="main-content">
          <div class="dashboard">
            <el-row :gutter="20">
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
                    <div class="stat-icon" style="background-color: #409eff;">
                      <el-icon :size="30"><Calendar /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-value">{{ seatsStore.statistics.reserved }}</div>
                      <div class="stat-label">已预约</div>
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
                      <div class="stat-value">{{ seatsStore.statistics.inUse }}</div>
                      <div class="stat-label">使用中</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              
              <el-col :xs="24" :sm="12" :md="6">
                <el-card class="stat-card">
                  <div class="stat-content">
                    <div class="stat-icon" style="background-color: #909399;">
                      <el-icon :size="30"><Coffee /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-value">{{ seatsStore.statistics.away }}</div>
                      <div class="stat-label">暂离中</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <el-row :gutter="20" style="margin-top: 20px;">
              <el-col :span="24">
                <el-alert
                  title="预约规则：每位用户仅可预约1个座位，请按时签到后生效"
                  type="info"
                  :closable="false"
                  show-icon
                />
              </el-col>
            </el-row>

            <el-row :gutter="20" style="margin-top: 10px;">
              <el-col :span="24">
                <el-alert
                  title="签到说明：预约时段开始后30分钟内可正常签到，超过30分钟预约将自动取消"
                  type="warning"
                  :closable="false"
                  show-icon
                />
              </el-col>
            </el-row>

            <el-row :gutter="20" style="margin-top: 20px;">
              <el-col :span="24">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>座位使用情况</span>
                      <el-tag type="success">使用率: {{ seatsStore.statistics.usageRate }}%</el-tag>
                    </div>
                  </template>
                  <div class="usage-chart">
                    <div class="usage-bar">
                      <div
                        class="usage-segment available"
                        :style="{ width: getPercentage(seatsStore.statistics.available) }"
                      ></div>
                      <div
                        class="usage-segment reserved"
                        :style="{ width: getPercentage(seatsStore.statistics.reserved) }"
                      ></div>
                      <div
                        class="usage-segment in-use"
                        :style="{ width: getPercentage(seatsStore.statistics.inUse) }"
                      ></div>
                      <div
                        class="usage-segment away"
                        :style="{ width: getPercentage(seatsStore.statistics.away) }"
                      ></div>
                      <div
                        class="usage-segment disabled"
                        :style="{ width: getPercentage(seatsStore.statistics.disabled) }"
                      ></div>
                    </div>
                    <div class="usage-legend">
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
                      <span>当前预约</span>
                    </div>
                  </template>
                  <div v-if="activeReservation" class="current-reservation">
                    <el-descriptions :column="1" border>
                      <el-descriptions-item label="座位号">
                        {{ activeReservation.seatId }}
                      </el-descriptions-item>
                      <el-descriptions-item label="日期">
                        {{ activeReservation.date }}
                      </el-descriptions-item>
                      <el-descriptions-item label="时段">
                        {{ activeReservation.startTime }} - {{ activeReservation.endTime }}
                      </el-descriptions-item>
                      <el-descriptions-item label="状态">
                        <el-tag :type="getStatusType(activeReservation.status)">
                          {{ getStatusText(activeReservation.status) }}
                        </el-tag>
                      </el-descriptions-item>
                    </el-descriptions>
                    <div class="reservation-actions">
                      <el-button
                        v-if="activeReservation.status === 'pending'"
                        type="success"
                        @click="handleCheckIn"
                      >
                        签到
                      </el-button>
                      <el-button
                        v-if="activeReservation.status === 'active'"
                        type="warning"
                        @click="handleAway"
                      >
                        暂离
                      </el-button>
                      <el-button
                        v-if="activeReservation.status === 'away'"
                        type="primary"
                        size="default"
                        @click="handleReturn"
                      >
                        取消暂离
                      </el-button>
                      <el-button
                        v-if="activeReservation.status !== 'active' && activeReservation.status !== 'away'"
                        type="danger"
                        size="default"
                        @click="handleCancel"
                      >
                        取消预约
                      </el-button>
                      <el-button
                        v-if="activeReservation.status === 'away'"
                        type="danger"
                        size="default"
                        @click="handleCancel"
                        style="margin-left: 10px;"
                      >
                        取消预约
                      </el-button>
                      <el-button
                        v-if="activeReservation.status === 'active'"
                        type="info"
                        @click="handleCheckOut"
                      >
                        结束使用
                      </el-button>
                    </div>
                  </div>
                  <el-empty v-else description="暂无预约" />
                </el-card>
              </el-col>

              <el-col :xs="24" :lg="12">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>快速操作</span>
                    </div>
                  </template>
                  <div class="quick-actions">
                    <el-button type="primary" size="large" @click="goToSeatMap">
                      <el-icon><Grid /></el-icon>
                      预约座位
                    </el-button>
                    <el-button type="success" size="large" @click="goToPersonal">
                      <el-icon><User /></el-icon>
                      查看记录
                    </el-button>
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useSeatsStore } from '@/stores/seats'

const router = useRouter()
const userStore = useUserStore()
const seatsStore = useSeatsStore()

const activeMenu = ref('/home')

const activeReservation = computed(() => {
  return seatsStore.getActiveReservation(userStore.user?.id)
})

const getPercentage = (count) => {
  const total = seatsStore.statistics.total
  return `${(count / total * 100).toFixed(1)}%`
}

const getStatusType = (status) => {
  const typeMap = {
    pending: 'warning',
    active: 'success',
    away: 'info',
    completed: 'info',
    cancelled: 'danger',
    violated: 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    pending: '待签到',
    active: '使用中',
    away: '暂离中',
    completed: '已完成',
    cancelled: '已取消',
    violated: '已违约'
  }
  return textMap[status] || status
}

const handleCommand = (command) => {
  if (command === 'personal') {
    router.push('/personal')
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}

const handleSelect = (index) => {
  activeMenu.value = index
}

const handleCheckIn = () => {
  const result = seatsStore.checkIn(activeReservation.value.id)
  if (result.success) {
    if (result.exceeded) {
      ElMessage({
        message: `签到成功！已超时${result.minutes}分钟，但仍在30分钟宽限期内`,
        type: 'warning',
        duration: 5000
      })
    } else {
      ElMessage.success('签到成功')
    }
  } else {
    ElMessage.error(result.message)
  }
}

const handleAway = () => {
  const result = seatsStore.setAway(activeReservation.value.id)
  if (result.success) {
    ElMessage.success('已设为暂离')
  } else {
    ElMessage.error(result.message)
  }
}

const handleReturn = () => {
  const result = seatsStore.returnFromAway(activeReservation.value.id)
  if (result.success) {
    ElMessage.success('已取消暂离')
  } else {
    ElMessage.error(result.message)
  }
}

const handleCancel = () => {
  const result = seatsStore.cancelReservation(activeReservation.value.id, userStore.user?.id)
  if (result.success) {
    ElMessage.success(result.message)
  } else {
    ElMessage.error(result.message)
  }
}

const handleCheckOut = () => {
  const result = seatsStore.checkOut(activeReservation.value.id)
  if (result.success) {
    ElMessage.success(result.message)
  } else {
    ElMessage.error(result.message)
  }
}

const goToSeatMap = () => {
  router.push('/seat-map')
}

const goToPersonal = () => {
  router.push('/personal')
}

onMounted(() => {
})
</script>

<style scoped>
.home-container {
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

.usage-chart {
  padding: 20px 0;
}

.usage-bar {
  display: flex;
  height: 40px;
  border-radius: 8px;
  overflow: hidden;
  background-color: #e0e0e0;
}

.usage-segment {
  height: 100%;
  transition: width 0.3s ease;
}

.usage-segment.available {
  background-color: #67c23a;
}

.usage-segment.reserved {
  background-color: #409eff;
}

.usage-segment.in-use {
  background-color: #e6a23c;
}

.usage-segment.away {
  background-color: #909399;
}

.usage-segment.disabled {
  background-color: #c0c4cc;
}

.usage-legend {
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

.current-reservation {
  padding: 10px 0;
}

.reservation-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
  flex-wrap: wrap;
}

.quick-actions {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.quick-actions .el-button {
  flex: 1;
  min-width: 150px;
}

@media (max-width: 768px) {
  .sidebar {
    display: none;
  }
  
  .header h1 {
    font-size: 16px;
  }
}
</style>
