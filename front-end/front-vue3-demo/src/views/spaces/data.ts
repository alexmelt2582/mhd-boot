import type { SpaceQuery } from '@/api/space/type'

export const defaultSearchParams: SpaceQuery = {
  page: 1,
  pageSize: 12,
  spaceType: '',
  areaName: '',
  floor: '',
  keyword: '',
}

export const searchFormSchema = [
  { prop: 'keyword', label: '关键词', type: 'input', placeholder: '搜索空间名称' },
  { prop: 'spaceType', label: '类型', type: 'select', options: [] },
  { prop: 'areaName', label: '区域', type: 'select', options: [] },
  { prop: 'floor', label: '楼层', type: 'select', options: [] },
]

export const tableColumns = [
  { prop: 'id', label: 'ID', width: 60 },
  { prop: 'spaceName', label: '名称', width: 160 },
  { prop: 'spaceType', label: '类型', width: 80 },
  { prop: 'areaName', label: '区域', width: 120 },
  { prop: 'floor', label: '楼层', width: 60 },
  { prop: 'capacity', label: '容纳人数', width: 80 },
  { prop: 'status', label: '状态', width: 80 },
  { prop: 'createTime', label: '创建时间', width: 160 },
]
