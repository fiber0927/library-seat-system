<template>
  <div class="admin-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h1>座位管理</h1>
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
            title="座位管理规则：每位用户同一时间仅可占用1个座位"
            type="warning"
            :closable="false"
            show-icon
            style="margin-bottom: 10px;"
          />

          <el-alert
            title="签到宽限期：预约时段开始后30分钟内可正常签到，超时自动释放座位"
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 20px;"
          />

          <el-card>
            <template #header>
              <div class="card-header">
                <span>座位列表</span>
                <el-button type="primary" @click="showAddDialog">
                  <el-icon><Plus /></el-icon>
                  添加座位
                </el-button>
              </div>
            </template>

            <el-form inline @submit.prevent>
              <el-form-item label="楼层">
                <el-select v-model="filterFloor" placeholder="全部楼层" clearable style="width: 120px;">
                  <el-option
                    v-for="floor in seatsStore.floors"
                    :key="floor"
                    :label="`${floor}楼`"
                    :value="floor"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="区域">
                <el-select v-model="filterArea" placeholder="全部区域" clearable style="width: 120px;">
                  <el-option
                    v-for="area in seatsStore.areas"
                    :key="area"
                    :label="`${area}区`"
                    :value="area"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 150px;">
                  <el-option label="空闲" value="available" />
                  <el-option label="已预约" value="reserved" />
                  <el-option label="使用中" value="in_use" />
                  <el-option label="暂离" value="away" />
                  <el-option label="不可用" value="disabled" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadSeats">查询</el-button>
                <el-button @click="resetFilter">重置</el-button>
              </el-form-item>
            </el-form>

            <el-table :data="filteredSeats" style="width: 100%; margin-top: 20px;">
              <el-table-column prop="id" label="座位号" width="120" />
              <el-table-column prop="floor" label="楼层" width="80">
                <template #default="scope">
                  {{ scope.row.floor }}楼
                </template>
              </el-table-column>
              <el-table-column prop="area" label="区域" width="80">
                <template #default="scope">
                  {{ scope.row.area }}区
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)">
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="features" label="设施" width="300">
                <template #default="scope">
                  <el-tag
                    v-for="feature in scope.row.features"
                    :key="feature"
                    size="small"
                    style="margin-right: 5px;"
                  >
                    {{ feature }}
                  </el-tag>
                  <span v-if="scope.row.features.length === 0">无</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="scope">
                  <el-button type="warning" size="small" @click="handleToggleStatus(scope.row)">
                    {{ scope.row.status === 'disabled' ? '启用' : '禁用' }}
                  </el-button>
                  <el-button type="danger" size="small" @click="handleDelete(scope.row)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="filteredSeats.length"
              layout="total, prev, pager, next"
              style="margin-top: 20px; text-align: right;"
            />
          </el-card>
        </el-main>
      </el-container>
    </el-container>

    <el-dialog
      v-model="addDialogVisible"
      title="添加座位"
      width="500px"
    >
      <el-form :model="addForm" label-position="top">
        <el-form-item label="座位号">
          <el-input v-model="addForm.id" placeholder="例如: E101" />
        </el-form-item>
        <el-form-item label="楼层">
          <el-select v-model="addForm.floor" placeholder="选择楼层" style="width: 100%;">
            <el-option
              v-for="floor in seatsStore.floors"
              :key="floor"
              :label="`${floor}楼`"
              :value="floor"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="区域">
          <el-select v-model="addForm.area" placeholder="选择区域" style="width: 100%;">
            <el-option
              v-for="area in seatsStore.areas"
              :key="area"
              :label="`${area}区`"
              :value="area"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设施">
          <el-checkbox-group v-model="addForm.features">
            <el-checkbox
              v-for="feature in seatsStore.seatFeatures"
              :key="feature"
              :label="feature"
              :value="feature"
            />
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useSeatsStore } from '@/stores/seats'

const router = useRouter()
const seatsStore = useSeatsStore()

const activeMenu = ref('/admin/seats')
const filterFloor = ref('')
const filterArea = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(20)

const addDialogVisible = ref(false)
const addForm = reactive({
  id: '',
  floor: 1,
  area: 'A',
  features: []
})

const filteredSeats = computed(() => {
  let result = seatsStore.seats
  
  if (filterFloor.value) {
    result = result.filter(s => s.floor === filterFloor.value)
  }
  if (filterArea.value) {
    result = result.filter(s => s.area === filterArea.value)
  }
  if (filterStatus.value) {
    result = result.filter(s => s.status === filterStatus.value)
  }
  
  return result
})

const getStatusType = (status) => {
  const typeMap = {
    available: 'success',
    reserved: 'primary',
    in_use: 'warning',
    away: 'info',
    disabled: 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    available: '空闲',
    reserved: '已预约',
    in_use: '使用中',
    away: '暂离',
    disabled: '不可用'
  }
  return textMap[status] || status
}

const showAddDialog = () => {
  addDialogVisible.value = true
}

const handleAdd = () => {
  if (!addForm.id) {
    ElMessage.warning('请输入座位号')
    return
  }

  const result = seatsStore.addSeat({
    id: addForm.id.toUpperCase(),
    floor: addForm.floor,
    area: addForm.area.toUpperCase(),
    features: addForm.features,
    row: 1,
    col: 1
  })

  if (result.success) {
    ElMessage.success('座位添加成功')
    addDialogVisible.value = false
    addForm.id = ''
    addForm.features = []
  } else {
    ElMessage.error(result.message)
  }
}

const handleToggleStatus = (seat) => {
  const newStatus = seat.status === 'disabled' ? 'available' : 'disabled'
  seatsStore.updateSeatStatus(seat.id, newStatus)
  ElMessage.success(`座位已${newStatus === 'disabled' ? '禁用' : '启用'}`)
}

const handleDelete = (seat) => {
  ElMessageBox.confirm(
    `确定要删除座位 ${seat.id} 吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    seatsStore.deleteSeat(seat.id)
    ElMessage.success('座位已删除')
  }).catch(() => {})
}

const loadSeats = () => {
  // 筛选已应用，这里只需触发重新计算
}

const resetFilter = () => {
  filterFloor.value = ''
  filterArea.value = ''
  filterStatus.value = ''
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
