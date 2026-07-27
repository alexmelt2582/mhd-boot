import type { ReservationQuery } from '@/api/reservation/type'

export const StatusOptions = [
  { label: '全部', value: '' },
  { label: '已预约', value: 0 },
  { label: '已签到', value: 1 },
  { label: '已完成', value: 2 },
  { label: '已取消', value: 3 },
  { label: '已违约', value: 4 },
]

export const ApprovalStatusOptions = [
  { label: '全部', value: '' },
  { label: '无需审批', value: 1 },
  { label: '待审批', value: 2 },
  { label: '已通过', value: 3 },
  { label: '已拒绝', value: 4 },
]

export const tableColumns = [
  { prop: 'id', label: 'ID', width: 70 },
  { prop: 'reservationCode', label: '预约码', width: 160 },
  { prop: 'spaceName', label: '空间名称', minWidth: 130 },
  { prop: 'userName', label: '用户', width: 100 },
  { prop: 'startTime', label: '开始时间', width: 170 },
  { prop: 'endTime', label: '结束时间', width: 170 },
  { prop: 'status', label: '预约状态', width: 90 },
  { prop: 'approvalStatus', label: '审批状态', width: 90 },
]

export const defaultQuery: ReservationQuery = {
  page: 1,
  pageSize: 10,
  keyword: '',
  status: undefined,
  approvalStatus: undefined,
  startTime: '',
  endTime: '',
}
