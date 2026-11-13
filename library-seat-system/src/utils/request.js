import axios from 'axios'

/**
 * 后端统一响应体：{ code, message, data, success }
 *   code=200 成功；401 未登录/Token 失效；403 无权限；其它为业务错误
 * 这里在响应拦截器里把 data 直接返回，业务代码拿到的就是纯数据；
 * 出错时抛出带 code 的 Error，调用方用 try/catch 或 .catch 兜住。
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

export const TOKEN_KEY = 'token'
export const USER_KEY = 'user'

export const getToken = () => localStorage.getItem(TOKEN_KEY) || ''

export const setAuth = (token, user) => {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  if (user) localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export const clearAuth = () => {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

// Token 失效时的处理回调，由 main.js 注入（避免 utils 与 router 循环依赖）
let unauthorizedHandler = null
export const setUnauthorizedHandler = (fn) => { unauthorizedHandler = fn }

const handleUnauthorized = () => {
  clearAuth()
  if (unauthorizedHandler) unauthorizedHandler()
}

// 请求拦截：带上 JWT
request.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截：拆统一响应体
request.interceptors.response.use(
  response => {
    const body = response.data
    // 非 Result 结构（例如后端直接返回数组）原样透传
    if (!body || typeof body.code !== 'number') {
      return body
    }
    if (body.code === 200) {
      return body.data
    }
    if (body.code === 401) {
      handleUnauthorized()
    }
    const error = new Error(body.message || '请求失败')
    error.code = body.code
    return Promise.reject(error)
  },
  error => {
    const status = error.response?.status
    if (status === 401) {
      handleUnauthorized()
    }
    const message = error.response?.data?.message || error.message || '网络异常'
    const wrapped = new Error(message)
    wrapped.code = status
    return Promise.reject(wrapped)
  }
)

export default request
