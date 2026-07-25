import type { InternalAxiosRequestConfig } from 'axios'
import axios, { AxiosError } from 'axios'
import { meMsgError } from '@/utils/modal'
import { useUserStore } from '@/store/modules/user'
import { appConfig } from '@/settings'

const BASE_URL = import.meta.env.VITE_APP_SERVER_PATH

const service = axios.create({
  baseURL: BASE_URL,
  timeout: appConfig.timeout || 5000,
})

export interface BaseResponse<T = any> {
  code: string
  msg: string
  data: T
}

export interface PageResponse<T = any> {
  total: number
  list: T[]
}

/**
 * 请求拦截器
 */
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()
    const token = userStore.token
    if (token && config.headers) {
      config.headers[appConfig.tokenKey] = token
    }
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  },
)

/**
 * 响应拦截器
 */
service.interceptors.response.use(
  (res: any) => {
    // 处理文件流响应（Blob/ArrayBuffer）
    if (res.request.responseType === 'blob' || res.request.responseType === 'arraybuffer') {
      return res
    }
    const {data} = res
    const code = data.code
    // 无 code 时直接返回数据（如列表接口）
    if (code === undefined) {
      return data
    }
    // 成功响应（后端约定 code=0 为成功）
    if (code === 0) {
      return data
    }
    // Token 异常（需登录）
    if (code === 100300002 || code === 401) {
      const userStore = useUserStore()
      userStore.HandleLogout().then(() => {
        setTimeout(() => {
          location.reload()
        }, 1000)
      })
    }
    const message = data.msg || data.message || '服务器未知错误'
    meMsgError({ message })
    return Promise.reject(message)
  },
  (error: AxiosError) => {
    // 处理网络/HTTP 错误
    const errorMsg = error.response?.status
      ? getHttpErrorMessage(error.response.status)
      : '连接到服务器失败'

    meMsgError({message: errorMsg})
    return Promise.reject(error)
  },
)

/**
 * HTTP 状态码错误信息映射
 */
function getHttpErrorMessage(status: number): string {
  const statusMap: Record<number, string> = {
    400: '错误请求',
    401: '未授权，请重新登录',
    403: '对不起，您没有权限访问',
    404: '请求错误，未找到请求路径',
    405: '请求方法未允许',
    408: '请求超时',
    500: '服务器出错，请重试',
    501: '网络未实现',
    502: '网络错误',
    503: '服务不可用',
    504: '网络超时',
    505: 'HTTP版本不支持该请求',
  }
  return statusMap[status] || `连接错误${status}`
}

/**
 * 获取基础URL
 */
export function getBaseURL() {
  return BASE_URL
}

export default service
