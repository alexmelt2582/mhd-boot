import type { PageParam, PageResult } from '@/api/common/type'

/** 违规类型 */
export const ViolationTypeMap: Record<string, string> = {
  SEVERE_DEFAULT: '严重违约',
  DAMAGE: '损坏公物',
  DISTURBANCE: '扰乱秩序',
  FRAUD: '违规转让',
  OTHER: '其他违规',
}

/** 黑名单状态 */
export const BlacklistStatusMap: Record<number, string> = {
  0: '已解除',
  1: '生效中',
}

/** 黑名单VO */
export interface BlacklistVO {
  id: number
  userId: number
  violationType: string
  reason: string
  penaltyDays: number
  startTime: string
  endTime: string
  status: number
  createTime: string
  updateTime: string
  /** 关联：用户姓名 */
  userName: string
}

/** 黑名单查询参数 */
export interface BlacklistQuery extends PageParam {
  violationType?: string
  status?: number
  keyword?: string
}

/** 添加黑名单DTO */
export interface AddBlacklistDTO {
  userId: number
  violationType: string
  reason: string
  penaltyDays: number
}
