import type { SpaceQuery } from '@/api/space/type'

export const SpaceTypeOptions = [
  { label: '全部', value: '' },
  { label: '座位', value: 'SEAT' },
  { label: '研讨室', value: 'ROOM' },
]

export const AreaNameOptions = [
  { label: '全部', value: '' },
  { label: '安静学习区', value: '安静学习区' },
  { label: '电子阅览区', value: '电子阅览区' },
  { label: '研讨区', value: '研讨区' },
  { label: '多媒体区', value: '多媒体区' },
  { label: '休闲阅读区', value: '休闲阅读区' },
]

export const FloorOptions = [
  { label: '全部', value: '' },
  { label: '1F', value: '1F' },
  { label: '2F', value: '2F' },
  { label: '3F', value: '3F' },
  { label: '4F', value: '4F' },
]

export const StatusOptions = [
  { label: '全部', value: '' },
  { label: '可用', value: 1 },
  { label: '维修中', value: 0 },
  { label: '停用', value: 2 },
]

export const tableColumns = [
  { prop: 'id', label: 'ID', width: 70 },
  { prop: 'spaceName', label: '空间名称', minWidth: 140 },
  { prop: 'spaceType', label: '空间类型', width: 90 },
  { prop: 'areaName', label: '区域', width: 110 },
  { prop: 'floor', label: '楼层', width: 70 },
  { prop: 'capacity', label: '容纳人数', width: 90 },
  { prop: 'status', label: '状态', width: 90 },
  { prop: 'createTime', label: '创建时间', width: 170 },
]

export const defaultQuery: SpaceQuery = {
  page: 1,
  pageSize: 10,
  keyword: '',
  spaceType: '',
  areaName: '',
  floor: '',
  status: undefined,
}

export const defaultForm = {
  spaceName: '',
  spaceType: 'SEAT',
  areaName: '',
  floor: '1F',
  capacity: 1,
  equipmentConfig: '',
  description: '',
  useRules: '',
  sortOrder: 0,
}

export const SpaceTypeMap: Record<string, string> = {
  SEAT: '座位',
  ROOM: '研讨室',
}
