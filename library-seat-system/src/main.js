import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import { setUnauthorizedHandler } from './utils/request'
import './styles/global.css'

const app = createApp(App)
const pinia = createPinia()

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// Token 失效（401）时清掉登录态并跳回登录页
setUnauthorizedHandler(() => {
  if (router.currentRoute.value.path !== '/login') {
    router.replace('/login')
  }
})

app.use(ElementPlus)
app.use(pinia)
app.use(router)
app.mount('#app')
