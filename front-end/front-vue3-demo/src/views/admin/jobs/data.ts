import type { JobQuery } from '@/api/job/type'

/** 默认查询参数 */
export const defaultJobQuery: JobQuery = {
  page: 1,
  pageSize: 10,
  jobName: '',
  jobGroup: '',
  status: undefined,
}

/** 表格列配置 */
export const tableColumns = [
  { prop: 'jobName', label: '任务名称', width: 180 },
  { prop: 'jobGroup', label: '任务组', width: 120 },
  { prop: 'cronExpression', label: 'Cron 表达式', width: 180 },
  { prop: 'status', label: '运行状态', width: 100 },
  { prop: 'remark', label: '备注', minWidth: 280 },
  { prop: 'createTime', label: '创建时间', width: 180 },
]
