import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const token = ref(localStorage.getItem('token') || '')

  const isLoggedIn = computed(() => !!user.value)
  const isAdmin = computed(() => user.value?.role === 'admin')

  const mockUsers = ref([
    {
      id: 1,
      username: 'admin',
      password: 'admin123',
      email: 'admin@library.com',
      phone: '13800138000',
      role: 'admin',
      violations: 0,
      createdAt: '2025-01-01'
    },
    {
      id: 2,
      username: 'student001',
      password: '123456',
      email: 'student001@example.com',
      phone: '13900139000',
      role: 'user',
      violations: 1,
      createdAt: '2025-03-01'
    },
    {
      id: 3,
      username: 'student002',
      password: '123456',
      email: 'student002@example.com',
      phone: '13700137000',
      role: 'user',
      violations: 0,
      createdAt: '2025-03-15'
    }
  ])

  const login = (username, password) => {
    const foundUser = mockUsers.value.find(
      u => u.username === username && u.password === password
    )
    
    if (foundUser) {
      const { password: _, ...userWithoutPassword } = foundUser
      user.value = userWithoutPassword
      token.value = 'mock-token-' + Date.now()
      localStorage.setItem('user', JSON.stringify(userWithoutPassword))
      localStorage.setItem('token', token.value)
      return { success: true, user: userWithoutPassword }
    }
    
    return { success: false, message: '用户名或密码错误' }
  }

  const register = (userData) => {
    const existingUser = mockUsers.value.find(
      u => u.username === userData.username || u.email === userData.email
    )
    
    if (existingUser) {
      return { success: false, message: '用户名或邮箱已存在' }
    }
    
    const newUser = {
      id: mockUsers.value.length + 1,
      username: userData.username,
      password: userData.password,
      email: userData.email,
      phone: userData.phone || '',
      role: 'user',
      violations: 0,
      createdAt: new Date().toISOString().split('T')[0]
    }
    
    mockUsers.value.push(newUser)
    return { success: true, message: '注册成功' }
  }

  const logout = () => {
    user.value = null
    token.value = ''
    localStorage.removeItem('user')
    localStorage.removeItem('token')
  }

  const updateViolations = (count) => {
    if (user.value) {
      user.value.violations = count
      localStorage.setItem('user', JSON.stringify(user.value))
      
      const userInList = mockUsers.value.find(u => u.id === user.value.id)
      if (userInList) {
        userInList.violations = count
      }
    }
  }

  return {
    user,
    token,
    isLoggedIn,
    isAdmin,
    login,
    register,
    logout,
    updateViolations,
    mockUsers
  }
})
