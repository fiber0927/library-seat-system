import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/user/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/user/Register.vue')
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/user/Home.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/seat-map',
    name: 'SeatMap',
    component: () => import('@/views/user/SeatMap.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/personal',
    name: 'Personal',
    component: () => import('@/views/user/PersonalCenter.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('@/views/admin/Dashboard.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/seats',
    name: 'SeatManagement',
    component: () => import('@/views/admin/SeatManagement.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/users',
    name: 'UserManagement',
    component: () => import('@/views/admin/UserManagement.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/violations',
    name: 'ViolationManagement',
    component: () => import('@/views/admin/ViolationManagement.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/reservations',
    name: 'ReservationManagement',
    component: () => import('@/views/admin/ReservationManagement.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else if (to.meta.requiresAdmin && userStore.user?.role !== 'admin') {
    next('/home')
  } else {
    next()
  }
})

export default router
