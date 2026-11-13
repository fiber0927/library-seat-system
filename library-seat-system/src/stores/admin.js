import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'

export const useAdminStore = defineStore('admin', () => {
  // 违约记录来自后端 GET /violations（管理员视角）
  const violations = ref([])
  // 当前用户自己的违约记录，来自 GET /violations/user/{userId}（普通用户也能调）
  const myViolations = ref([])
  // 全部预约（管理员视角）来自 GET /reservations
  const reservations = ref([])

  /** 拉取全部违约记录（管理员） */
  const loadAllViolations = async () => {
    try {
      violations.value = await request.get('/violations')
      return { success: true }
    } catch (e) {
      return { success: false, message: e.message }
    }
  }

  /** 拉取当前用户的违约记录（普通用户可用） */
  const loadMyViolations = async (userId) => {
    if (!userId) return { success: false, message: '未登录' }
    try {
      myViolations.value = await request.get(`/violations/user/${userId}`)
      return { success: true }
    } catch (e) {
      return { success: false, message: e.message }
    }
  }

  /** 拉取全部预约（管理员视角） */
  const loadAllReservations = async () => {
    try {
      reservations.value = await request.get('/reservations')
      return { success: true }
    } catch (e) {
      return { success: false, message: e.message }
    }
  }

  const getAllViolations = computed(() => violations.value)

  const getPendingViolations = computed(() => {
    return violations.value.filter(v => v.status === 'pending')
  })

  const getViolationsByUser = computed(() => (userId) => {
    return violations.value.filter(v => v.userId === userId)
  })

  /** 处理违约：后端写入 handling / handledBy / handledAt */
  const handleViolation = async (violationId, handling) => {
    try {
      const message = await request.post(`/violations/${violationId}/handle`, { handling })
      await loadAllViolations()
      return { success: true, message }
    } catch (e) {
      return { success: false, message: e.message }
    }
  }

  const deleteViolation = async (violationId) => {
    try {
      await request.delete(`/violations/${violationId}`)
      await loadAllViolations()
      return { success: true }
    } catch (e) {
      return { success: false, message: e.message }
    }
  }

  /** 清零只清预约与座位，违约记录保留 —— 与后端 reset 接口语义一致 */
  const clearReservations = async () => {
    try {
      const data = await request.post('/reservations/reset')
      await loadAllReservations()
      return { success: true, message: data?.message || '清零成功' }
    } catch (e) {
      return { success: false, message: e.message }
    }
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
    myViolations,
    reservations,
    loadAllViolations,
    loadMyViolations,
    loadAllReservations,
    getAllViolations,
    getPendingViolations,
    getViolationsByUser,
    handleViolation,
    deleteViolation,
    violationStatistics,
    clearReservations
  }
})
