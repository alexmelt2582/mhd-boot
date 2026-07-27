/** 签到状态 */
export const CheckinStatusMap: Record<number, string> = {
  0: '待签到',
  1: '已签到',
  2: '暂离',
  3: '已返回',
  4: '已签退',
  5: '已违约',
}

/** 签到方式 */
export const CheckinTypeMap: Record<string, string> = {
  QR_CODE: '扫码签到',
  FACE: '人脸签到',
}

/** 签到记录VO */
export interface CheckinLogVO {
  id: number
  reservationId: number
  userId: number
  spaceId: number
  checkinType: string
  checkinTime: string
  tempLeaveTime: string
  tempReturnTime: string
  checkoutTime: string
  status: number
  createTime: string
  /** 关联：空间名称 */
  spaceName: string
  /** 关联：空间类型 */
  spaceType: string
  /** 关联：预约编号 */
  reservationCode: string
}
