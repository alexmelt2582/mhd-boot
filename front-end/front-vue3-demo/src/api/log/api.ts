import type { BaseResponse, PageResponse } from '@/utils/service'
import service, { USE_MOCK } from '@/utils/service'
import type { OperationLogVO, OperationLogQuery, LoginLogVO, LoginLogQuery } from './type'
import { mockGetOperationLogs, mockGetLoginLogs } from './mock'

export function getOperationLogs(query: OperationLogQuery): Promise<BaseResponse<PageResponse<OperationLogVO>>> {
  if (USE_MOCK) return mockGetOperationLogs(query)
  return service({ url: '/api/logs/operation', method: 'get', params: query }) as any
}

export function getLoginLogs(query: LoginLogQuery): Promise<BaseResponse<PageResponse<LoginLogVO>>> {
  if (USE_MOCK) return mockGetLoginLogs(query)
  return service({ url: '/api/logs/login', method: 'get', params: query }) as any
}
