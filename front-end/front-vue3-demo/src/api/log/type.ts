import type { PageParam } from '@/api/common/type'

/** 操作日志 VO */
export interface OperationLogVO {
  id: number
  userId: number
  username: string
  operation: string
  method: string
  requestParams: string
  ipAddress: string
  executeTime: number
  status: number
  createTime: string
}

/** 登录日志 VO */
export interface LoginLogVO {
  id: number
  userId: number
  username: string
  ipAddress: string
  loginResult: string
  failReason: string
  userAgent: string
  createTime: string
}

/** 操作日志查询参数 */
export interface OperationLogQuery extends PageParam {
  username?: string
  status?: number
  startTime?: string
  endTime?: string
}

/** 登录日志查询参数 */
export interface LoginLogQuery extends PageParam {
  username?: string
  loginResult?: string
  startTime?: string
  endTime?: string
}
