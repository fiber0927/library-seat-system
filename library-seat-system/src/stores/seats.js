import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'

export const useSeatsStore = defineStore('seats', () => {
  // 座位数据来自后端 GET /seats
  const seats = ref([])
  // 当前登录用户的预约记录，来自 GET /reservations/user/{userId}
  const reservations = ref([])
  // 记住当前用户，操作后用来刷新预约列表
  const currentUserId = ref(null)
  const loaded = ref(false)

  const floors = [1, 2, 3]
  const areas = ['A', 'B', 'C', 'D']
  const seatFeatures = ['电源插座', '靠窗', '空调', '安静区', '讨论区']

  /**
   * 后端 seat.features 是逗号分隔的字符串（如 "电源插座,靠窗"），
   * 前端展示需要数组；后端字段是 rowNum/colNum，模板里习惯用 row/col。
   * 这里统一做一次转换，视图就不用关心后端的字段形态了。
   */
  const normalizeSeat = (seat) => ({
    ...seat,
    features: seat.features ? seat.features.split(',').filter(Boolean) : [],
    row: seat.rowNum,
    col: seat.colNum
  })

  /** 拉取全部座位（一次请求，楼层/区域筛选在前端做） */
  const loadSeats = async () => {
    try {
      const list = await request.get('/seats')
      seats.value = (list || []).map(normalizeSeat)
      loaded.value = true
      return { success: true }
    } catch (e) {
      return { success: false, message: e.message }
    }
  }

  /** 拉取当前用户的预约记录 */
  const loadMyReservations = async (userId) => {
    if (!userId) return { success: false, message: '未登录' }
    try {
      currentUserId.value = userId
      reservations.value = await request.get(`/reservations/user/${userId}`)
      return { success: true }
    } catch (e) {
      return { success: false, message: e.message }
    }
  }

  /** 数据变更后统一刷新：座位 + 当前用户预约 */
  const refresh = async () => {
    await loadSeats()
    if (currentUserId.value) {
      await loadMyReservations(currentUserId.value)
    }
  }

  const getSeatsByFloor = computed(() => (floor) => {
    return seats.value.filter(seat => seat.floor === floor)
  })

  const getSeatsByArea = computed(() => (floor, area) => {
    return seats.value.filter(seat => seat.floor === floor && seat.area === area)
  })

  const getSeatsByStatus = computed(() => (status) => {
    return seats.value.filter(seat => seat.status === status)
  })

  const statistics = computed(() => {
    const total = seats.value.length
    const available = seats.value.filter(s => s.status === 'available').length
    const reserved = seats.value.filter(s => s.status === 'reserved').length
    const inUse = seats.value.filter(s => s.status === 'in_use').length
    const away = seats.value.filter(s => s.status === 'away').length
    const disabled = seats.value.filter(s => s.status === 'disabled').length

    return {
      total,
      available,
      reserved,
      inUse,
      away,
      disabled,
      usageRate: total ? ((inUse + reserved + away) / total * 100).toFixed(1) : '0.0'
    }
  })

  const getUserReservations = computed(() => (userId) => {
    return reservations.value.filter(r => r.userId === userId)
  })

  const getActiveReservation = computed(() => (userId) => {
    return reservations.value.find(
      r => r.userId === userId &&
        (r.status === 'pending' || r.status === 'active' || r.status === 'away')
    )
  })

  // ---------- 预约相关（全部走后端，业务规则由后端判定） ----------

  const createReservation = async (seatId, userId, date, startTime, endTime) => {
    try {
      const data = await request.post('/reservations', { seatId, date, startTime, endTime })
      await refresh()
      return { success: true, message: data.message, reservation: data.reservation }
    } catch (e) {
      return { success: false, message: e.message }
    }
  }

  const checkIn = async (reservationId) => {
    try {
      const data = await request.post(`/reservations/${reservationId}/check-in`)
      await refresh()
      return { success: true, ...data }
    } catch (e) {
      return { success: false, message: e.message }
    }
  }

  const setAway = async (reservationId) => {
    try {
      const data = await request.post(`/reservations/${reservationId}/away`)
      await refresh()
      return { success: true, message: data.message }
    } catch (e) {
      return { success: false, message: e.message }
    }
  }

  const returnFromAway = async (reservationId) => {
    try {
      const data = await request.post(`/reservations/${reservationId}/return`)
      await refresh()
      return { success: true, message: data.message }
    } catch (e) {
      return { success: false, message: e.message }
    }
  }

  const cancelReservation = async (reservationId) => {
    try {
      const data = await request.post(`/reservations/${reservationId}/cancel`)
      await refresh()
      return { success: true, message: data.message }
    } catch (e) {
      return { success: false, message: e.message }
    }
  }

  const checkOut = async (reservationId) => {
    try {
      const data = await request.post(`/reservations/${reservationId}/checkout`)
      await refresh()
      return { success: true, message: data.message }
    } catch (e) {
      return { success: false, message: e.message }
    }
  }

  // ---------- 管理员：座位维护 ----------

  const addSeat = async (seatData) => {
    try {
      await request.post('/seats', seatData)
      await loadSeats()
      return { success: true }
    } catch (e) {
      return { success: false, message: e.message }
    }
  }

  const updateSeatStatus = async (seatId, status) => {
    try {
      await request.put(`/seats/${seatId}/status`, { status })
      await loadSeats()
      return { success: true }
    } catch (e) {
      return { success: false, message: e.message }
    }
  }

  const deleteSeat = async (seatId) => {
    try {
      await request.delete(`/seats/${seatId}`)
      await loadSeats()
      return { success: true }
    } catch (e) {
      return { success: false, message: e.message }
    }
  }

  /** 一键清零（演示用）：清空所有活跃预约并把座位恢复空闲，违约记录保留 */
  const resetAllSeats = async () => {
    try {
      const data = await request.post('/reservations/reset')
      await refresh()
      return { success: true, message: data?.message || '清零成功' }
    } catch (e) {
      return { success: false, message: e.message }
    }
  }

  return {
    seats,
    reservations,
    loaded,
    floors,
    areas,
    seatFeatures,
    loadSeats,
    loadMyReservations,
    getSeatsByFloor,
    getSeatsByArea,
    getSeatsByStatus,
    statistics,
    createReservation,
    checkIn,
    setAway,
    returnFromAway,
    cancelReservation,
    checkOut,
    getUserReservations,
    getActiveReservation,
    updateSeatStatus,
    addSeat,
    deleteSeat,
    resetAllSeats
  }
})
