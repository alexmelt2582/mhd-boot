import type { PageParam, PageResult } from '@/api/common/type'

/** 预约状态 */
export const ReservationStatusMap: Record<number, string> = {
  0: '已预约',
  1: '已签到',
  2: '已完成',
  3: '已取消',
  4: '已违约',
}

/** 审批状态 */
export const ApprovalStatusMap: Record<number, string> = {
  1: '无需审批',
  2: '待审批',
  3: '已通过',
  4: '已驳回',
}

/** 空间类型 */
export const SpaceTypeMap: Record<string, string> = {
  SEAT: '座位',
  ROOM: '研讨室',
}

/** 预约VO */
export interface ReservationVO {
  id: number
  reservationCode: string
  userId: number
  spaceId: number
  startTime: string
  endTime: string
  participants: string[]
  purpose: string
  approvalStatus: number
  approvalRemark: string
  cancelReason: string
  cancelTime: string
  status: number
  createTime: string
  /** 关联：用户姓名 */
  userName: string
  /** 关联：空间名称 */
  spaceName: string
  /** 关联：空间类型 */
  spaceType: string
}

/** 预约查询参数 */
export interface ReservationQuery extends PageParam {
  status?: number
  approvalStatus?: number
  spaceType?: string
  startTime?: string
  endTime?: string
  keyword?: string
}

/** 创建预约DTO */
export interface CreateReservationDTO {
  spaceId: number
  startTime: string
  endTime: string
  participants?: string[]
  purpose?: string
}

/** 取消预约DTO */
export interface CancelReservationDTO {
  id: number
  reason: string
}

/** 审批预约参数 */
export interface ApproveReservationDTO {
  id: number
  approved: boolean
  remark?: string
}
