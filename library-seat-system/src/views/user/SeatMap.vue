<template>
  <div class="seat-map-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h1>座位地图</h1>
          <div class="user-info">
            <el-button text @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
          </div>
        </div>
      </el-header>

      <el-container>
        <el-aside width="280px" class="filter-panel">
          <el-card>
            <template #header>
              <span>筛选条件</span>
            </template>
            
            <el-form label-position="top">
              <el-form-item label="楼层">
                <el-radio-group v-model="selectedFloor" size="default">
                  <el-radio-button
                    v-for="floor in seatsStore.floors"
                    :key="floor"
                    :value="floor"
                  >
                    {{ floor }}楼
                  </el-radio-button>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="区域">
                <el-checkbox-group v-model="selectedAreas">
                  <el-checkbox
                    v-for="area in seatsStore.areas"
                    :key="area"
                    :value="area"
                    :label="area"
                  >
                    {{ area }}区
                  </el-checkbox>
                </el-checkbox-group>
              </el-form-item>

              <el-form-item label="状态">
                <el-checkbox-group v-model="selectedStatuses">
                  <el-checkbox value="available">
                    <span class="status-dot available"></span>
                    空闲
                  </el-checkbox>
                  <el-checkbox value="reserved">
                    <span class="status-dot reserved"></span>
                    已预约
                  </el-checkbox>
                  <el-checkbox value="in_use">
                    <span class="status-dot in-use"></span>
                    使用中
                  </el-checkbox>
                  <el-checkbox value="away">
                    <span class="status-dot away"></span>
                    暂离
                  </el-checkbox>
                </el-checkbox-group>
              </el-form-item>

              <el-form-item label="座位号搜索">
                <el-input
                  v-model="searchSeatId"
                  placeholder="例如: A101"
                  clearable
                />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="resetFilters" style="width: 100%;">
                  重置筛选
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <el-card style="margin-top: 20px;">
            <template #header>
              <span>图例</span>
            </template>
            <div class="legend">
              <div class="legend-item">
                <span class="legend-color available"></span>
                <span>空闲 ({{ getStatusCount('available') }})</span>
              </div>
              <div class="legend-item">
                <span class="legend-color reserved"></span>
                <span>已预约 ({{ getStatusCount('reserved') }})</span>
              </div>
              <div class="legend-item">
                <span class="legend-color in-use"></span>
                <span>使用中 ({{ getStatusCount('in_use') }})</span>
              </div>
              <div class="legend-item">
                <span class="legend-color away"></span>
                <span>暂离 ({{ getStatusCount('away') }})</span>
              </div>
              <div class="legend-item">
                <span class="legend-color disabled"></span>
                <span>不可用 ({{ getStatusCount('disabled') }})</span>
              </div>
            </div>
          </el-card>
        </el-aside>

        <el-main class="main-content">
          <div class="floor-map">
            <div class="floor-header">
              <div class="floor-title-row">
                <h2>{{ selectedFloor }}楼座位图</h2>
                <div class="floor-tabs">
                  <el-radio-group v-model="selectedFloor" size="large">
                    <el-radio-button
                      v-for="floor in seatsStore.floors"
                      :key="floor"
                      :value="floor"
                      :class="{ 'active-floor': selectedFloor === floor }"
                    >
                      {{ floor }}楼
                    </el-radio-button>
                  </el-radio-group>
                </div>
              </div>
              <div class="header-right">
                <el-tag v-if="hasActiveReservation" type="warning" size="large">
                  您已有预约：{{ getActiveReservationInfo() }}
                </el-tag>
                <el-tag v-else type="success" size="large">
                  可预约：{{ getStatusCount('available') }} / {{ getFloorTotal(selectedFloor) }}
                </el-tag>
              </div>
            </div>

            <div class="floor-stats-summary">
              <div class="stat-item available">
                <span class="stat-count">{{ getStatusCount('available') }}</span>
                <span class="stat-label">空闲</span>
              </div>
              <div class="stat-item reserved">
                <span class="stat-count">{{ getStatusCount('reserved') }}</span>
                <span class="stat-label">已预约</span>
              </div>
              <div class="stat-item in-use">
                <span class="stat-count">{{ getStatusCount('in_use') }}</span>
                <span class="stat-label">使用中</span>
              </div>
              <div class="stat-item away">
                <span class="stat-count">{{ getStatusCount('away') }}</span>
                <span class="stat-label">暂离</span>
              </div>
              <div class="stat-item disabled">
                <span class="stat-count">{{ getStatusCount('disabled') }}</span>
                <span class="stat-label">不可用</span>
              </div>
            </div>

            <div class="map-container">
              <div
                v-for="area in selectedAreas.length > 0 ? selectedAreas : seatsStore.areas"
                :key="area"
                class="area-section"
              >
                <div class="area-header">{{ area }}区 ({{ getAreaSeats(area).length }}个座位)</div>
                <div class="seat-grid">
                  <div
                    v-for="seat in getAreaSeats(area)"
                    :key="seat.id"
                    :class="['seat-item', `seat-${seat.status}`, { selected: selectedSeat?.id === seat.id }]"
                    @click="handleSeatClick(seat)"
                  >
                    <div class="seat-back"></div>
                    <div class="seat-main">
                      <div class="seat-arm arm-left"></div>
                      <div class="seat-center">
                        <span class="seat-id">{{ seat.id }}</span>
                        <div class="seat-features">
                          <span v-if="seat.features.includes('电源插座')" title="电源插座">⚡</span>
                          <span v-if="seat.features.includes('靠窗')" title="靠窗">🪟</span>
                        </div>
                      </div>
                      <div class="seat-arm arm-right"></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-main>
      </el-container>
    </el-container>

    <el-dialog
      v-model="dialogVisible"
      :title="`座位 ${selectedSeat?.id } 详情`"
      width="500px"
    >
      <div v-if="selectedSeat" class="seat-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="座位号">
            {{ selectedSeat.id }}
          </el-descriptions-item>
          <el-descriptions-item label="楼层">
            {{ selectedSeat.floor }}楼
          </el-descriptions-item>
          <el-descriptions-item label="区域">
            {{ selectedSeat.area }}区
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(selectedSeat.status)">
              {{ getStatusText(selectedSeat.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="座位设施">
            <el-tag
              v-for="feature in selectedSeat.features"
              :key="feature"
              size="small"
              style="margin-right: 5px;"
            >
              {{ feature }}
            </el-tag>
            <span v-if="selectedSeat.features.length === 0">无</span>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="selectedSeat.status === 'available'" class="booking-form">
          <el-alert
            v-if="hasActiveReservation"
            title="您当前已有预约，不可重复预约"
            type="error"
            :closable="false"
            show-icon
          />
          <el-alert
            v-else
            title="预约后请在预约时段开始后30分钟内签到，超时将自动取消预约"
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 10px;"
          />
          <el-divider v-if="hasActiveReservation">预约信息</el-divider>
          <el-divider v-else>预约信息</el-divider>
          
          <el-form :model="bookingForm" label-position="top">
            <el-form-item label="预约楼层">
              <el-select
                v-model="bookingForm.floor"
                placeholder="选择楼层"
                :disabled="hasActiveReservation"
                style="width: 100%;"
              >
                <el-option
                  v-for="floor in seatsStore.floors"
                  :key="floor"
                  :label="`${floor}楼`"
                  :value="floor"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="预约日期">
              <el-date-picker
                v-model="bookingForm.date"
                type="date"
                placeholder="选择日期"
                :disabled-date="disabledDate"
                :disabled="hasActiveReservation"
                style="width: 100%;"
              />
            </el-form-item>

            <el-form-item label="开始时间">
              <el-time-select
                v-model="bookingForm.startTime"
                start="08:00"
                step="00:30"
                end="21:30"
                placeholder="选择开始时间"
                :disabled="hasActiveReservation"
                style="width: 100%;"
              />
            </el-form-item>

            <el-form-item label="结束时间">
              <el-time-select
                v-model="bookingForm.endTime"
                start="08:30"
                step="00:30"
                end="22:00"
                placeholder="选择结束时间"
                :min-time="bookingForm.startTime"
                :disabled="hasActiveReservation"
                style="width: 100%;"
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="bookingLoading"
                :disabled="hasActiveReservation"
                @click="handleBooking"
                style="width: 100%;"
              >
                {{ hasActiveReservation ? '已有限约，不可重复预约' : '确认预约' }}
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <div v-else-if="selectedSeat.status !== 'available'" class="unavailable-notice">
          <el-alert
            :title="getUnavailableNotice(selectedSeat.status)"
            type="warning"
            :closable="false"
          />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useSeatsStore } from '@/stores/seats'

const router = useRouter()
const userStore = useUserStore()
const seatsStore = useSeatsStore()

const selectedFloor = ref(1)
const selectedAreas = ref(['A', 'B', 'C', 'D'])
const selectedStatuses = ref(['available', 'reserved', 'in_use', 'away'])
const searchSeatId = ref('')
const selectedSeat = ref(null)
const dialogVisible = ref(false)
const bookingLoading = ref(false)

const bookingForm = reactive({
  floor: 1,
  date: new Date(),
  startTime: '09:00',
  endTime: '12:00'
})

watch(selectedFloor, (newFloor) => {
  bookingForm.floor = newFloor
})

const hasActiveReservation = computed(() => {
  const activeReservation = seatsStore.getActiveReservation(userStore.user?.id)
  return activeReservation !== null && activeReservation !== undefined
})

const getActiveReservationInfo = () => {
  const reservation = seatsStore.getActiveReservation(userStore.user?.id)
  if (reservation) {
    return `${reservation.seatId} (${reservation.date} ${reservation.startTime}-${reservation.endTime})`
  }
  return ''
}

const filteredSeats = computed(() => {
  return seatsStore.seats.filter(seat => {
    if (seat.floor !== selectedFloor.value) return false
    
    if (selectedAreas.value.length > 0 && !selectedAreas.value.includes(seat.area)) {
      return false
    }
    
    if (!selectedStatuses.value.includes(seat.status)) {
      return false
    }
    
    if (searchSeatId.value) {
      return seat.id.toLowerCase().includes(searchSeatId.value.toLowerCase())
    }
    
    return true
  })
})

const getAreaSeats = (area) => {
  return filteredSeats.value.filter(seat => seat.area === area)
}

const getStatusCount = (status) => {
  return seatsStore.seats.filter(
    seat => seat.floor === selectedFloor.value && seat.status === status
  ).length
}

const getFloorTotal = (floor) => {
  return seatsStore.seats.filter(s => s.floor === floor).length
}

const getFloorAvailable = (floor) => {
  return seatsStore.seats.filter(s => s.floor === floor && s.status === 'available').length
}

const getFloorStats = (floor) => {
  const floorSeats = seatsStore.seats.filter(s => s.floor === floor)
  return {
    available: floorSeats.filter(s => s.status === 'available').length,
    reserved: floorSeats.filter(s => s.status === 'reserved').length,
    in_use: floorSeats.filter(s => s.status === 'in_use').length,
    away: floorSeats.filter(s => s.status === 'away').length,
    disabled: floorSeats.filter(s => s.status === 'disabled').length,
    total: floorSeats.length
  }
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

const getStatusTagType = (status) => {
  const typeMap = {
    available: 'success',
    reserved: 'primary',
    in_use: 'warning',
    away: 'info',
    disabled: 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusClass = (status) => {
  return `seat-${status}`
}

const getUnavailableNotice = (status) => {
  const notices = {
    reserved: '该座位已被预约，请选择其他座位',
    in_use: '该座位正在使用中，请选择其他座位',
    away: '该座位使用者暂离中，请选择其他座位',
    disabled: '该座位暂时不可用，请选择其他座位'
  }
  return notices[status] || '该座位不可预约'
}

const handleSeatClick = (seat) => {
  selectedSeat.value = seat
  bookingForm.floor = seat.floor
  dialogVisible.value = true
}

const handleBooking = async () => {
  if (!bookingForm.date || !bookingForm.startTime || !bookingForm.endTime) {
    ElMessage.warning('请填写完整的预约信息')
    return
  }

  if (bookingForm.startTime >= bookingForm.endTime) {
    ElMessage.warning('结束时间必须晚于开始时间')
    return
  }

  if (hasActiveReservation.value) {
    ElMessage.warning('您当前已预约座位，不可重复预约')
    return
  }

  bookingLoading.value = true
  
  setTimeout(() => {
    const dateStr = new Date(bookingForm.date).toISOString().split('T')[0]
    const result = seatsStore.createReservation(
      selectedSeat.value.id,
      userStore.user.id,
      dateStr,
      bookingForm.startTime,
      bookingForm.endTime
    )

    if (result.success) {
      ElMessage({
        message: '预约成功！仅可预约1个座位，请按时签到后生效',
        type: 'success',
        duration: 5000
      })
      dialogVisible.value = false
      selectedSeat.value = null
    } else {
      ElMessage.error(result.message)
    }

    bookingLoading.value = false
  }, 500)
}

const disabledDate = (date) => {
  return date < new Date(new Date().setHours(0, 0, 0, 0))
}

const resetFilters = () => {
  selectedAreas.value = ['A', 'B', 'C', 'D']
  selectedStatuses.value = ['available', 'reserved', 'in_use', 'away']
  searchSeatId.value = ''
}

const goBack = () => {
  router.push('/home')
}
</script>

<style scoped>
.seat-map-container {
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

.floor-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  flex-wrap: wrap;
  gap: 10px;
}

.floor-tabs {
  display: flex;
  gap: 8px;
}

.floor-tabs :deep(.el-radio-button__inner) {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: bold;
  border-radius: 4px;
}

.floor-tabs :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 4px;
}

.floor-tabs :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 4px;
}

.floor-tabs :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: #409eff;
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.filter-panel {
  padding: 20px;
  background-color: #fff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  overflow-y: auto;
}

.status-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 5px;
}

.status-dot.available {
  background-color: #67c23a;
}

.status-dot.reserved {
  background-color: #409eff;
}

.status-dot.in-use {
  background-color: #e6a23c;
}

.status-dot.away {
  background-color: #909399;
}

.legend {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.legend-color {
  width: 24px;
  height: 24px;
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

.main-content {
  padding: 20px;
  background-color: #f5f7fa;
  overflow-y: auto;
}

.floor-map {
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.floor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 2px solid #e0e0e0;
  flex-wrap: wrap;
  gap: 10px;
}

.floor-header h2 {
  margin: 0;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.floor-stats-summary {
  display: flex;
  justify-content: space-around;
  gap: 15px;
  margin-bottom: 25px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 20px;
  border-radius: 6px;
  min-width: 80px;
}

.stat-count {
  font-size: 24px;
  font-weight: bold;
  color: white;
}

.stat-label {
  font-size: 12px;
  color: white;
  margin-top: 4px;
}

.stat-item.available {
  background-color: #67c23a;
}

.stat-item.reserved {
  background-color: #409eff;
}

.stat-item.in-use {
  background-color: #e6a23c;
}

.stat-item.away {
  background-color: #909399;
}

.stat-item.disabled {
  background-color: #c0c4cc;
}

.seat-count {
  color: #666;
  font-size: 14px;
}

.map-container {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.area-section {
  width: 100%;
  background-color: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.area-header {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
  padding: 12px 16px;
  background-color: #409eff;
  color: white;
  border-radius: 6px;
  text-align: center;
}

.seat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: 12px;
  padding: 10px;
}

.seat-item {
  width: 80px;
  height: 85px;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.seat-item:hover {
  transform: translateY(-2px);
}

.seat-item:hover .seat-back,
.seat-item:hover .seat-main {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.seat-item.selected .seat-back,
.seat-item.selected .seat-main {
  outline: 3px solid #409eff;
  outline-offset: 2px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.seat-back {
  width: 60px;
  height: 25px;
  border-radius: 15px 15px 5px 5px;
  position: relative;
  margin-bottom: -3px;
  z-index: 2;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.seat-main {
  width: 75px;
  height: 55px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  position: relative;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  z-index: 1;
}

.seat-arm {
  width: 8px;
  height: 40px;
  border-radius: 4px;
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.arm-left {
  left: -4px;
}

.arm-right {
  right: -4px;
}

.seat-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.seat-available .seat-back,
.seat-available .seat-main,
.seat-available .seat-arm {
  background: linear-gradient(180deg, #81C784 0%, #66BB6A 100%);
}

.seat-reserved .seat-back,
.seat-reserved .seat-main,
.seat-reserved .seat-arm {
  background: linear-gradient(180deg, #64B5F6 0%, #42A5F5 100%);
}

.seat-in_use .seat-back,
.seat-in_use .seat-main,
.seat-in_use .seat-arm {
  background: linear-gradient(180deg, #FFB74D 0%, #FFA726 100%);
}

.seat-away .seat-back,
.seat-away .seat-main,
.seat-away .seat-arm {
  background: linear-gradient(180deg, #B0BEC5 0%, #90A4AE 100%);
}

.seat-disabled .seat-back,
.seat-disabled .seat-main,
.seat-disabled .seat-arm {
  background: linear-gradient(180deg, #E0E0E0 0%, #BDBDBD 100%);
  cursor: not-allowed;
}

.seat-id {
  font-size: 11px;
  font-weight: bold;
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  z-index: 1;
  line-height: 1.2;
}

.seat-features {
  font-size: 9px;
  display: flex;
  gap: 3px;
  z-index: 1;
  margin-top: 2px;
}

.seat-features span {
  margin: 0;
}

.seat-detail {
  padding: 10px 0;
}

.booking-form {
  margin-top: 20px;
}

.unavailable-notice {
  margin-top: 20px;
}

@media (max-width: 768px) {
  .filter-panel {
    width: 100% !important;
    position: fixed;
    left: 0;
    top: 60px;
    bottom: 0;
    z-index: 1000;
    transform: translateX(-100%);
    transition: transform 0.3s;
  }

  .filter-panel.show {
    transform: translateX(0);
  }

  .map-container {
    flex-direction: column;
  }

  .area-section {
    max-width: 100%;
  }

  .seat-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 8px;
  }

  .seat-item {
    width: 75px;
    height: 52px;
    font-size: 11px;
  }

  .seat-desk {
    padding: 3px;
  }

  .seat-id {
    font-size: 10px;
  }

  .seat-features {
    font-size: 8px;
  }

  .floor-map {
    padding: 15px;
  }

  .floor-title-row {
    flex-direction: column;
    align-items: stretch;
  }

  .floor-tabs {
    width: 100%;
    display: flex;
    justify-content: space-around;
  }

  .floor-tabs :deep(.el-radio-button) {
    flex: 1;
  }

  .floor-tabs :deep(.el-radio-button__inner) {
    width: 100%;
    padding: 12px 8px;
    font-size: 16px;
  }

  .header-right {
    margin-top: 10px;
  }

  .floor-stats-summary {
    flex-wrap: wrap;
  }

  .stat-item {
    min-width: 60px;
    padding: 8px 12px;
  }

  .stat-count {
    font-size: 18px;
  }
}
</style>
