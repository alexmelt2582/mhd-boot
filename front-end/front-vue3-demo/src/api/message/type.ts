import type { PageParam } from '@/api/common/type'

/** 消息类型：SYSTEM | RESERVATION | CREDIT | VIOLATION */
export type MessageType = 'SYSTEM' | 'RESERVATION' | 'CREDIT' | 'VIOLATION'

/** 消息VO */
export interface MessageVO {
  /** 消息ID */
  id: number
  /** 接收用户ID */
  userId: number
  /** 消息标题 */
  title: string
  /** 消息内容 */
  content: string
  /** 消息类型 */
  messageType: MessageType
  /** 关联业务ID（如预约ID） */
  relatedId: number | null
  /** 是否已读：0-未读 1-已读 */
  isRead: number
  /** 发送时间 */
  sendTime: string
  /** 创建时间 */
  createTime: string
}

/** 消息查询参数 */
export interface MessageQuery extends PageParam {
  /** 消息类型筛选 */
  messageType?: MessageType
  /** 是否已读筛选 */
  isRead?: number
}
