import type { BaseResponse, PageResponse } from '@/utils/service'
import service, { USE_MOCK } from '@/utils/service'
import type { MessageVO, MessageQuery } from './type'
import {
  mockGetMyMessages,
  mockGetUnreadCount,
  mockMarkAsRead,
  mockMarkAllAsRead,
  mockDeleteMessage,
  mockGetMessageById,
} from './mock'

/** 获取我的消息列表（分页 + 筛选） */
export function getMyMessages(query: MessageQuery): Promise<BaseResponse<PageResponse<MessageVO>>> {
  if (USE_MOCK) return mockGetMyMessages(query)
  return service({ url: '/api/messages', method: 'get', params: query }) as any
}

/** 获取未读消息数量 */
export function getUnreadCount(): Promise<BaseResponse<number>> {
  if (USE_MOCK) return mockGetUnreadCount()
  return service({ url: '/api/messages/unread/count', method: 'get' }) as any
}

/** 标记单条消息为已读 */
export function markAsRead(messageId: number): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockMarkAsRead(messageId)
  return service({ url: `/api/messages/${messageId}/read`, method: 'put' }) as any
}

/** 全部标记为已读 */
export function markAllAsRead(): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockMarkAllAsRead()
  return service({ url: '/api/messages/read-all', method: 'put' }) as any
}

/** 删除消息 */
export function deleteMessage(messageId: number): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockDeleteMessage(messageId)
  return service({ url: `/api/messages/${messageId}`, method: 'delete' }) as any
}

/** 根据ID获取消息详情 */
export function getMessageById(messageId: number): Promise<BaseResponse<MessageVO>> {
  if (USE_MOCK) return mockGetMessageById(messageId)
  return service({ url: `/api/messages/${messageId}`, method: 'get' }) as any
}
