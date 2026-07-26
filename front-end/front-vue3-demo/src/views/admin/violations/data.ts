import type { BlacklistQuery } from '@/api/blacklist/type'
import type { CreditLogQuery } from '@/api/credit/type'

export const ViolationTypeOptions = [
  { label: '全部', value: '' },
  { label: '严重违约', value: 'SEVERE_DEFAULT' },
  { label: '损坏公物', value: 'DAMAGE' },
  { label: '扰乱秩序', value: 'DISTURBANCE' },
  { label: '违规转让', value: 'FRAUD' },
  { label: '其他违规', value: 'OTHER' },
]

export const BlacklistStatusOptions = [
  { label: '全部', value: '' },
  { label: '生效中', value: 1 },
  { label: '已解除', value: 0 },
]

export const ViolationTypeMap: Record<string, string> = {
  SEVERE_DEFAULT: '严重违约',
  DAMAGE: '损坏公物',
  DISTURBANCE: '扰乱秩序',
  FRAUD: '违规转让',
  OTHER: '其他违规',
}

export const CreditChangeTypeOptions = [
  { label: '全部', value: '' },
  { label: '加分', value: 'REWARD' },
  { label: '扣分', value: 'PUNISH' },
]

export const blacklistColumns = [
  { prop: 'id', label: 'ID', width: 70 },
  { prop: 'userName', label: '用户', width: 100 },
  { prop: 'violationType', label: '违规类型', width: 100 },
  { prop: 'reason', label: '违规原因', minWidth: 160 },
  { prop: 'startTime', label: '封禁开始', width: 170 },
  { prop: 'endTime', label: '封禁结束', width: 170 },
  { prop: 'status', label: '状态', width: 80 },
]

export const creditLogColumns = [
  { prop: 'id', label: 'ID', width: 70 },
  { prop: 'userName', label: '用户', width: 100 },
  { prop: 'changeScore', label: '变动分数', width: 90 },
  { prop: 'afterScore', label: '变动后', width: 80 },
  { prop: 'reason', label: '原因', minWidth: 180 },
  { prop: 'createTime', label: '变动时间', width: 170 },
]

export const defaultBlacklistQuery: BlacklistQuery = {
  page: 1,
  pageSize: 10,
  keyword: '',
  violationType: '',
  status: undefined,
}

export const defaultCreditLogQuery: CreditLogQuery = {
  page: 1,
  pageSize: 10,
  changeType: '',
  userId: undefined,
}

export const defaultBlacklistForm = {
  userId: null as number | null,
  violationType: '' as string,
  reason: '',
  penaltyDays: 7,
}

export const defaultAdjustForm = {
  userId: null as number | null,
  changeScore: 0,
  remark: '',
}
