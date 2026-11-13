import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request, { getToken, setAuth, clearAuth } from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  // 登录态从 localStorage 恢复，刷新页面不掉线
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const token = ref(getToken())
  // 管理员用：后端返回的全部用户列表
  const users = ref([])

  const isLoggedIn = computed(() => !!user.value && !!token.value)
  const isAdmin = computed(() => user.value?.role === 'admin')

  const login = async (username, password) => {
    try {
      const data = await request.post('/users/login', { username, password })
      // data = { success, token, user }
      token.value = data.token
      user.value = data.user
      setAuth(data.token, data.user)
      return { success: true, user: data.user }
    } catch (e) {
      return { success: false, message: e.message }
    }
  }

  const register = async (form) => {
    try {
      const message = await request.post('/users/register', {
        username: form.username,
        password: form.password,
        email: form.email,
        phone: form.phone || null
      })
      return { success: true, message }
    } catch (e) {
      return { success: false, message: e.message }
    }
  }

  const logout = () => {
    user.value = null
    token.value = ''
    clearAuth()
  }

  /** 刷新页面后用 token 换一次最新的用户信息（违约次数等由后端维护） */
  const fetchCurrentUser = async () => {
    try {
      const current = await request.get('/users/current')
      user.value = current
      localStorage.setItem('user', JSON.stringify(current))
      return { success: true, user: current }
    } catch (e) {
      return { success: false, message: e.message }
    }
  }

  /** 管理员：拉取全部用户 */
  const loadUsers = async () => {
    try {
      users.value = await request.get('/users')
      return { success: true }
    } catch (e) {
      return { success: false, message: e.message }
    }
  }

  /** 管理员：启用 / 禁用账号 */
  const toggleUserEnabled = async (id) => {
    try {
      const message = await request.put(`/users/${id}/toggle-enabled`)
      await loadUsers()
      return { success: true, message }
    } catch (e) {
      return { success: false, message: e.message }
    }
  }

  /** 违约次数由后端维护，这里重新拉一次当前用户即可 */
  const refreshViolations = async () => {
    const res = await fetchCurrentUser()
    return res
  }

  return {
    user,
    token,
    users,
    isLoggedIn,
    isAdmin,
    login,
    register,
    logout,
    fetchCurrentUser,
    loadUsers,
    toggleUserEnabled,
    refreshViolations
  }
})
