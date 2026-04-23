import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAdminStore = defineStore('admin', () => {
  const violations = ref(JSON.parse(localStorage.getItem('violations') || '[]'))

  const mockViolations = [
    {
      id: 1,
      userId: 2,
      username: 'student001',
      seatId: 'A102',
      date: '2026-04-05',
      reason: '预约后未签到',
      status: 'pending',
      createdAt: '2026-04-05T10:15:00'
    },
    {
      id: 2,
      userId: 2,
      username: 'student001',
      seatId: 'B203',
      date: '2026-04-03',
      reason: '暂离超时',
      status: 'handled',
      createdAt: '2026-04-03T14:30:00'
    },
    {
      id: 3,
      userId: 3,
      username: 'student002',
      seatId: 'C301',
      date: '2026-04-06',
      reason: '损坏公共设施',
      status: 'pending',
      createdAt: '2026-04-06T16:45:00'
    }
  ]

  if (violations.value.length === 0) {
    violations.value = mockViolations
    localStorage.setItem('violations', JSON.stringify(mockViolations))
  }

  const getAllViolations = computed(() => violations.value)
  
  const getPendingViolations = computed(() => {
    return violations.value.filter(v => v.status === 'pending')
  })

  const getViolationsByUser = computed(() => (userId) => {
    return violations.value.filter(v => v.userId === userId)
  })

  const createViolation = (violationData) => {
    const violation = {
      id: violations.value.length + 1,
      ...violationData,
      status: 'pending',
      createdAt: new Date().toISOString()
    }
    violations.value.push(violation)
    saveViolations()
    return { success: true, violation }
  }

  const handleViolation = (violationId, handling) => {
    const violation = violations.value.find(v => v.id === violationId)
    if (!violation) {
      return { success: false, message: '违规记录不存在' }
    }

    violation.status = 'handled'
    violation.handledAt = new Date().toISOString()
    violation.handling = handling
    
    saveViolations()
    return { success: true }
  }

  const deleteViolation = (violationId) => {
    const index = violations.value.findIndex(v => v.id === violationId)
    if (index > -1) {
      violations.value.splice(index, 1)
      saveViolations()
      return { success: true }
    }
    return { success: false, message: '违规记录不存在' }
  }

  const clearReservations = () => {
    // 清零时不需要清空违规记录，违规记录保留
    // 只清空座位和预约相关的数据
  }

  const saveViolations = () => {
    localStorage.setItem('violations', JSON.stringify(violations.value))
  }

  const violationStatistics = computed(() => {
    const total = violations.value.length
    const pending = violations.value.filter(v => v.status === 'pending').length
    const handled = violations.value.filter(v => v.status === 'handled').length
    
    const reasonCount = {}
    violations.value.forEach(v => {
      reasonCount[v.reason] = (reasonCount[v.reason] || 0) + 1
    })

    return {
      total,
      pending,
      handled,
      reasonCount
    }
  })

  return {
    violations,
    getAllViolations,
    getPendingViolations,
    getViolationsByUser,
    createViolation,
    handleViolation,
    deleteViolation,
    violationStatistics,
    clearReservations
  }
})
