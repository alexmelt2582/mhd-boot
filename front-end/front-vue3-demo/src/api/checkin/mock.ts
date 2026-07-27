import type { BaseResponse } from '@/utils/service'
import type { CheckinLogVO } from './type'
import { reservationList } from '@/api/reservation/mock'

/* ========= 空间名称映射 ========= */
const spaceNameMap: Record<number, { name: string; type: string }> = {}
for (const r of reservationList) {
  if (!spaceNameMap[r.spaceId]) {
    spaceNameMap[r.spaceId] = { name: r.spaceName, type: r.spaceType }
  }
}

/* ========= 生成签到记录（为已签到/已完成/已违约的预约生成日志） ========= */
function generateCheckinLogs(): CheckinLogVO[] {
  const list: CheckinLogVO[] = []
  let id = 1

  for (const res of reservationList) {
    // 只对已签到、已完成、已违约状态生成签到记录
    if (![1, 2, 4].includes(res.status)) continue

    const space = spaceNameMap[res.spaceId] || { name: '未知空间', type: 'SEAT' }

    // 签到时间：预约开始时间前 5-15 分钟
    const startDate = new Date(res.startTime.replace(/-/g, '/'))
    const checkinOffset = Math.floor(Math.random() * 10) + 5 // 5-14 min before
    const checkinTime = new Date(startDate.getTime() - checkinOffset * 60000)
      .toISOString().replace('T', ' ').slice(0, 19)

    const createTime = new Date(startDate.getTime() - 30 * 60000)
      .toISOString().replace('T', ' ').slice(0, 19)

    if (res.status === 1) {
      // 已签到 — 可能暂离或刚签到
      const isTempLeave = Math.random() > 0.6
      const status = isTempLeave ? 2 : 1
      const tempLeaveTime = isTempLeave
        ? new Date(startDate.getTime() + 60 * 60000).toISOString().replace('T', ' ').slice(0, 19)
        : ''
      list.push({
        id: id++,
        reservationId: res.id,
        userId: res.userId,
        spaceId: res.spaceId,
        checkinType: Math.random() > 0.5 ? 'QR_CODE' : 'FACE',
        checkinTime,
        tempLeaveTime,
        tempReturnTime: '',
        checkoutTime: '',
        status,
        createTime,
        spaceName: space.name,
        spaceType: space.type,
        reservationCode: res.reservationCode,
      })
    } else if (res.status === 2) {
      // 已完成 — 已签退
      const endDate = new Date(res.endTime.replace(/-/g, '/'))
      const checkoutTime = new Date(endDate.getTime() - Math.floor(Math.random() * 20) * 60000)
        .toISOString().replace('T', ' ').slice(0, 19)
      list.push({
        id: id++,
        reservationId: res.id,
        userId: res.userId,
        spaceId: res.spaceId,
        checkinType: Math.random() > 0.5 ? 'QR_CODE' : 'FACE',
        checkinTime,
        tempLeaveTime: '',
        tempReturnTime: '',
        checkoutTime,
        status: 4,
        createTime,
        spaceName: space.name,
        spaceType: space.type,
        reservationCode: res.reservationCode,
      })
    } else if (res.status === 4) {
      // 已违约 — 未签到
      list.push({
        id: id++,
        reservationId: res.id,
        userId: res.userId,
        spaceId: res.spaceId,
        checkinType: '',
        checkinTime: '',
        tempLeaveTime: '',
        tempReturnTime: '',
        checkoutTime: '',
        status: 5,
        createTime: res.createTime,
        spaceName: space.name,
        spaceType: space.type,
        reservationCode: res.reservationCode,
      })
    }
  }

  return list
}

const checkinLogList: CheckinLogVO[] = generateCheckinLogs()

const delay = () => new Promise((r) => setTimeout(r, 200 + Math.random() * 300))

/** 签到 */
export async function mockCheckin(reservationId: number): Promise<BaseResponse<CheckinLogVO>> {
  await delay()
  const res = reservationList.find((r) => r.id === reservationId)
  if (!res) return { code: 404, msg: '预约不存在', data: null as any }
  if (res.status !== 0) return { code: 1001, msg: '预约状态不可签到', data: null as any }

  const space = spaceNameMap[res.spaceId] || { name: '未知空间', type: 'SEAT' }
  const now = new Date().toISOString().replace('T', ' ').slice(0, 19)

  // 更新预约状态
  res.status = 1

  const newLog: CheckinLogVO = {
    id: checkinLogList.length + 1,
    reservationId: res.id,
    userId: res.userId,
    spaceId: res.spaceId,
    checkinType: 'QR_CODE',
    checkinTime: now,
    tempLeaveTime: '',
    tempReturnTime: '',
    checkoutTime: '',
    status: 1,
    createTime: now,
    spaceName: space.name,
    spaceType: space.type,
    reservationCode: res.reservationCode,
  }
  checkinLogList.push(newLog)
  return { code: 0, msg: '签到成功', data: newLog }
}

/** 暂离 */
export async function mockTempLeave(reservationId: number): Promise<BaseResponse<null>> {
  await delay()
  const log = checkinLogList.find((l) => l.reservationId === reservationId)
  if (!log) return { code: 404, msg: '签到记录不存在', data: null }
  if (log.status !== 1) return { code: 1001, msg: '当前状态不可暂离', data: null }

  const now = new Date().toISOString().replace('T', ' ').slice(0, 19)
  log.status = 2
  log.tempLeaveTime = now

  // 更新预约状态为暂离（仍为已签到，因为 ReservationStatus 没有暂离状态）
  const res = reservationList.find((r) => r.id === reservationId)
  if (res) res.status = 1

  return { code: 0, msg: '已暂离', data: null }
}

/** 暂离返回 */
export async function mockTempReturn(reservationId: number): Promise<BaseResponse<null>> {
  await delay()
  const log = checkinLogList.find((l) => l.reservationId === reservationId)
  if (!log) return { code: 404, msg: '签到记录不存在', data: null }
  if (log.status !== 2) return { code: 1001, msg: '当前状态不可返回', data: null }

  const now = new Date().toISOString().replace('T', ' ').slice(0, 19)
  log.status = 3
  log.tempReturnTime = now

  return { code: 0, msg: '已返回', data: null }
}

/** 签退 */
export async function mockCheckout(reservationId: number): Promise<BaseResponse<null>> {
  await delay()
  const log = checkinLogList.find((l) => l.reservationId === reservationId)
  if (!log) return { code: 404, msg: '签到记录不存在', data: null }
  if (![1, 2, 3].includes(log.status)) return { code: 1001, msg: '当前状态不可签退', data: null }

  const now = new Date().toISOString().replace('T', ' ').slice(0, 19)
  log.status = 4
  log.checkoutTime = now

  // 更新预约为已完成
  const res = reservationList.find((r) => r.id === reservationId)
  if (res) res.status = 2

  return { code: 0, msg: '签退成功', data: null }
}

/** 获取签到记录 */
export async function mockGetCheckinLog(reservationId: number): Promise<BaseResponse<CheckinLogVO>> {
  await delay()
  const log = checkinLogList.find((l) => l.reservationId === reservationId)
  if (!log) return { code: 404, msg: '签到记录不存在', data: null as any }
  return { code: 0, msg: 'ok', data: { ...log } }
}

export { checkinLogList }
