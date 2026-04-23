<template>
  <div class="personal-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h1>个人中心</h1>
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
            @select="handleSelect"
          >
            <el-menu-item index="profile">
              <el-icon><User /></el-icon>
              <span>个人信息</span>
            </el-menu-item>
            <el-menu-item index="reservations">
              <el-icon><Calendar /></el-icon>
              <span>预约记录</span>
            </el-menu-item>
            <el-menu-item index="violations">
              <el-icon><Warning /></el-icon>
              <span>违约信息</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <el-main class="main-content">
          <div v-if="activeMenu === 'profile'" class="profile-section">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>个人信息</span>
                </div>
              </template>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="用户名">
                  {{ userStore.user?.username }}
                </el-descriptions-item>
                <el-descriptions-item label="邮箱">
                  {{ userStore.user?.email }}
                </el-descriptions-item>
                <el-descriptions-item label="手机号">
                  {{ userStore.user?.phone || '未设置' }}
                </el-descriptions-item>
                <el-descriptions-item label="角色">
                  <el-tag :type="userStore.isAdmin ? 'danger' : 'success'">
                    {{ userStore.isAdmin ? '管理员' : '普通用户' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="违约次数">
                  <el-tag :type="userStore.user?.violations > 0 ? 'danger' : 'success'">
                    {{ userStore.user?.violations }} 次
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="注册时间">
                  {{ userStore.user?.createdAt }}
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
          </div>

          <div v-if="activeMenu === 'reservations'" class="reservations-section">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>预约记录</span>
                  <el-select
                    v-model="reservationFilter"
                    placeholder="筛选状态"
                    style="width: 150px;"
                  >
                    <el-option label="全部" value="all" />
                    <el-option label="待签到" value="pending" />
                    <el-option label="使用中" value="active" />
                    <el-option label="暂离中" value="away" />
                    <el-option label="已完成" value="completed" />
                    <el-option label="已取消" value="cancelled" />
                    <el-option label="已违约" value="violated" />
                  </el-select>
                </div>
              </template>

              <el-table :data="filteredReservations" style="width: 100%">
                <el-table-column prop="id" label="预约ID" width="80" />
                <el-table-column prop="seatId" label="座位号" width="100" />
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
                <el-table-column label="操作" width="280">
                  <template #default="scope">
                    <div class="action-buttons">
                      <el-button
                        v-if="scope.row.status === 'pending'"
                        type="success"
                        size="small"
                        @click="handleCheckIn(scope.row)"
                      >
                        签到
                      </el-button>
                      <el-button
                        v-if="scope.row.status === 'active'"
                        type="warning"
                        size="small"
                        @click="handleAway(scope.row)"
                      >
                        暂离
                      </el-button>
                      <el-button
                        v-if="scope.row.status === 'away'"
                        type="primary"
                        size="small"
                        @click="handleReturn(scope.row)"
                      >
                        取消暂离
                      </el-button>
                      <el-button
                        v-if="scope.row.status === 'active'"
                        type="info"
                        size="small"
                        @click="handleCheckOut(scope.row)"
                      >
                        结束使用
                      </el-button>
                      <el-button
                        v-if="scope.row.status !== 'active'"
                        type="danger"
                        size="small"
                        @click="handleCancel(scope.row)"
                        class="cancel-btn"
                      >
                        取消预约
                      </el-button>
                      <el-button
                        v-if="scope.row.status === 'away'"
                        type="danger"
                        size="small"
                        @click="handleCancel(scope.row)"
                        class="cancel-btn"
                        style="margin-left: 10px;"
                      >
                        取消预约
                      </el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>

              <el-empty
                v-if="filteredReservations.length === 0"
                description="暂无预约记录"
              />
            </el-card>
          </div>

          <div v-if="activeMenu === 'violations'" class="violations-section">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>违约信息</span>
                </div>
              </template>

              <div class="violation-summary">
                <el-alert
                  :title="`您目前有 ${userViolations.length} 条违约记录`"
                  :type="userViolations.length > 0 ? 'error' : 'success'"
                  :closable="false"
                />
              </div>

              <el-table
                v-if="userViolations.length > 0"
                :data="userViolations"
                style="width: 100%; margin-top: 20px;"
              >
                <el-table-column prop="id" label="违规ID" width="80" />
                <el-table-column prop="seatId" label="座位号" width="100" />
                <el-table-column prop="date" label="日期" width="120" />
                <el-table-column prop="reason" label="违规原因" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="scope.row.status === 'pending' ? 'warning' : 'info'">
                      {{ scope.row.status === 'pending' ? '待处理' : '已处理' }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>

              <el-empty
                v-else
                description="暂无违约记录"
              />
            </el-card>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useSeatsStore } from '@/stores/seats'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const userStore = useUserStore()
const seatsStore = useSeatsStore()
const adminStore = useAdminStore()

const activeMenu = ref('profile')
const reservationFilter = ref('all')

const reservations = computed(() => {
  return seatsStore.getUserReservations(userStore.user?.id)
})

const filteredReservations = computed(() => {
  if (reservationFilter.value === 'all') {
    return reservations.value
  }
  return reservations.value.filter(r => r.status === reservationFilter.value)
})

const userViolations = computed(() => {
  return adminStore.getViolationsByUser(userStore.user?.id)
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

const handleSelect = (index) => {
  activeMenu.value = index
}

const handleCheckIn = (reservation) => {
  const result = seatsStore.checkIn(reservation.id)
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

const handleAway = (reservation) => {
  const result = seatsStore.setAway(reservation.id)
  if (result.success) {
    ElMessage.success('已设为暂离')
  } else {
    ElMessage.error(result.message)
  }
}

const handleReturn = (reservation) => {
  const result = seatsStore.returnFromAway(reservation.id)
  if (result.success) {
    ElMessage.success('已取消暂离')
  } else {
    ElMessage.error(result.message)
  }
}

const handleCancel = (reservation) => {
  const result = seatsStore.cancelReservation(reservation.id, userStore.user?.id)
  if (result.success) {
    ElMessage.success(result.message)
  } else {
    ElMessage.error(result.message)
  }
}

const handleCheckOut = (reservation) => {
  seatsStore.checkOut(reservation.id)
  ElMessage.success('已结束使用，感谢使用')
}

const goBack = () => {
  router.push('/home')
}
</script>

<style scoped>
.personal-container {
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

.violation-summary {
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.cancel-btn {
  background-color: #f56c6c;
  border-color: #f56c6c;
}

.cancel-btn:hover {
  background-color: #f78989;
  border-color: #f78989;
}

@media (max-width: 768px) {
  .sidebar {
    display: none;
  }
}
</style>
