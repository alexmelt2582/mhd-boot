import type { BaseResponse, PageResponse } from '@/utils/service'
import service, { USE_MOCK } from '@/utils/service'
import type { ReservationVO, ReservationQuery, CreateReservationDTO } from './type'
import {
  mockGetMyReservations,
  mockGetReservationById,
  mockCreateReservation,
  mockCancelReservation,
  mockGetAllReservations,
  mockForceCancelReservation,
  mockForceCheckout,
  mockApproveReservation,
} from './mock'

/** 获取我的预约列表 */
export function getMyReservations(query: ReservationQuery): Promise<BaseResponse<PageResponse<ReservationVO>>> {
  if (USE_MOCK) return mockGetMyReservations(query)
  return service({ url: '/api/reservations/my', method: 'get', params: query }) as any
}

/** 获取预约详情 */
export function getReservationById(id: number): Promise<BaseResponse<ReservationVO>> {
  if (USE_MOCK) return mockGetReservationById(id)
  return service({ url: `/api/reservations/${id}`, method: 'get' }) as any
}

/** 创建预约 */
export function createReservation(data: CreateReservationDTO): Promise<BaseResponse<ReservationVO>> {
  if (USE_MOCK) return mockCreateReservation(data)
  return service({ url: '/api/reservations', method: 'post', data }) as any
}

/** 取消预约 */
export function cancelReservation(id: number, reason: string): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockCancelReservation(id, reason)
  return service({ url: `/api/reservations/${id}/cancel`, method: 'put', data: { reason } }) as any
}

/* ========= 管理员 API ========= */

/** 管理员：获取所有预约 */
export function getAllReservations(query: ReservationQuery): Promise<BaseResponse<PageResponse<ReservationVO>>> {
  if (USE_MOCK) return mockGetAllReservations(query)
  return service({ url: '/api/admin/reservations', method: 'get', params: query }) as any
}

/** 管理员：强制取消预约 */
export function forceCancelReservation(id: number): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockForceCancelReservation(id)
  return service({ url: `/api/admin/reservations/${id}/force-cancel`, method: 'put' }) as any
}

/** 管理员：强制签退 */
export function forceCheckout(id: number): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockForceCheckout(id)
  return service({ url: `/api/admin/reservations/${id}/force-checkout`, method: 'put' }) as any
}

/** 管理员：审批预约 */
export function approveReservation(id: number, approved: boolean, remark?: string): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockApproveReservation(id, approved, remark)
  return service({ url: `/api/admin/reservations/${id}/approve`, method: 'put', data: { approved, remark } }) as any
}

