import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useSeatsStore = defineStore('seats', () => {
  const seats = ref(generateMockSeats())
  const reservations = ref(JSON.parse(localStorage.getItem('reservations') || '[]'))
  
  const floors = [1, 2, 3]
  const areas = ['A', 'B', 'C', 'D']
  const seatFeatures = ['电源插座', '靠窗', '空调', '安静区', '讨论区']

  function generateMockSeats() {
    const mockSeats = []
    for (let floor = 1; floor <= 3; floor++) {
      for (let area of ['A', 'B', 'C', 'D']) {
        const seatsInArea = area === 'A' || area === 'C' ? 16 : 12
        for (let i = 1; i <= seatsInArea; i++) {
          const row = Math.ceil(i / 4)
          const col = ((i - 1) % 4) + 1
          const seatId = `${area}${floor}${String(i).padStart(2, '0')}`
          
          const features = []
          if (i % 5 === 1) features.push('电源插座')
          if (col === 1) features.push('靠窗')
          if (floor === 3) features.push('安静区')
          if (area === 'D') features.push('讨论区')
          
          mockSeats.push({
            id: seatId,
            floor,
            area,
            row,
            col,
            status: getRandomStatus(),
            features,
            userId: null
          })
        }
      }
    }
    return mockSeats
  }

  function getRandomStatus() {
    const statuses = ['available', 'reserved', 'in_use', 'away', 'disabled']
    const weights = [0.4, 0.2, 0.2, 0.1, 0.1]
    const random = Math.random()
    let cumulative = 0
    for (let i = 0; i < statuses.length; i++) {
      cumulative += weights[i]
      if (random < cumulative) {
        return statuses[i]
      }
    }
    return 'available'
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
      usageRate: ((inUse + reserved + away) / total * 100).toFixed(1)
    }
  })

  const createReservation = (seatId, userId, date, startTime, endTime) => {
    const seat = seats.value.find(s => s.id === seatId)
    if (!seat || seat.status !== 'available') {
      return { success: false, message: '座位不可预约' }
    }

    const existingReservation = reservations.value.find(
      r => r.seatId === seatId && r.date === date && 
           ((startTime >= r.startTime && startTime < r.endTime) ||
            (endTime > r.startTime && endTime <= r.endTime))
    )

    if (existingReservation) {
      return { success: false, message: '该时段已被预约' }
    }

    const reservation = {
      id: reservations.value.length + 1,
      seatId,
      userId,
      date,
      startTime,
      endTime,
      status: 'pending',
      checkInTime: null,
      checkOutTime: null,
      createdAt: new Date().toISOString()
    }

    reservations.value.push(reservation)
    seat.status = 'reserved'
    seat.userId = userId
    
    saveReservations()
    return { success: true, reservation }
  }

  const GRACE_PERIOD_MINUTES = 30
  
  const checkIn = (reservationId) => {
    const reservation = reservations.value.find(r => r.id === reservationId)
    if (!reservation) {
      return { success: false, message: '预约记录不存在' }
    }

    const now = new Date()
    const [hours, minutes] = reservation.startTime.split(':').map(Number)
    const startTime = new Date()
    startTime.setHours(hours, minutes, 0, 0)
    
    const diffMinutes = (now - startTime) / (1000 * 60)
    
    if (diffMinutes > GRACE_PERIOD_MINUTES) {
      reservation.status = 'violated'
      
      const seat = seats.value.find(s => s.id === reservation.seatId)
      if (seat) {
        seat.status = 'available'
        seat.userId = null
      }
      
      saveReservations()
      return {
        success: false,
        message: `签到已超时（超过${GRACE_PERIOD_MINUTES}分钟宽限期），预约已取消`,
        exceeded: true,
        minutes: Math.round(diffMinutes)
      }
    }

    reservation.status = 'active'
    reservation.checkInTime = now.toISOString()
    
    const seat = seats.value.find(s => s.id === reservation.seatId)
    if (seat) {
      seat.status = 'in_use'
    }
    
    saveReservations()
    return {
      success: true,
      exceeded: false,
      minutes: Math.round(diffMinutes)
    }
  }

  const setAway = (reservationId) => {
    const reservation = reservations.value.find(r => r.id === reservationId)
    if (!reservation || reservation.status !== 'active') {
      return { success: false, message: '无法暂离' }
    }

    reservation.status = 'away'
    
    const seat = seats.value.find(s => s.id === reservation.seatId)
    if (seat) {
      seat.status = 'away'
    }
    
    saveReservations()
    return { success: true }
  }

  const returnFromAway = (reservationId) => {
    const reservation = reservations.value.find(r => r.id === reservationId)
    if (!reservation || reservation.status !== 'away') {
      return { success: false, message: '无法取消暂离' }
    }

    reservation.status = 'active'
    
    const seat = seats.value.find(s => s.id === reservation.seatId)
    if (seat) {
      seat.status = 'in_use'
    }
    
    saveReservations()
    return { success: true }
  }

  const cancelReservation = (reservationId, userId) => {
    const reservationIndex = reservations.value.findIndex(
      r => r.id === reservationId && r.userId === userId
    )
    if (reservationIndex === -1) {
      return { success: false, message: '预约记录不存在' }
    }

    const reservation = reservations.value[reservationIndex]
    const currentStatus = reservation.status
    const seatId = reservation.seatId
    
    const seat = seats.value.find(s => s.id === seatId)
    if (seat) {
      seat.status = 'available'
      seat.userId = null
    }
    
    reservations.value.splice(reservationIndex, 1)
    
    saveReservations()
    
    let message
    switch (currentStatus) {
      case 'pending':
        message = '预约已取消（待签到状态），座位已释放'
        break
      case 'active':
        message = '预约已取消（使用中状态），座位已释放'
        break
      case 'away':
        message = '预约已取消（暂离状态），座位已释放'
        break
      default:
        message = '预约已取消，座位已释放'
    }
    
    return { success: true, message }
  }

  const checkOut = (reservationId) => {
    const reservationIndex = reservations.value.findIndex(r => r.id === reservationId)
    if (reservationIndex === -1) {
      return { success: false, message: '预约记录不存在' }
    }

    const reservation = reservations.value[reservationIndex]
    const seatId = reservation.seatId
    
    const seat = seats.value.find(s => s.id === seatId)
    if (seat) {
      seat.status = 'available'
      seat.userId = null
    }
    
    reservations.value.splice(reservationIndex, 1)
    
    saveReservations()
    return { success: true, message: '已结束使用，感谢使用' }
  }

  const getUserReservations = (userId) => {
    return reservations.value.filter(r => r.userId === userId)
  }

  const getActiveReservation = (userId) => {
    return reservations.value.find(
      r => r.userId === userId && 
           (r.status === 'pending' || r.status === 'active' || r.status === 'away')
    )
  }

  const updateSeatStatus = (seatId, status) => {
    const seat = seats.value.find(s => s.id === seatId)
    if (seat) {
      seat.status = status
    }
  }

  const addSeat = (seatData) => {
    const existingSeat = seats.value.find(s => s.id === seatData.id)
    if (existingSeat) {
      return { success: false, message: '座位号已存在' }
    }
    
    seats.value.push({
      ...seatData,
      status: seatData.status || 'available',
      userId: null
    })
    return { success: true }
  }

  const deleteSeat = (seatId) => {
    const index = seats.value.findIndex(s => s.id === seatId)
    if (index > -1) {
      seats.value.splice(index, 1)
      return { success: true }
    }
    return { success: false, message: '座位不存在' }
  }

  const resetAllSeats = () => {
    seats.value.forEach(seat => {
      seat.status = 'available'
      seat.userId = null
    })
    reservations.value = []
    saveReservations()
  }

  const saveReservations = () => {
    localStorage.setItem('reservations', JSON.stringify(reservations.value))
  }

  return {
    seats,
    reservations,
    floors,
    areas,
    seatFeatures,
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
