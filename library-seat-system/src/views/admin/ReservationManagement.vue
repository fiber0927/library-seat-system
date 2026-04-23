<template>
  <div class="admin-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h1>预约管理</h1>
          <div class="user-info">
            <el-button text @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
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
          <el-alert
            title="预约规则：每位用户同一时间仅可预约1个座位，预约后30分钟内签到有效"
            type="warning"
            :closable="false"
            show-icon
            style="margin-bottom: 10px;"
          />

          <el-alert
            title="签到宽限期说明：预约时段开始后30分钟内可正常签到，超过30分钟系统自动取消预约并释放座位"
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 20px;"
          />

          <el-card>
            <template #header>
              <div class="card-header">
                <span>预约记录列表</span>
                <el-select
                  v-model="statusFilter"
                  placeholder="筛选状态"
                  style="width: 150px;"
                >
                  <el-option label="全部" value="all" />
                  <el-option label="待签到" value="pending" />
                  <el-option label="使用中" value="active" />
                  <el-option label="暂离" value="away" />
                  <el-option label="已完成" value="completed" />
                  <el-option label="已取消" value="cancelled" />
                  <el-option label="已违约" value="violated" />
                </el-select>
              </div>
            </template>

            <el-table :data="filteredReservations" style="width: 100%;">
              <el-table-column prop="id" label="预约ID" width="80" />
              <el-table-column prop="seatId" label="座位号" width="100" />
              <el-table-column prop="userId" label="用户ID" width="100" />
              <el-table-column prop="date" label="日期" width="120" />
              <el-table-column prop="startTime" label="开始时间" width="100" />
              <el-table-column prop="endTime" label="结束时间" width="100" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)">
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="签到状态" width="200">
                <template #default="scope">
                  <div v-if="scope.row.status === 'pending'">
                    <el-tag type="warning">待签到</el-tag>
                    <span style="margin-left: 10px; color: #909399; font-size: 12px;">
                      30分钟宽限期内可签到
                    </span>
                  </div>
                  <div v-else-if="scope.row.status === 'active'">
                    <el-tag type="success">已签到</el-tag>
                  </div>
                  <div v-else-if="scope.row.status === 'violated'">
                    <el-tag type="danger">超时取消</el-tag>
                  </div>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column prop="checkInTime" label="签到时间" width="180">
                <template #default="scope">
                  {{ scope.row.checkInTime ? formatTime(scope.row.checkInTime) : '-' }}
                </template>
              </el-table-column>
            </el-table>

            <el-empty
              v-if="filteredReservations.length === 0"
              description="暂无预约记录"
            />
          </el-card>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useSeatsStore } from '@/stores/seats'

const router = useRouter()
const seatsStore = useSeatsStore()

const activeMenu = ref('/admin')
const statusFilter = ref('all')

const filteredReservations = computed(() => {
  if (statusFilter.value === 'all') {
    return seatsStore.reservations
  }
  return seatsStore.reservations.filter(r => r.status === statusFilter.value)
})

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
    away: '暂离',
    completed: '已完成',
    cancelled: '已取消',
    violated: '已违约'
  }
  return textMap[status] || status
}

const formatTime = (isoString) => {
  if (!isoString) return '-'
  const date = new Date(isoString)
  return date.toLocaleString('zh-CN')
}

const goBack = () => {
  router.push('/admin')
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

.sidebar {
  background-color: #fff;
  min-height: calc(100vh - 60px);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
}

.main-content {
  padding: 20px;
  background-color: #f5f7fa;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

@media (max-width: 768px) {
  .sidebar {
    display: none;
  }
}
</style>
