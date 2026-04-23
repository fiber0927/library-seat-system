<template>
  <div class="admin-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h1>用户管理</h1>
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
          <el-alert
            title="用户预约规则：每位用户同一时间仅可预约1个座位"
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 10px;"
          />

          <el-alert
            title="签到宽限期：用户需在预约时段开始后30分钟内签到，超时预约自动取消"
            type="warning"
            :closable="false"
            show-icon
            style="margin-bottom: 20px;"
          />

          <el-card>
            <template #header>
              <div class="card-header">
                <span>用户列表</span>
                <el-input
                  v-model="searchQuery"
                  placeholder="搜索用户名或邮箱"
                  style="width: 300px;"
                  clearable
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </div>
            </template>

            <el-table :data="filteredUsers" style="width: 100%;">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="username" label="用户名" width="150" />
              <el-table-column prop="email" label="邮箱" width="200" />
              <el-table-column prop="phone" label="手机号" width="150" />
              <el-table-column prop="role" label="角色" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.role === 'admin' ? 'danger' : 'success'">
                    {{ scope.row.role === 'admin' ? '管理员' : '普通用户' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="violations" label="违约次数" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.violations > 0 ? 'warning' : 'success'">
                    {{ scope.row.violations }} 次
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="注册时间" width="120" />
              <el-table-column label="操作" width="150">
                <template #default="scope">
                  <el-button
                    type="primary"
                    size="small"
                    @click="viewDetails(scope.row)"
                  >
                    详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-main>
      </el-container>
    </el-container>

    <el-dialog
      v-model="detailsDialogVisible"
      title="用户详情"
      width="600px"
    >
      <div v-if="selectedUser" class="user-details">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户名">
            {{ selectedUser.username }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ selectedUser.email }}
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            {{ selectedUser.phone || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag :type="selectedUser.role === 'admin' ? 'danger' : 'success'">
              {{ selectedUser.role === 'admin' ? '管理员' : '普通用户' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="违约次数">
            <el-tag :type="selectedUser.violations > 0 ? 'warning' : 'success'">
              {{ selectedUser.violations }} 次
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">
            {{ selectedUser.createdAt }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider>违规记录</el-divider>
        
        <div v-if="userViolations.length > 0">
          <el-table
            :data="userViolations"
            size="small"
            style="margin-top: 10px;"
          >
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="seatId" label="座位号" width="100" />
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="reason" label="原因" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="scope">
                <el-tag size="small" :type="scope.row.status === 'pending' ? 'warning' : 'info'">
                  {{ scope.row.status === 'pending' ? '待处理' : '已处理' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else description="暂无违规记录" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const userStore = useUserStore()
const adminStore = useAdminStore()

const activeMenu = ref('/admin/users')
const searchQuery = ref('')
const detailsDialogVisible = ref(false)
const selectedUser = ref(null)

const filteredUsers = computed(() => {
  if (!searchQuery.value) {
    return userStore.mockUsers
  }
  
  const query = searchQuery.value.toLowerCase()
  return userStore.mockUsers.filter(user =>
    user.username.toLowerCase().includes(query) ||
    user.email.toLowerCase().includes(query)
  )
})

const userViolations = computed(() => {
  if (!selectedUser.value) return []
  return adminStore.getViolationsByUser(selectedUser.value.id)
})

const viewDetails = (user) => {
  selectedUser.value = user
  detailsDialogVisible.value = true
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

.user-details {
  padding: 10px 0;
}

@media (max-width: 768px) {
  .sidebar {
    display: none;
  }
}
</style>
