import type { BaseResponse, PageResponse } from '@/utils/service'
import type { JobVO, JobQuery } from './type'

const delay = () => new Promise((r) => setTimeout(r, 200 + Math.random() * 300))

const jobList: JobVO[] = [
  {
    id: 1,
    jobName: '清理过期预约',
    jobGroup: '预约管理',
    invokeTarget: 'com.mhd.boot.job.ReservationJob.cleanExpiredReservations',
    cronExpression: '0 0 2 * * ?',
    concurrent: 0,
    misfirePolicy: 'DO_NOTHING',
    status: 1,
    remark: '每天凌晨2点执行，将超时未签到的预约状态更新为"已过期"',
    createTime: '2026-01-15 10:00:00',
  },
  {
    id: 2,
    jobName: '信用积分年重置',
    jobGroup: '信用管理',
    invokeTarget: 'com.mhd.boot.job.CreditJob.resetYearlyCredit',
    cronExpression: '0 0 0 1 1 ?',
    concurrent: 0,
    misfirePolicy: 'DO_NOTHING',
    status: 1,
    remark: '每年1月1日凌晨执行，重置所有用户的年度信用积分统计',
    createTime: '2026-01-15 10:30:00',
  },
  {
    id: 3,
    jobName: '发送预约提醒',
    jobGroup: '消息通知',
    invokeTarget: 'com.mhd.boot.job.ReminderJob.sendReservationReminder',
    cronExpression: '0 */15 * * * ?',
    concurrent: 1,
    misfirePolicy: 'IGNORE_MISFIRES',
    status: 1,
    remark: '每15分钟执行一次，向前30分钟内即将开始的预约发送提醒通知',
    createTime: '2026-02-10 14:00:00',
  },
  {
    id: 4,
    jobName: '生成使用统计报表',
    jobGroup: '统计分析',
    invokeTarget: 'com.mhd.boot.job.ReportJob.generateUsageReport',
    cronExpression: '0 0 1 * * ?',
    concurrent: 0,
    misfirePolicy: 'DO_NOTHING',
    status: 1,
    remark: '每天凌晨1点执行，生成前一日各空间使用率、签到率等统计报表',
    createTime: '2026-02-10 14:30:00',
  },
  {
    id: 5,
    jobName: '清理过期文件',
    jobGroup: '文件管理',
    invokeTarget: 'com.mhd.boot.job.FileJob.cleanExpiredFiles',
    cronExpression: '0 0 3 * * ?',
    concurrent: 0,
    misfirePolicy: 'DO_NOTHING',
    status: 0,
    remark: '每天凌晨3点执行（已暂停），清理超过90天的临时上传文件和导出报表',
    createTime: '2026-03-01 09:00:00',
  },
  {
    id: 6,
    jobName: '数据库备份',
    jobGroup: '系统维护',
    invokeTarget: 'com.mhd.boot.job.BackupJob.databaseBackup',
    cronExpression: '0 0 4 * * ?',
    concurrent: 0,
    misfirePolicy: 'DO_NOTHING',
    status: 1,
    remark: '每天凌晨4点执行，自动备份数据库至MinIO对象存储',
    createTime: '2026-03-15 11:00:00',
  },
]

export async function mockGetJobList(query: JobQuery): Promise<BaseResponse<PageResponse<JobVO>>> {
  await delay()
  let filtered = [...jobList]

  if (query.jobName) {
    filtered = filtered.filter((j) => j.jobName.includes(query.jobName!))
  }
  if (query.jobGroup) {
    filtered = filtered.filter((j) => j.jobGroup === query.jobGroup)
  }
  if (query.status !== undefined) {
    filtered = filtered.filter((j) => j.status === query.status)
  }

  const page = query.page || 1
  const pageSize = query.pageSize || 10
  const start = (page - 1) * pageSize
  const list = filtered.slice(start, start + pageSize)

  return { code: 0, msg: 'ok', data: { total: filtered.length, list } }
}

export async function mockUpdateJob(data: Partial<JobVO> & { id: number }): Promise<BaseResponse<null>> {
  await delay()
  const idx = jobList.findIndex((j) => j.id === data.id)
  if (idx === -1) return { code: 404, msg: '任务不存在', data: null }
  Object.assign(jobList[idx], data)
  return { code: 0, msg: '更新成功', data: null }
}

export async function mockTriggerJob(id: number): Promise<BaseResponse<null>> {
  await delay()
  const job = jobList.find((j) => j.id === id)
  if (!job) return { code: 404, msg: '任务不存在', data: null }
  if (job.status === 0) return { code: 1001, msg: '任务已暂停，请先恢复后再执行', data: null }
  return { code: 0, msg: `任务"${job.jobName}"已触发执行`, data: null }
}

export async function mockPauseJob(id: number): Promise<BaseResponse<null>> {
  await delay()
  const job = jobList.find((j) => j.id === id)
  if (!job) return { code: 404, msg: '任务不存在', data: null }
  if (job.status === 0) return { code: 1002, msg: '任务已是暂停状态', data: null }
  job.status = 0
  return { code: 0, msg: `任务"${job.jobName}"已暂停`, data: null }
}

export async function mockResumeJob(id: number): Promise<BaseResponse<null>> {
  await delay()
  const job = jobList.find((j) => j.id === id)
  if (!job) return { code: 404, msg: '任务不存在', data: null }
  if (job.status === 1) return { code: 1003, msg: '任务已是运行状态', data: null }
  job.status = 1
  return { code: 0, msg: `任务"${job.jobName}"已恢复`, data: null }
}

export { jobList }
