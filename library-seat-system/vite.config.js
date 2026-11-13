import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': '/src'
    }
  },
  server: {
    port: 5174,
    host: '0.0.0.0',
    // 前端请求统一走 /api，开发服务器转发到后端 8080。
    // 后端 server.servlet.context-path 也是 /api，所以这里不需要 rewrite。
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
