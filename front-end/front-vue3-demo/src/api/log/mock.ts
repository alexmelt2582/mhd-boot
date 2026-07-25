import type { BaseResponse, PageResponse } from '@/utils/service'
import type { OperationLogVO, OperationLogQuery, LoginLogVO, LoginLogQuery } from './type'

const delay = () => new Promise((r) => setTimeout(r, 200 + Math.random() * 300))

const ips = [
  '10.1.2.101', '10.1.2.102', '10.1.2.103', '10.1.2.104', '10.1.2.105',
  '192.168.1.10', '192.168.1.11', '192.168.1.12', '192.168.1.13', '192.168.1.14',
  '172.16.0.50', '172.16.0.51', '172.16.0.52', '172.16.0.53', '172.16.0.54',
  '10.0.0.1', '10.0.0.2', '10.0.0.3', '10.0.0.4', '10.0.0.5',
]

const users = [
  { id: 1, username: 'admin', realName: '系统管理员' },
  { id: 2, username: '2021001001', realName: '张三' },
  { id: 3, username: 'libadmin', realName: '图书管理员' },
  { id: 4, username: '2021001002', realName: '李四' },
  { id: 5, username: '2021001003', realName: '王五' },
  { id: 6, username: '2021001004', realName: '赵六' },
  { id: 7, username: '2021001005', realName: '孙七' },
  { id: 8, username: 'T2026001', realName: '陈教授' },
]

const operationTemplates = [
  { operation: '新增空间', method: 'com.mhd.boot.web.controller.SpaceController.createSpace', target: '空间' },
  { operation: '编辑空间', method: 'com.mhd.boot.web.controller.SpaceController.updateSpace', target: '空间' },
  { operation: '删除空间', method: 'com.mhd.boot.web.controller.SpaceController.deleteSpace', target: '空间' },
  { operation: '更新空间状态', method: 'com.mhd.boot.web.controller.SpaceController.updateStatus', target: '空间' },
  { operation: '新增预约', method: 'com.mhd.boot.web.controller.ReservationController.createReservation', target: '预约' },
  { operation: '取消预约', method: 'com.mhd.boot.web.controller.ReservationController.cancelReservation', target: '预约' },
  { operation: '签到', method: 'com.mhd.boot.web.controller.CheckinController.checkin', target: '签到' },
  { operation: '签退', method: 'com.mhd.boot.web.controller.CheckinController.checkout', target: '签到' },
  { operation: '新增用户', method: 'com.mhd.boot.web.controller.UserController.createUser', target: '用户' },
  { operation: '编辑用户', method: 'com.mhd.boot.web.controller.UserController.updateUser', target: '用户' },
  { operation: '删除用户', method: 'com.mhd.boot.web.controller.UserController.deleteUser', target: '用户' },
  { operation: '重置密码', method: 'com.mhd.boot.web.controller.UserController.resetPassword', target: '用户' },
  { operation: '更新用户状态', method: 'com.mhd.boot.web.controller.UserController.updateStatus', target: '用户' },
  { operation: '导出预约报表', method: 'com.mhd.boot.web.controller.ReservationController.exportReport', target: '预约' },
  { operation: '导入空间数据', method: 'com.mhd.boot.web.controller.SpaceController.importSpaces', target: '空间' },
  { operation: '批量删除预约', method: 'com.mhd.boot.web.controller.ReservationController.batchDelete', target: '预约' },
  { operation: '新增设备', method: 'com.mhd.boot.web.controller.EquipmentController.createEquipment', target: '设备' },
  { operation: '编辑设备', method: 'com.mhd.boot.web.controller.EquipmentController.updateEquipment', target: '设备' },
  { operation: '新增黑名单', method: 'com.mhd.boot.web.controller.BlacklistController.addBlacklist', target: '黑名单' },
  { operation: '解除黑名单', method: 'com.mhd.boot.web.controller.BlacklistController.removeBlacklist', target: '黑名单' },
]

const requestParamSamples: Record<string, string[]> = {
  '空间': [
    '{"spaceName":"安静学习区-A区-01号座","spaceType":"SEAT","areaName":"安静学习区","floor":"1F","capacity":1}',
    '{"spaceName":"研讨室-101","spaceType":"ROOM","areaName":"研讨区","floor":"1F","capacity":8}',
    '{"id":15,"spaceName":"电子阅览区-C区-03号座","spaceType":"SEAT"}',
  ],
  '预约': [
    '{"spaceId":23,"userId":2,"reservationDate":"2026-07-26","startTime":"09:00","endTime":"12:00"}',
    '{"id":128,"reason":"临时有事取消"}',
    '{"ids":[45,46,47]}',
  ],
  '签到': [
    '{"reservationId":128}',
    '{"reservationId":129,"qrCode":"QR-023-20260725"}',
  ],
  '用户': [
    '{"username":"2021002001","realName":"周八","userType":"STUDENT","college":"理学院"}',
    '{"id":4,"realName":"李四四","phone":"13900002222"}',
    '{"id":5,"password":"reset123456"}',
  ],
  '设备': [
    '{"equipmentName":"投影仪-PRO-01","equipmentType":"PROJECTOR","status":1}',
    '{"id":3,"equipmentName":"投影仪-PRO-03","status":0}',
  ],
  '黑名单': [
    '{"userId":2,"reason":"连续3次未签到","endTime":"2026-08-25"}',
    '{"id":5}',
  ],
}

function randomItem<T>(arr: T[]): T {
  return arr[Math.floor(Math.random() * arr.length)]
}

function randomInt(min: number, max: number): number {
  return Math.floor(Math.random() * (max - min + 1)) + min
}

function generateOperationLogs(): OperationLogVO[] {
  const logs: OperationLogVO[] = []
  const baseDate = new Date('2026-07-25T08:00:00')

  for (let i = 0; i < 80; i++) {
    const template = randomItem(operationTemplates)
    const user = randomItem(users)
    const paramList = requestParamSamples[template.target] || ['{}']
    const offsetMs = i * 450000 + randomInt(0, 300000)
    const date = new Date(baseDate.getTime() - offsetMs)
    const isFail = i % 19 === 0 || i % 23 === 0

    logs.push({
      id: i + 1,
      userId: user.id,
      username: user.username,
      operation: template.operation,
      method: template.method,
      requestParams: randomItem(paramList),
      ipAddress: randomItem(ips),
      executeTime: randomInt(50, 200),
      status: isFail ? 0 : 1,
      createTime: date.toISOString().replace('T', ' ').substring(0, 19),
    })
  }

  return logs.sort((a, b) => b.createTime.localeCompare(a.createTime)).map((log, idx) => ({
    ...log,
    id: idx + 1,
  }))
}

const loginFailReasons = ['密码错误', '账号锁定', '账号禁用']
const userAgents = [
  'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/126.0.0.0 Safari/537.36',
  'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 Safari/17.5',
  'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 Chrome/126.0.0.0 Safari/537.36',
  'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Edge/126.0.0.0 Safari/537.36',
  'Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 Mobile/15E148',
  'Mozilla/5.0 (Linux; Android 14; SM-S9280) AppleWebKit/537.36 Chrome/126.0.0.0 Mobile Safari/537.36',
  'Mozilla/5.0 (iPad; CPU OS 17_5 like Mac OS X) AppleWebKit/605.1.15 Mobile/15E148',
  'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/126.0.0.0 Safari/537.36 Edg/126.0.0.0',
]

function generateLoginLogs(): LoginLogVO[] {
  const logs: LoginLogVO[] = []
  const baseDate = new Date('2026-07-25T22:00:00')

  for (let i = 0; i < 50; i++) {
    const user = randomItem(users)
    const offsetMs = i * 3600000 + randomInt(0, 1800000)
    const date = new Date(baseDate.getTime() - offsetMs)
    const isFail = i % 6 === 0 || i % 8 === 0
    const failReason = isFail ? randomItem(loginFailReasons) : ''

    logs.push({
      id: i + 1,
      userId: isFail && failReason !== '密码错误' ? user.id : (isFail ? 7 : user.id),
      username: isFail && failReason === '密码错误' ? user.username : (isFail ? 'unknown_user' : user.username),
      ipAddress: randomItem(ips),
      loginResult: isFail ? 'FAIL' : 'SUCCESS',
      failReason,
      userAgent: randomItem(userAgents),
      createTime: date.toISOString().replace('T', ' ').substring(0, 19),
    })
  }

  // Ensure some specific fail scenarios
  logs[2] = { ...logs[2], loginResult: 'FAIL', failReason: '密码错误', username: '2021001003' }
  logs[7] = { ...logs[7], loginResult: 'FAIL', failReason: '账号锁定', username: '2021001006', userId: 9 }
  logs[12] = { ...logs[12], loginResult: 'FAIL', failReason: '账号禁用', username: '2021001007', userId: 10 }
  logs[18] = { ...logs[18], loginResult: 'FAIL', failReason: '密码错误', username: 'admin' }
  logs[25] = { ...logs[25], loginResult: 'FAIL', failReason: '密码错误', username: 'libadmin' }

  return logs.sort((a, b) => b.createTime.localeCompare(a.createTime)).map((log, idx) => ({
    ...log,
    id: idx + 1,
  }))
}

const operationLogs = generateOperationLogs()
const loginLogs = generateLoginLogs()

export async function mockGetOperationLogs(query: OperationLogQuery): Promise<BaseResponse<PageResponse<OperationLogVO>>> {
  await delay()
  let filtered = [...operationLogs]

  if (query.username) {
    filtered = filtered.filter((l) => l.username.includes(query.username!))
  }
  if (query.status !== undefined) {
    filtered = filtered.filter((l) => l.status === query.status)
  }
  if (query.startTime) {
    filtered = filtered.filter((l) => l.createTime >= query.startTime!)
  }
  if (query.endTime) {
    filtered = filtered.filter((l) => l.createTime <= query.endTime!)
  }

  const page = query.page || 1
  const pageSize = query.pageSize || 10
  const start = (page - 1) * pageSize
  const list = filtered.slice(start, start + pageSize)

  return { code: 0, msg: 'ok', data: { total: filtered.length, list } }
}

export async function mockGetLoginLogs(query: LoginLogQuery): Promise<BaseResponse<PageResponse<LoginLogVO>>> {
  await delay()
  let filtered = [...loginLogs]

  if (query.username) {
    filtered = filtered.filter((l) => l.username.includes(query.username!))
  }
  if (query.loginResult) {
    filtered = filtered.filter((l) => l.loginResult === query.loginResult)
  }
  if (query.startTime) {
    filtered = filtered.filter((l) => l.createTime >= query.startTime!)
  }
  if (query.endTime) {
    filtered = filtered.filter((l) => l.createTime <= query.endTime!)
  }

  const page = query.page || 1
  const pageSize = query.pageSize || 10
  const start = (page - 1) * pageSize
  const list = filtered.slice(start, start + pageSize)

  return { code: 0, msg: 'ok', data: { total: filtered.length, list } }
}

export { operationLogs, loginLogs }
