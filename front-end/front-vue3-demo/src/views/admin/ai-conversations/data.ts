import type { AIConversationQuery } from '@/api/ai/type'

/** 默认查询参数 */
export const defaultConversationQuery: AIConversationQuery & { keyword?: string; dateRange: [string, string] | [] } = {
  page: 1,
  pageSize: 10,
  userId: undefined,
  convType: '',
  keyword: '',
  startTime: '',
  endTime: '',
  dateRange: [],
}

/** 表格列配置 */
export const tableColumns = [
  { prop: 'userName', label: '用户', width: 100 },
  { prop: 'question', label: '问题', minWidth: 240 },
  { prop: 'answer', label: '回答', minWidth: 240 },
  { prop: 'convType', label: '类型', width: 100 },
  { prop: 'isFavorite', label: '收藏', width: 70 },
  { prop: 'isUseful', label: '有用', width: 70 },
  { prop: 'createTime', label: '对话时间', width: 180 },
]

/** 对话类型选项 */
export const convTypeOptions = [
  { label: '全部', value: '' },
  { label: '问答', value: 'QA' },
  { label: '推荐', value: 'RECOMMEND' },
]
