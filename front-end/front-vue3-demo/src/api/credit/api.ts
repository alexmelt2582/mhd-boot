import type { BaseResponse, PageResponse } from '@/utils/service'
import service, { USE_MOCK } from '@/utils/service'
import type { CreditLogVO, CreditRuleVO, CreditLogQuery, AdjustCreditDTO, CreditRuleDTO } from './type'
import {
  mockGetMyCreditLogs,
  mockGetMyCreditScore,
  mockGetCreditRules,
  mockManualAdjustCredit,
  mockGetUserCreditLogs,
  mockSaveCreditRule,
  mockDeleteCreditRule,
} from './mock'

/** 获取我的积分流水 */
export function getMyCreditLogs(query: CreditLogQuery): Promise<BaseResponse<PageResponse<CreditLogVO>>> {
  if (USE_MOCK) return mockGetMyCreditLogs(query)
  return service({ url: '/api/credit/my-logs', method: 'get', params: query }) as any
}

/** 获取我的当前积分 */
export function getMyCreditScore(): Promise<BaseResponse<number>> {
  if (USE_MOCK) return mockGetMyCreditScore()
  return service({ url: '/api/credit/my-score', method: 'get' }) as any
}

/** 获取积分规则列表 */
export function getCreditRules(): Promise<BaseResponse<CreditRuleVO[]>> {
  if (USE_MOCK) return mockGetCreditRules()
  return service({ url: '/api/credit/rules', method: 'get' }) as any
}

/** 管理员：手动调整积分 */
export function manualAdjustCredit(data: AdjustCreditDTO): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockManualAdjustCredit(data)
  return service({ url: '/api/admin/credit/adjust', method: 'post', data }) as any
}

/** 管理员：获取指定用户积分流水 */
export function getUserCreditLogs(
  userId: number,
  query: CreditLogQuery,
): Promise<BaseResponse<PageResponse<CreditLogVO>>> {
  if (USE_MOCK) return mockGetUserCreditLogs(userId, query)
  return service({ url: `/api/admin/credit/user/${userId}`, method: 'get', params: query }) as any
}

/** 管理员：保存积分规则 */
export function saveCreditRule(data: CreditRuleDTO): Promise<BaseResponse<CreditRuleVO>> {
  if (USE_MOCK) return mockSaveCreditRule(data)
  if (data.id) {
    return service({ url: `/api/admin/credit/rules/${data.id}`, method: 'put', data }) as any
  }
  return service({ url: '/api/admin/credit/rules', method: 'post', data }) as any
}

/** 管理员：删除积分规则 */
export function deleteCreditRule(id: number): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockDeleteCreditRule(id)
  return service({ url: `/api/admin/credit/rules/${id}`, method: 'delete' }) as any
}
