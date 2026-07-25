import type { BaseResponse } from '@/utils/service'
import service, { USE_MOCK } from '@/utils/service'
import type { CheckinLogVO } from './type'
import {
  mockCheckin,
  mockTempLeave,
  mockTempReturn,
  mockCheckout,
  mockGetCheckinLog,
} from './mock'

/** 签到 */
export function checkin(reservationId: number): Promise<BaseResponse<CheckinLogVO>> {
  if (USE_MOCK) return mockCheckin(reservationId)
  return service({ url: `/api/checkin/${reservationId}`, method: 'post' }) as any
}

/** 暂离 */
export function tempLeave(reservationId: number): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockTempLeave(reservationId)
  return service({ url: `/api/checkin/${reservationId}/temp-leave`, method: 'put' }) as any
}

/** 暂离返回 */
export function tempReturn(reservationId: number): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockTempReturn(reservationId)
  return service({ url: `/api/checkin/${reservationId}/temp-return`, method: 'put' }) as any
}

/** 签退 */
export function checkout(reservationId: number): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockCheckout(reservationId)
  return service({ url: `/api/checkin/${reservationId}/checkout`, method: 'put' }) as any
}

/** 获取签到记录 */
export function getCheckinLog(reservationId: number): Promise<BaseResponse<CheckinLogVO>> {
  if (USE_MOCK) return mockGetCheckinLog(reservationId)
  return service({ url: `/api/checkin/${reservationId}`, method: 'get' }) as any
}
