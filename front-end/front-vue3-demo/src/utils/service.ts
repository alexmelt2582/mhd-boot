import type {AxiosResponse, InternalAxiosRequestConfig} from 'axios'
import axios, { AxiosError } from 'axios'
import { meMsgError } from '@/utils/modal'
import { useUserStore } from '@/store/modules/user'
import { appConfig } from '@/settings'
import {RESPONSE_CODE} from "@/constants/status.ts";
import {useMessage} from "@/hooks/message.ts";

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

export interface PageInfo<T = any> {
  total: number
  list: T[]
}

export interface PageParam {
  pageNo?: number;
  pageSize?: number;
}

/** Mock 开关：开发环境默认开启 */
export const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true' || true

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
  async (response: AxiosResponse) => {
    const { config, data, headers } = response;
    // ==========================================
    // 第一步：如果是文件流（Blob/ArrayBuffer），直接返回
    // ==========================================
    // 判断依据：请求配置中指定了 responseType 为 blob 或 arraybuffer
    if (config.responseType === 'blob' || config.responseType === 'arraybuffer') {
      // 如果返回的是 application/json，说明实际上是报错了，需要将 blob 转为文本错误信息
      const contentType = headers['content-type'] || '';
      if (contentType.includes('application/json')) {
        // 如果拿到的是 blob 但包含 JSON 报错（例如权限不足返回 403）
        const text = await new Response(data).text();
        const jsonError = JSON.parse(text);
        // 直接拦截并报错，不要继续下载错误内容的文件
        useMessage().error(jsonError.msg || '下载失败');
        return Promise.reject(new Error(jsonError.msg || 'Error'));
      }
      // 正常情况下，直接返回 Blob 流，让外部 `downBlobFile` 去处理
      return data;
    }

    // ==========================================
    // 第二步：正常 JSON 业务接口，统一处理状态码
    // ==========================================
    const res = data;
    // 校验后台约定的成功状态码
    if (res.code === RESPONSE_CODE.SUCCESS) {
      // 直接返回 data
      return res.data;
    }
    // 权限拦截
    if (res.code === RESPONSE_CODE.UNAUTHORIZED || res.code === RESPONSE_CODE.FORBIDDEN) {
      const userStore = useUserStore()
      userStore.HandleLogout()
      return Promise.reject(new Error('登录已过期'));
    }

    // 业务失败
    useMessage().error(res.msg || '操作失败');
    return Promise.reject(new Error(res.msg || 'Error'));
  },
  (error) => {
    // 处理网络/HTTP 错误
    const errorMsg = error.response?.status
      ? getHttpErrorMessage(error.response.status)
      : '连接到服务器失败'

    meMsgError({message: errorMsg})
    return Promise.reject(error)
  }
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
