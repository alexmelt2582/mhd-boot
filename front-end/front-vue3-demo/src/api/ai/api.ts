import type { BaseResponse, PageResponse } from '@/utils/service'
import service, { USE_MOCK } from '@/utils/service'
import type { AIConversationVO, AIConversationQuery } from './type'
import { mockGetConversations, mockDeleteConversation } from './mock'

export function getConversations(query: AIConversationQuery): Promise<BaseResponse<PageResponse<AIConversationVO>>> {
  if (USE_MOCK) return mockGetConversations(query)
  return service({ url: '/api/ai/conversations', method: 'get', params: query }) as any
}

export function deleteConversation(id: number): Promise<BaseResponse<null>> {
  if (USE_MOCK) return mockDeleteConversation(id)
  return service({ url: `/api/ai/conversations/${id}`, method: 'delete' }) as any
}
