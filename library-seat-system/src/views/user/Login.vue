<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>图书馆座位管理系统</h2>
        </div>
      </template>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="0"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="register-link">
        <span>还没有账号？</span>
        <router-link to="/register">立即注册</router-link>
      </div>

      <el-divider>测试账号</el-divider>
      
      <div class="test-accounts">
        <el-tag @click="fillAdmin">管理员: admin / admin123</el-tag>
        <el-tag type="success" @click="fillStudent">学生: student001 / 123456</el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      
      setTimeout(() => {
        const result = userStore.login(loginForm.username, loginForm.password)
        
        if (result.success) {
          ElMessage.success('登录成功')
          
          if (result.user.role === 'admin') {
            router.push('/admin')
          } else {
            router.push('/home')
          }
        } else {
          ElMessage.error(result.message)
        }
        
        loading.value = false
      }, 500)
    }
  })
}

const fillAdmin = () => {
  loginForm.username = 'admin'
  loginForm.password = 'admin123'
}

const fillStudent = () => {
  loginForm.username = 'student001'
  loginForm.password = '123456'
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 100%;
  max-width: 420px;
  margin: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.login-button {
  width: 100%;
}

.register-link {
  text-align: center;
  margin-top: 10px;
  color: #666;
}

.register-link a {
  color: #409eff;
  text-decoration: none;
  margin-left: 5px;
}

.register-link a:hover {
  text-decoration: underline;
}

.test-accounts {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 10px;
}

.test-accounts .el-tag {
  cursor: pointer;
  justify-content: center;
}

@media (max-width: 480px) {
  .login-card {
    margin: 10px;
  }
}
</style>
