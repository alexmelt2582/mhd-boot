import type { BaseResponse, PageResponse } from '@/utils/service'
import service, { USE_MOCK } from '@/utils/service'
import type { BlacklistVO, BlacklistQuery, AddBlacklistDTO } from './type'
import {
  mockGetBlacklist,
  mockAddBlacklist,
  mockRemoveBlacklist,
  mockCheckBlacklist,
} from './mock'

/** 获取黑名单列表 */
export function getBlacklist(query: BlacklistQuery): Promise<BaseResponse<PageResponse<BlacklistVO>>> {
  if (USE_MOCK) return mockGetBlacklist(query)
  return service({ url: '/api/admin/blacklist', method: 'get', params: query }) as any
}

/** 添加黑名单 */
export function addBlacklist(data: AddBlacklistDTO): Promise<BaseResponse<BlacklistVO>> {
  if (USE_MOCK) return mockAddBlacklist(data)
  return service({ url: '/api/admin/blacklist', method: 'post', data }) as any
}

/** 解除黑名单 */
export function removeBlacklist(id: number): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockRemoveBlacklist(id)
  return service({ url: `/api/admin/blacklist/${id}`, method: 'put' }) as any
}

/** 检查用户是否在黑名单中 */
export function checkBlacklist(userId: number): Promise<BaseResponse<BlacklistVO | null>> {
  if (USE_MOCK) return mockCheckBlacklist(userId)
  return service({ url: `/api/blacklist/check/${userId}`, method: 'get' }) as any
}
