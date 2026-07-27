import type { PageParam } from '@/api/common/type'

/** 定时任务 VO */
export interface JobVO {
  id: number
  jobName: string
  jobGroup: string
  invokeTarget: string
  cronExpression: string
  concurrent: number
  misfirePolicy: string
  status: number
  remark: string
  createTime: string
}

/** 定时任务查询参数 */
export interface JobQuery extends PageParam {
  jobName?: string
  jobGroup?: string
  status?: number
}
