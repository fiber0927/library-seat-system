<template>
  <div class="admin-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h1>违规管理</h1>
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
          <el-card>
            <template #header>
              <div class="card-header">
                <span>违规记录列表</span>
                <el-select
                  v-model="statusFilter"
                  placeholder="筛选状态"
                  style="width: 150px;"
                >
                  <el-option label="全部" value="all" />
                  <el-option label="待处理" value="pending" />
                  <el-option label="已处理" value="handled" />
                </el-select>
              </div>
            </template>

            <el-table :data="filteredViolations" style="width: 100%;">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="username" label="用户名" width="120" />
              <el-table-column prop="seatId" label="座位号" width="100" />
              <el-table-column prop="date" label="日期" width="120" />
              <el-table-column prop="reason" label="违规原因" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 'pending' ? 'warning' : 'success'">
                    {{ scope.row.status === 'pending' ? '待处理' : '已处理' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="scope">
                  <el-button
                    v-if="scope.row.status === 'pending'"
                    type="primary"
                    size="small"
                    @click="handleViolation(scope.row)"
                  >
                    处理
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    @click="deleteViolation(scope.row)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <el-empty
              v-if="filteredViolations.length === 0"
              description="暂无违规记录"
            />
          </el-card>
        </el-main>
      </el-container>
    </el-container>

    <el-dialog
      v-model="handleDialogVisible"
      title="处理违规"
      width="500px"
    >
      <div v-if="selectedViolation" class="handle-form">
        <el-alert
          :title="`用户: ${selectedViolation.username}`"
          type="info"
          :closable="false"
        />
        <el-divider />
        <el-descriptions :column="1" border>
          <el-descriptions-item label="座位号">
            {{ selectedViolation.seatId }}
          </el-descriptions-item>
          <el-descriptions-item label="日期">
            {{ selectedViolation.date }}
          </el-descriptions-item>
          <el-descriptions-item label="违规原因">
            {{ selectedViolation.reason }}
          </el-descriptions-item>
        </el-descriptions>

        <el-form :model="handleForm" label-position="top" style="margin-top: 20px;">
          <el-form-item label="处理方式">
            <el-radio-group v-model="handleForm.action">
              <el-radio label="warning">口头警告</el-radio>
              <el-radio label="suspend">暂停使用权限</el-radio>
              <el-radio label="blacklist">加入黑名单</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="处理说明">
            <el-input
              v-model="handleForm.notes"
              type="textarea"
              :rows="3"
              placeholder="请输入处理说明"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandling">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAdminStore } from '@/stores/admin'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const adminStore = useAdminStore()
const userStore = useUserStore()

const activeMenu = ref('/admin/violations')
const statusFilter = ref('all')
const handleDialogVisible = ref(false)
const selectedViolation = ref(null)

const handleForm = reactive({
  action: 'warning',
  notes: ''
})

const filteredViolations = computed(() => {
  if (statusFilter.value === 'all') {
    return adminStore.getAllViolations
  }
  return adminStore.violations.filter(v => v.status === statusFilter.value)
})

const handleViolation = (violation) => {
  selectedViolation.value = violation
  handleForm.action = 'warning'
  handleForm.notes = ''
  handleDialogVisible.value = true
}

const submitHandling = () => {
  if (!handleForm.notes) {
    ElMessage.warning('请输入处理说明')
    return
  }

  const result = adminStore.handleViolation(selectedViolation.value.id, {
    action: handleForm.action,
    notes: handleForm.notes
  })

  if (result.success) {
    ElMessage.success('违规处理成功')
    
    const user = userStore.mockUsers.find(u => u.id === selectedViolation.value.userId)
    if (user && handleForm.action !== 'warning') {
      user.violations++
      ElMessage.warning(`${user.username} 的违约次数已更新`)
    }
    
    handleDialogVisible.value = false
    selectedViolation.value = null
  } else {
    ElMessage.error(result.message)
  }
}

const deleteViolation = (violation) => {
  ElMessageBox.confirm(
    '确定要删除这条违规记录吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    adminStore.deleteViolation(violation.id)
    ElMessage.success('违规记录已删除')
  }).catch(() => {})
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

.handle-form {
  padding: 10px 0;
}

@media (max-width: 768px) {
  .sidebar {
    display: none;
  }
}
</style>
