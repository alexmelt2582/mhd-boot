import type { BaseResponse, PageResponse } from '@/utils/service'
import type { BlacklistVO, BlacklistQuery, AddBlacklistDTO } from './type'

const userNames: Record<number, string> = {
  1: '系统管理员',
  2: '张三',
  3: '图书管理员',
  4: '李四',
  5: '王五',
  6: '赵六',
  7: '孙七',
  8: '周八',
}

/* ========= 生成 15 条黑名单记录 ========= */
function generateBlacklist(): BlacklistVO[] {
  const list: BlacklistVO[] = []
  let id = 1

  function add(
    userId: number,
    violationType: string,
    reason: string,
    penaltyDays: number,
    startTime: string,
    endTime: string,
    status: number,
    createTime: string,
    updateTime: string,
  ) {
    list.push({
      id: id++,
      userId,
      violationType,
      reason,
      penaltyDays,
      startTime,
      endTime,
      status,
      createTime,
      updateTime,
      userName: userNames[userId] || '未知用户',
    })
  }

  // 生效中的黑名单（6条）
  add(2, 'SEVERE_DEFAULT', '累计违约3次，2026-06-20 严重违约，多次预约未签到', 30, '2026-06-20 00:00:00', '2026-07-20 23:59:59', 1, '2026-06-20 10:00:00', '2026-06-20 10:00:00')
  add(5, 'SEVERE_DEFAULT', '连续违约5次，恶意占用座位资源', 60, '2026-07-01 00:00:00', '2026-08-30 23:59:59', 1, '2026-07-01 10:00:00', '2026-07-01 10:00:00')
  add(6, 'DAMAGE', '损坏研讨室投影设备，维修费500元', 90, '2026-06-15 00:00:00', '2026-09-13 23:59:59', 1, '2026-06-15 14:00:00', '2026-06-15 14:00:00')
  add(4, 'DISTURBANCE', '在安静学习区大声喧哗，多次劝阻无效', 14, '2026-07-20 00:00:00', '2026-08-03 23:59:59', 1, '2026-07-20 09:00:00', '2026-07-20 09:00:00')
  add(7, 'FRAUD', '将预约座位转让他人并收取费用', 180, '2026-07-10 00:00:00', '2027-01-06 23:59:59', 1, '2026-07-10 16:00:00', '2026-07-10 16:00:00')
  add(8, 'OTHER', '私自携带外来人员进入图书馆并占用多个座位', 30, '2026-07-15 00:00:00', '2026-08-14 23:59:59', 1, '2026-07-15 11:00:00', '2026-07-15 11:00:00')

  // 已解除的黑名单（9条）
  add(2, 'DEFAULTED', '2026-04-05 预约研讨室未签到', 7, '2026-04-05 00:00:00', '2026-04-12 23:59:59', 0, '2026-04-05 15:00:00', '2026-04-12 23:59:59')
  add(2, 'DEFAULTED', '2026-05-12 预约研讨室未签到', 14, '2026-05-12 00:00:00', '2026-05-26 23:59:59', 0, '2026-05-12 15:00:00', '2026-05-26 23:59:59')
  add(3, 'DISTURBANCE', '在电子阅览区外放音频影响他人', 3, '2026-03-20 00:00:00', '2026-03-23 23:59:59', 0, '2026-03-20 14:00:00', '2026-03-23 23:59:59')
  add(4, 'CANCEL', '连续一周临近时间取消预约，浪费座位资源', 7, '2026-05-01 00:00:00', '2026-05-08 23:59:59', 0, '2026-05-01 10:00:00', '2026-05-08 23:59:59')
  add(5, 'DEFAULTED', '预约后未签到，累计第2次违约', 7, '2026-04-10 00:00:00', '2026-04-17 23:59:59', 0, '2026-04-10 10:00:00', '2026-04-17 23:59:59')
  add(5, 'DEFAULTED', '预约后未签到，累计第3次违约', 14, '2026-05-20 00:00:00', '2026-06-03 23:59:59', 0, '2026-05-20 10:00:00', '2026-06-03 23:59:59')
  add(6, 'DISTURBANCE', '在研讨室吸烟触发烟雾报警', 30, '2026-02-10 00:00:00', '2026-03-12 23:59:59', 0, '2026-02-10 15:00:00', '2026-03-12 23:59:59')
  add(7, 'OTHER', '借用他人校园卡预约座位', 14, '2026-04-15 00:00:00', '2026-04-29 23:59:59', 0, '2026-04-15 09:00:00', '2026-04-29 23:59:59')
  add(8, 'DEFAULTED', '预约后未签到，累计第2次违约', 7, '2026-06-01 00:00:00', '2026-06-08 23:59:59', 0, '2026-06-01 08:00:00', '2026-06-08 23:59:59')

  return list
}

const blacklistData: BlacklistVO[] = generateBlacklist()

const delay = () => new Promise((r) => setTimeout(r, 200 + Math.random() * 300))

/** 获取黑名单列表 */
export async function mockGetBlacklist(query: BlacklistQuery): Promise<BaseResponse<PageResponse<BlacklistVO>>> {
  await delay()
  let filtered = [...blacklistData]

  if (query.violationType) filtered = filtered.filter((b) => b.violationType === query.violationType)
  if (query.status !== undefined) filtered = filtered.filter((b) => b.status === query.status)
  if (query.keyword) {
    const kw = query.keyword.toLowerCase()
    filtered = filtered.filter(
      (b) => b.userName.includes(kw) || b.reason.includes(kw) || b.violationType.includes(kw),
    )
  }

  filtered.sort((a, b) => b.createTime.localeCompare(a.createTime))

  const page = query.page || 1
  const pageSize = query.pageSize || 10
  const start = (page - 1) * pageSize
  return { code: 0, msg: 'ok', data: { total: filtered.length, list: filtered.slice(start, start + pageSize) } }
}

/** 添加黑名单 */
export async function mockAddBlacklist(data: AddBlacklistDTO): Promise<BaseResponse<BlacklistVO>> {
  await delay()
  const now = new Date().toISOString().replace('T', ' ').slice(0, 19)
  const startDate = new Date()
  const endDate = new Date(startDate.getTime() + data.penaltyDays * 86400000)

  const newItem: BlacklistVO = {
    id: blacklistData.length + 1,
    userId: data.userId,
    violationType: data.violationType,
    reason: data.reason,
    penaltyDays: data.penaltyDays,
    startTime: startDate.toISOString().replace('T', ' ').slice(0, 19),
    endTime: endDate.toISOString().replace('T', ' ').slice(0, 19),
    status: 1,
    createTime: now,
    updateTime: now,
    userName: userNames[data.userId] || '未知用户',
  }
  blacklistData.push(newItem)
  return { code: 0, msg: '添加成功', data: newItem }
}

/** 解除黑名单 */
export async function mockRemoveBlacklist(id: number): Promise<BaseResponse<null>> {
  await delay()
  const item = blacklistData.find((b) => b.id === id)
  if (!item) return { code: 404, msg: '记录不存在', data: null }
  const now = new Date().toISOString().replace('T', ' ').slice(0, 19)
  item.status = 0
  item.endTime = now
  item.updateTime = now
  return { code: 0, msg: '已解除', data: null }
}

/** 检查用户是否在黑名单中 */
export async function mockCheckBlacklist(userId: number): Promise<BaseResponse<BlacklistVO | null>> {
  await delay()
  const active = blacklistData.find((b) => b.userId === userId && b.status === 1)
  if (active) {
    return { code: 0, msg: '该用户处于黑名单中', data: { ...active } }
  }
  return { code: 0, msg: 'ok', data: null as any }
}

export { blacklistData }
