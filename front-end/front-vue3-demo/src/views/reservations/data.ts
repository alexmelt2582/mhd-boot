import type { ReservationQuery } from '@/api/reservation/type'

/** 预约状态 Tab 选项 */
export const statusTabOptions = [
  { label: '全部', value: '' },
  { label: '待履约', value: 0 },
  { label: '已签到', value: 1 },
  { label: '已完成', value: 2 },
  { label: '已取消', value: 3 },
  { label: '已违约', value: 4 },
]

/** 空间类型筛选选项 */
export const spaceTypeOptions = [
  { label: '全部类型', value: '' },
  { label: '座位', value: 'SEAT' },
  { label: '研讨室', value: 'ROOM' },
]

/** 默认查询参数 */
export const defaultSearchParams: ReservationQuery & { dateRange: [string, string] | [] } = {
  page: 1,
  pageSize: 8,
  status: undefined,
  spaceType: '',
  startTime: '',
  endTime: '',
  keyword: '',
  dateRange: [],
}

/** 搜索表单 schema（供 search 组件使用） */
export const searchFormSchema = [
  { prop: 'keyword', label: '关键词', type: 'input', placeholder: '搜索预约码/空间名/用途' },
  { prop: 'spaceType', label: '类型', type: 'select', options: [] },
  { prop: 'dateRange', label: '日期范围', type: 'daterange' },
]

/** 表格列配置（备选 table 视图） */
export const tableColumns = [
  { prop: 'id', label: 'ID', width: 60 },
  { prop: 'reservationCode', label: '预约码', width: 160 },
  { prop: 'spaceName', label: '空间名称', width: 140 },
  { prop: 'spaceType', label: '空间类型', width: 80 },
  { prop: 'startTime', label: '开始时间', width: 160 },
  { prop: 'endTime', label: '结束时间', width: 160 },
  { prop: 'status', label: '预约状态', width: 90 },
  { prop: 'approvalStatus', label: '审批状态', width: 90 },
  { prop: 'purpose', label: '用途', width: 140 },
  { prop: 'createTime', label: '创建时间', width: 160 },
]
