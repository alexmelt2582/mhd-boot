import type { PageParam, PageResult } from '@/api/common/type'

/** 积分变动类型 */
export const CreditChangeTypeMap: Record<string, string> = {
  REWARD: '加分',
  PUNISH: '扣分',
}

/** 积分参考类型 */
export const CreditReferenceTypeMap: Record<string, string> = {
  CHECKIN: '签到履约',
  CONSECUTIVE: '连续履约',
  DEFAULTED: '预约违约',
  SEVERE_DEFAULT: '严重违约',
  CANCEL: '取消预约',
  MANUAL: '手动调整',
}

/** 积分流水VO */
export interface CreditLogVO {
  id: number
  userId: number
  changeType: string
  changeScore: number
  beforeScore: number
  afterScore: number
  referenceType: string
  referenceId: number
  remark: string
  createTime: string
  /** 关联：用户姓名 */
  userName: string
}

/** 积分规则VO */
export interface CreditRuleVO {
  id: number
  ruleName: string
  ruleType: string
  changeValue: number
  referenceType: string
  description: string
  isEnabled: number
  createTime: string
  updateTime: string
}

/** 积分流水查询参数 */
export interface CreditLogQuery extends PageParam {
  referenceType?: string
  changeType?: string
  startTime?: string
  endTime?: string
  userId?: number
}

/** 手动调整积分DTO */
export interface AdjustCreditDTO {
  userId: number
  changeScore: number
  remark: string
}

/** 保存积分规则DTO */
export interface CreditRuleDTO {
  id?: number
  ruleName: string
  ruleType: string
  changeValue: number
  referenceType: string
  description: string
  isEnabled: number
}
