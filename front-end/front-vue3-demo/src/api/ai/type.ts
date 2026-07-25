import type { PageParam } from '@/api/common/type'

/** AI 对话记录 VO */
export interface AIConversationVO {
  id: number
  userId: number
  userName?: string
  question: string
  answer: string
  convType: string
  isFavorite: number
  isUseful: number
  createTime: string
}

/** AI 对话查询参数 */
export interface AIConversationQuery extends PageParam {
  userId?: number
  convType?: string
  startTime?: string
  endTime?: string
}
