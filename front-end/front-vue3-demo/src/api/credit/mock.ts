import type { BaseResponse, PageResponse } from '@/utils/service'
import type { CreditLogVO, CreditRuleVO, CreditLogQuery, AdjustCreditDTO, CreditRuleDTO } from './type'

/* ========= 积分规则 ========= */
const creditRules: CreditRuleVO[] = [
  {
    id: 1,
    ruleName: '签到履约加分',
    ruleType: 'REWARD',
    changeValue: 2,
    referenceType: 'CHECKIN',
    description: '按时签到并使用完毕，每次加2分',
    isEnabled: 1,
    createTime: '2026-03-01 00:00:00',
    updateTime: '2026-03-01 00:00:00',
  },
  {
    id: 2,
    ruleName: '连续履约加分',
    ruleType: 'REWARD',
    changeValue: 5,
    referenceType: 'CONSECUTIVE',
    description: '连续5天正常履约，额外加5分',
    isEnabled: 1,
    createTime: '2026-03-01 00:00:00',
    updateTime: '2026-03-01 00:00:00',
  },
  {
    id: 3,
    ruleName: '预约违约扣分',
    ruleType: 'PUNISH',
    changeValue: -10,
    referenceType: 'DEFAULTED',
    description: '预约后未签到，视作违约，扣10分',
    isEnabled: 1,
    createTime: '2026-03-01 00:00:00',
    updateTime: '2026-03-01 00:00:00',
  },
  {
    id: 4,
    ruleName: '严重违约扣分',
    ruleType: 'PUNISH',
    changeValue: -20,
    referenceType: 'SEVERE_DEFAULT',
    description: '累计违约3次或严重违规，扣20分',
    isEnabled: 1,
    createTime: '2026-03-01 00:00:00',
    updateTime: '2026-03-01 00:00:00',
  },
  {
    id: 5,
    ruleName: '取消预约扣分',
    ruleType: 'PUNISH',
    changeValue: -5,
    referenceType: 'CANCEL',
    description: '预约开始前2小时内取消，扣5分',
    isEnabled: 1,
    createTime: '2026-03-01 00:00:00',
    updateTime: '2026-06-15 00:00:00',
  },
]

/* ========= 用户名称 ========= */
const userNames: Record<number, string> = {
  1: '系统管理员',
  2: '张三',
  3: '图书管理员',
  4: '李四',
  5: '王五',
}

/* ========= 生成 40 条积分流水（userId=2 张三，积分从100逐步降至85） ========= */
function generateCreditLogs(): CreditLogVO[] {
  const list: CreditLogVO[] = []
  let id = 1
  let score = 100

  function add(changeType: string, changeScore: number, referenceType: string, referenceId: number, remark: string, date: string) {
    const before = score
    score += changeScore
    if (score < 0) score = 0
    list.push({
      id: id++,
      userId: 2,
      changeType,
      changeScore,
      beforeScore: before,
      afterScore: score,
      referenceType,
      referenceId,
      remark,
      createTime: date,
      userName: '张三',
    })
  }

  // 3月份：初始100分，正常签到积累
  add('REWARD', 2, 'CHECKIN', 101, '按时签到-安区01号座', '2026-03-05 10:00:00')
  add('REWARD', 2, 'CHECKIN', 102, '按时签到-电区01号座', '2026-03-06 10:00:00')
  add('REWARD', 2, 'CHECKIN', 103, '按时签到-休区01号座', '2026-03-08 10:00:00')
  add('REWARD', 2, 'CHECKIN', 104, '按时签到-多区03号座', '2026-03-10 10:00:00')
  add('PUNISH', -5, 'CANCEL', 201, '临近开始取消预约-研讨室101', '2026-03-12 08:15:00')
  add('REWARD', 2, 'CHECKIN', 105, '按时签到-安区03号座', '2026-03-15 10:00:00')
  add('REWARD', 2, 'CHECKIN', 106, '按时签到-电区01号座', '2026-03-16 10:00:00')
  add('REWARD', 2, 'CHECKIN', 107, '按时签到-安区04号座', '2026-03-17 10:00:00')
  add('REWARD', 2, 'CHECKIN', 108, '按时签到-休区04号座', '2026-03-18 10:00:00')
  add('REWARD', 2, 'CHECKIN', 109, '按时签到-多区02号座', '2026-03-19 10:00:00')
  add('REWARD', 5, 'CONSECUTIVE', 301, '连续5天履约奖励', '2026-03-19 10:05:00')
  add('REWARD', 2, 'CHECKIN', 110, '按时签到-安区01号座', '2026-03-22 10:00:00')

  // 4月份：开始出现违约
  add('REWARD', 2, 'CHECKIN', 111, '按时签到-安区02号座', '2026-04-01 10:00:00')
  add('PUNISH', -10, 'DEFAULTED', 401, '预约违约-研讨室201未签到', '2026-04-05 15:00:00')
  add('REWARD', 2, 'CHECKIN', 112, '按时签到-电区01号座', '2026-04-08 10:00:00')
  add('REWARD', 2, 'CHECKIN', 113, '按时签到-休区01号座', '2026-04-10 10:00:00')
  add('PUNISH', -5, 'CANCEL', 202, '临近开始取消预约-安区05号座', '2026-04-12 07:50:00')
  add('REWARD', 2, 'CHECKIN', 114, '按时签到-多区03号座', '2026-04-15 10:00:00')
  add('REWARD', 2, 'CHECKIN', 115, '按时签到-安区03号座', '2026-04-17 10:00:00')

  // 5月份
  add('REWARD', 2, 'CHECKIN', 116, '按时签到-电区01号座', '2026-05-06 10:00:00')
  add('REWARD', 2, 'CHECKIN', 117, '按时签到-安区02号座', '2026-05-08 10:00:00')
  add('PUNISH', -10, 'DEFAULTED', 402, '预约违约-研讨室301未签到', '2026-05-12 15:00:00')
  add('REWARD', 2, 'CHECKIN', 118, '按时签到-休区04号座', '2026-05-15 10:00:00')
  add('REWARD', 2, 'CHECKIN', 119, '按时签到-多区02号座', '2026-05-18 10:00:00')
  add('PUNISH', -5, 'CANCEL', 203, '临近开始取消预约-安区04号座', '2026-05-22 08:30:00')

  // 6月份
  add('REWARD', 2, 'CHECKIN', 120, '按时签到-安区01号座', '2026-06-02 10:00:00')
  add('REWARD', 2, 'CHECKIN', 121, '按时签到-电区01号座', '2026-06-05 10:00:00')
  add('REWARD', 2, 'CHECKIN', 122, '按时签到-休区02号座', '2026-06-08 10:00:00')
  add('PUNISH', -10, 'DEFAULTED', 403, '预约违约-安区03号座未签到', '2026-06-10 15:00:00')
  add('REWARD', 2, 'CHECKIN', 123, '按时签到-多区03号座', '2026-06-12 10:00:00')
  add('REWARD', 2, 'CHECKIN', 124, '按时签到-安区01号座', '2026-06-15 10:00:00')
  add('REWARD', 2, 'CHECKIN', 125, '按时签到-电区01号座', '2026-06-18 10:00:00')
  add('PUNISH', -20, 'SEVERE_DEFAULT', 501, '累计违约3次，严重违约处罚', '2026-06-20 10:00:00')
  add('REWARD', 2, 'CHECKIN', 126, '按时签到-休区02号座', '2026-06-22 10:00:00')

  // 7月份
  add('REWARD', 2, 'CHECKIN', 127, '按时签到-安区02号座', '2026-07-01 10:00:00')
  add('REWARD', 2, 'CHECKIN', 128, '按时签到-电区01号座', '2026-07-03 10:00:00')
  add('PUNISH', -5, 'CANCEL', 204, '临近开始取消预约-休区03号座', '2026-07-08 08:20:00')
  add('PUNISH', -10, 'DEFAULTED', 404, '预约违约-多区03号座未签到', '2026-07-14 15:00:00')
  add('REWARD', 2, 'CHECKIN', 129, '按时签到-安区01号座', '2026-07-20 10:00:00')
  add('REWARD', 2, 'CHECKIN', 130, '按时签到-电区01号座', '2026-07-21 10:00:00')
  add('REWARD', 2, 'CHECKIN', 131, '按时签到-休区04号座', '2026-07-23 10:00:00')

  return list
}

const creditLogList: CreditLogVO[] = generateCreditLogs()
let nextRuleId = 6
let nextLogId = creditLogList.length + 1

const delay = () => new Promise((r) => setTimeout(r, 200 + Math.random() * 300))

/** 获取我的积分流水 */
export async function mockGetMyCreditLogs(query: CreditLogQuery): Promise<BaseResponse<PageResponse<CreditLogVO>>> {
  await delay()
  let filtered = creditLogList.filter((l) => l.userId === 2)

  if (query.changeType) filtered = filtered.filter((l) => l.changeType === query.changeType)
  if (query.referenceType) filtered = filtered.filter((l) => l.referenceType === query.referenceType)
  if (query.startTime) filtered = filtered.filter((l) => l.createTime >= query.startTime!)
  if (query.endTime) filtered = filtered.filter((l) => l.createTime <= query.endTime!)

  filtered.sort((a, b) => b.createTime.localeCompare(a.createTime))

  const page = query.page || 1
  const pageSize = query.pageSize || 10
  const start = (page - 1) * pageSize
  return { code: 0, msg: 'ok', data: { total: filtered.length, list: filtered.slice(start, start + pageSize) } }
}

/** 获取我的当前积分 */
export async function mockGetMyCreditScore(): Promise<BaseResponse<number>> {
  await delay()
  const lastLog = creditLogList[creditLogList.length - 1]
  return { code: 0, msg: 'ok', data: lastLog ? lastLog.afterScore : 100 }
}

/** 获取积分规则列表 */
export async function mockGetCreditRules(): Promise<BaseResponse<CreditRuleVO[]>> {
  await delay()
  return { code: 0, msg: 'ok', data: [...creditRules] }
}

/** 手动调整积分 */
export async function mockManualAdjustCredit(data: AdjustCreditDTO): Promise<BaseResponse<null>> {
  await delay()
  const lastLog = creditLogList.filter((l) => l.userId === data.userId).pop()
  const beforeScore = lastLog ? lastLog.afterScore : 100
  const newLog: CreditLogVO = {
    id: nextLogId++,
    userId: data.userId,
    changeType: data.changeScore > 0 ? 'REWARD' : 'PUNISH',
    changeScore: data.changeScore,
    beforeScore,
    afterScore: beforeScore + data.changeScore,
    referenceType: 'MANUAL',
    referenceId: 0,
    remark: data.remark,
    createTime: new Date().toISOString().replace('T', ' ').slice(0, 19),
    userName: userNames[data.userId] || '未知用户',
  }
  creditLogList.push(newLog)
  return { code: 0, msg: '调整成功', data: null }
}

/** 管理员：获取指定用户积分流水 */
export async function mockGetUserCreditLogs(
  userId: number,
  query: CreditLogQuery,
): Promise<BaseResponse<PageResponse<CreditLogVO>>> {
  await delay()
  let filtered = creditLogList.filter((l) => l.userId === userId)

  if (query.changeType) filtered = filtered.filter((l) => l.changeType === query.changeType)
  if (query.referenceType) filtered = filtered.filter((l) => l.referenceType === query.referenceType)
  if (query.startTime) filtered = filtered.filter((l) => l.createTime >= query.startTime!)
  if (query.endTime) filtered = filtered.filter((l) => l.createTime <= query.endTime!)

  filtered.sort((a, b) => b.createTime.localeCompare(a.createTime))

  const page = query.page || 1
  const pageSize = query.pageSize || 10
  const start = (page - 1) * pageSize
  return { code: 0, msg: 'ok', data: { total: filtered.length, list: filtered.slice(start, start + pageSize) } }
}

/** 保存积分规则 */
export async function mockSaveCreditRule(data: CreditRuleDTO): Promise<BaseResponse<CreditRuleVO>> {
  await delay()
  if (data.id) {
    const idx = creditRules.findIndex((r) => r.id === data.id)
    if (idx === -1) return { code: 404, msg: '规则不存在', data: null as any }
    Object.assign(creditRules[idx], data, { updateTime: new Date().toISOString().replace('T', ' ').slice(0, 19) })
    return { code: 0, msg: '更新成功', data: { ...creditRules[idx] } }
  }
  const newRule: CreditRuleVO = {
    id: nextRuleId++,
    ruleName: data.ruleName,
    ruleType: data.ruleType,
    changeValue: data.changeValue,
    referenceType: data.referenceType,
    description: data.description,
    isEnabled: data.isEnabled,
    createTime: new Date().toISOString().replace('T', ' ').slice(0, 19),
    updateTime: new Date().toISOString().replace('T', ' ').slice(0, 19),
  }
  creditRules.push(newRule)
  return { code: 0, msg: '保存成功', data: newRule }
}

/** 删除积分规则 */
export async function mockDeleteCreditRule(id: number): Promise<BaseResponse<null>> {
  await delay()
  const idx = creditRules.findIndex((r) => r.id === id)
  if (idx === -1) return { code: 404, msg: '规则不存在', data: null }
  creditRules.splice(idx, 1)
  return { code: 0, msg: '删除成功', data: null }
}

export { creditLogList, creditRules }
